package com.arcanumrealms.battle;

import com.arcanumrealms.ArcanumConfig;
import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.core.battle.*;
import com.arcanumrealms.core.minion.CreatureDef;
import com.arcanumrealms.core.minion.Creatures;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.core.util.Rng;
import com.arcanumrealms.core.util.StatsFactory;
import com.arcanumrealms.network.NetworkHandler;
import com.arcanumrealms.network.packet.BattleStatePacket;
import com.arcanumrealms.player.WizardData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

/**
 * Chef d'orchestre des duels cote serveur.
 *
 * <p>Il fait le lien entre les entites Minecraft et le moteur de duel :
 * <ul>
 *   <li>il detecte les creatures hostiles autour des sorciers et declenche un duel (option) ;</li>
 *   <li>il gere les arenes (cercles arcaniques) ;</li>
 *   <li>il fait tourner les rounds : phase de choix, resolution, pause de lecture ;</li>
 *   <li>il synchronise la vie affichee (barres de vie Minecraft = pourcentage de vie de duel) ;</li>
 *   <li>il distribue l'experience et libere les creatures a la fin.</li>
 * </ul>
 */
public class ServerBattleManager {

    public static final ServerBattleManager INSTANCE = new ServerBattleManager();

    /** Correspondance entre entites Minecraft et creatures du mod. */
    private static final Map<EntityType<?>, String> TYPE_TO_CREATURE = Map.ofEntries(
            Map.entry(EntityType.ZOMBIE, "ember_imp"),
            Map.entry(EntityType.HUSK, "grave_shade"),
            Map.entry(EntityType.DROWNED, "balance_scribe"),
            Map.entry(EntityType.SKELETON, "static_wisp"),
            Map.entry(EntityType.STRAY, "frost_wraith"),
            Map.entry(EntityType.SPIDER, "bramble_thrall"),
            Map.entry(EntityType.CAVE_SPIDER, "rune_gnome"),
            Map.entry(EntityType.CREEPER, "rune_gnome"),
            Map.entry(EntityType.WITHER_SKELETON, "cinder_tyrant"),
            Map.entry(EntityType.ZOMBIFIED_PIGLIN, "frost_matriarch"),
            Map.entry(EntityType.PILLAGER, "balance_scribe"),
            Map.entry(EntityType.VINDICATOR, "glacier_hound"),
            Map.entry(EntityType.EVOKER, "frost_wraith"),
            Map.entry(EntityType.WITCH, "grave_shade"),
            Map.entry(EntityType.SLIME, "bramble_thrall"),
            Map.entry(EntityType.MAGMA_CUBE, "cinder_tyrant"),
            Map.entry(EntityType.PHANTOM, "static_wisp"),
            Map.entry(EntityType.SILVERFISH, "rune_gnome"),
            Map.entry(EntityType.ENDERMITE, "rune_gnome"),
            Map.entry(EntityType.BLAZE, "cinder_tyrant"),
            Map.entry(EntityType.WITHER, "cinder_tyrant")
    );

    /** Etat d'un duel cote serveur. */
    public enum State { PLANNING, RESOLVING, FINISHED }

    public static final class Session {
        public final String id;
        public final ServerLevel level;
        public final Vec3 center;
        public final Battle battle;
        public final Map<UUID, WizardData> wizards = new LinkedHashMap<>();
        public final Map<String, UUID> playerIds = new LinkedHashMap<>();
        public final Map<String, LivingEntity> entities = new LinkedHashMap<>();
        public final Map<String, CreatureDef> creatures = new LinkedHashMap<>();
        public final List<BattleEvent> recentEvents = new ArrayList<>();
        public State state = State.PLANNING;
        public int timer;
        public boolean pvp;

        Session(String id, ServerLevel level, Vec3 center, Battle battle) {
            this.id = id;
            this.level = level;
            this.center = center;
            this.battle = battle;
        }

        public boolean hasPlayer(UUID uuid) { return wizards.containsKey(uuid); }
        public boolean hasEntity(LivingEntity entity) { return entities.containsValue(entity); }
    }

    private final Map<String, Session> sessions = new LinkedHashMap<>();
    private final Map<UUID, Session> byPlayer = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private int nextSession = 1;
    private int tickCounter;

    private ServerBattleManager() {}

    // ------------------------------------------------------- declencheurs

    /** Clic droit d'une baguette sur une creature : duel avec les creatures proches. */
    public void startDuelWithNearby(ServerPlayer player, LivingEntity firstTarget) {
        if (byPlayer.containsKey(player.getUUID())) {
            player.displayClientMessage(Component.translatable("arcanum_realms.message.already_in_duel"), true);
            return;
        }
        List<LivingEntity> nearby = collectNearbyCreatures(player, firstTarget);
        if (nearby.isEmpty()) {
            player.displayClientMessage(Component.translatable("arcanum_realms.message.no_creature"), true);
            return;
        }
        List<ServerPlayer> allies = collectNearbyWizards(player);
        startDuel(player, nearby, allies, player.position());
    }

    /** Clic droit d'une baguette sur un cercle arcanique : rencontre de l'arene. */
    public void startArenaDuel(ServerPlayer player, BlockPos pos) {
        if (byPlayer.containsKey(player.getUUID())) {
            player.displayClientMessage(Component.translatable("arcanum_realms.message.already_in_duel"), true);
            return;
        }
        List<String> encounter = pickEncounter();
        if (encounter.isEmpty()) {
            player.displayClientMessage(Component.translatable("arcanum_realms.message.no_encounter"), true);
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) return;
        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);

        BattleConfig config = buildBattleConfig();
        Battle battle = new Battle("duel-" + (nextSession++), config, new Rng(level.getRandom().nextLong()), new Host());
        Session session = new Session(battle.id(), level, center, battle);

        // Les sorciers proches rejoignent le duel
        session.wizards.put(player.getUUID(), WizardData.load(player));
        for (ServerPlayer ally : collectNearbyWizards(player)) {
            if (session.wizards.size() >= ArcanumConfig.MAX_PLAYERS.get()) break;
            session.wizards.put(ally.getUUID(), WizardData.load(ally));
        }
        for (Map.Entry<UUID, WizardData> e : session.wizards.entrySet()) {
            ServerPlayer sp = level.getServer().getPlayerList().getPlayer(e.getKey());
            if (sp == null) continue;
            WizardData data = e.getValue();
            data.ensureStarterDeck();
            Combatant combatant = buildWizardCombatant(sp, data, session);
            battle.addCombatant(combatant);
            session.playerIds.put(combatant.id(), sp.getUUID());
        }

        // Les creatures de la rencontre apparaissent dans le cercle
        for (int i = 0; i < encounter.size(); i++) {
            CreatureDef def = Creatures.get(encounter.get(i));
            spawnArenaCreature(session, def, i, encounter.size());
        }

        register(session, player);
    }

    /** Boucle serveur : duels automatiques + progression des sessions. */
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        tickCounter++;

        if (tickCounter % 20 == 0) {
            autoTrigger(server);
            cooldowns.entrySet().removeIf(e -> e.getValue() <= server.getTickCount());
        }

        if (tickCounter % 5 == 0) {
            for (Session session : new ArrayList<>(sessions.values())) advance(session);
        }
        for (Session session : new ArrayList<>(sessions.values())) syncHealth(session);
        if (tickCounter % 20 == 0) {
            for (Session session : new ArrayList<>(sessions.values())) NetworkHandler.broadcastState(session);
        }
    }

    /** Deteche les joueurs qui se deconnectent et libere leurs creatures. */
    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Session session = byPlayer.get(player.getUUID());
        if (session != null) leaveDuel(session, player.getUUID(), true);
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!ArcanumConfig.INVULNERABLE_DURING_DUEL.get()) return;
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            if (byPlayer.containsKey(player.getUUID())) event.setCanceled(true);
        } else {
            for (Session session : sessions.values()) {
                if (session.hasEntity(entity)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        for (Session session : new ArrayList<>(sessions.values())) endSession(session, false);
        sessions.clear();
        byPlayer.clear();
    }

    // ---------------------------------------------------------- demarrage

    /** Declenche un duel contre des creatures donnees. */
    public Session startDuel(ServerPlayer initiator, List<LivingEntity> mobs, List<ServerPlayer> allies, Vec3 center) {
        if (mobs.isEmpty()) return null;
        if (!(initiator.level() instanceof ServerLevel level)) return null;

        BattleConfig config = buildBattleConfig();
        Battle battle = new Battle("duel-" + (nextSession++), config, new Rng(level.getRandom().nextLong()), new Host());
        Session session = new Session(battle.id(), level, center, battle);

        // Sorciers
        session.wizards.put(initiator.getUUID(), WizardData.load(initiator));
        for (ServerPlayer ally : allies) {
            if (session.wizards.size() >= ArcanumConfig.MAX_PLAYERS.get()) break;
            session.wizards.put(ally.getUUID(), WizardData.load(ally));
        }
        for (Map.Entry<UUID, WizardData> e : session.wizards.entrySet()) {
            ServerPlayer sp = level.getServer().getPlayerList().getPlayer(e.getKey());
            if (sp == null) continue;
            WizardData data = e.getValue();
            data.ensureStarterDeck();
            Combatant combatant = buildWizardCombatant(sp, data, session);
            battle.addCombatant(combatant);
            session.playerIds.put(combatant.id(), sp.getUUID());
        }

        // Creatures
        int index = 0;
        for (LivingEntity mob : mobs) {
            if (index >= ArcanumConfig.MAX_CREATURES.get()) break;
            CreatureDef def = defFor(mob, session.wizards.values().stream()
                    .mapToInt(WizardData::level).max().orElse(1));
            Combatant combatant = buildCreatureCombatant(mob, def);
            battle.addCombatant(combatant);
            session.entities.put(combatant.id(), mob);
            session.creatures.put(combatant.id(), def);
            prepareEntity(mob, session, index);
            index++;
        }
        if (session.entities.isEmpty()) return null;

        register(session, initiator);
        return session;
    }

    private void register(Session session, ServerPlayer initiator) {
        sessions.put(session.id, session);
        for (UUID uuid : session.wizards.keySet()) byPlayer.put(uuid, session);

        // Positionne tout le monde dans le cercle de duel
        arrangeCircle(session);

        session.battle.start();
        session.recentEvents.addAll(session.battle.drainEvents());
        session.state = State.PLANNING;
        session.timer = ArcanumConfig.PLANNING_SECONDS.get() * 20;

        for (UUID uuid : session.wizards.keySet()) {
            ServerPlayer sp = session.level.getServer().getPlayerList().getPlayer(uuid);
            if (sp != null) sp.displayClientMessage(Component.translatable("arcanum_realms.message.duel_started"), false);
        }
        NetworkHandler.broadcastState(session);
        logDuelStart(session);
    }

    private void logDuelStart(Session session) {
        ArcanumRealms.LOGGER.debug("[Arcanum Realms] Duel {} lance ({})",
                session.id, session.battle.combatants().size());
    }

    private BattleConfig buildBattleConfig() {
        return BattleConfig.builder()
                .handSize(7)
                .maxPipSlots(CombatantPips.MAX_SLOTS)
                .fizzle(ArcanumConfig.FIZZLE_ENABLED.get())
                .critMode(ArcanumConfig.CRITICAL_ENABLED.get() ? BattleConfig.CritMode.RATING : BattleConfig.CritMode.FLAT)
                .critMultiplier(2.0)
                .damageScale(ArcanumConfig.DAMAGE_SCALE.get())
                .healScale(ArcanumConfig.HEAL_SCALE.get())
                .planningSeconds(ArcanumConfig.PLANNING_SECONDS.get())
                .allowFlee(ArcanumConfig.ALLOW_FLEE.get())
                .maxRounds(ArcanumConfig.MAX_ROUNDS.get())
                .turnOrder(BattleConfig.TurnOrderStrategy.ALTERNATING)
                .build();
    }

    private Combatant buildWizardCombatant(ServerPlayer player, WizardData data, Session session) {
        StatsFactory.WizardGear gear = gear(data);
        return Combatant.builder(player.getUUID().toString(), player.getName().getString())
                .kind(Combatant.Kind.WIZARD)
                .school(data.school())
                .level(data.level())
                .team(Team.PLAYERS)
                .stats(StatsFactory.forWizard(data.school(), data.level(), gear))
                .health(StatsFactory.healthFor(data.school(), data.level()))
                .deck(data.buildCardList())
                .handSize(7)
                .rng(session.battle.rng())
                .build();
    }

    /** Equipement d'un sorcier : pour l'instant issu du niveau et de la configuration. */
    public static StatsFactory.WizardGear gear(WizardData data) {
        int powerPips = data.powerPipChance() >= 0 ? data.powerPipChance() : ArcanumConfig.BASE_POWER_PIP_CHANCE.get();
        int crit = data.critRating() >= 0 ? data.critRating()
                : data.level() * ArcanumConfig.CRIT_RATING_PER_LEVEL.get();
        Map<School, Integer> damage = new EnumMap<>(School.class);
        Map<School, Integer> resist = new EnumMap<>(School.class);
        int baseDamage = ArcanumConfig.BASE_DAMAGE_BONUS.get();
        int baseResist = ArcanumConfig.BASE_RESIST.get();
        for (School school : School.values()) {
            if (baseDamage > 0) damage.put(school, baseDamage);
            if (baseResist > 0) resist.put(school, baseResist);
        }
        return new StatsFactory.WizardGear(powerPips, crit, 0, ArcanumConfig.ACCURACY_BONUS.get(), 0, 0, 0,
                damage, resist, Map.of());
    }

    private Combatant buildCreatureCombatant(LivingEntity mob, CreatureDef def) {
        return Combatant.builder("mob-" + mob.getId(), def.name(false))
                .kind(Combatant.Kind.ENEMY)
                .school(def.school())
                .level(def.level())
                .team(Team.ENEMIES)
                .stats(StatsFactory.forCreature(def))
                .def(def)
                .ai(new com.arcanumrealms.core.ai.EnemyAI())
                .health(def.health())
                .deck(new ArrayList<>(def.deck()))
                .handSize(7)
                .rng(new Rng(mob.getId() * 2654435761L))
                .build();
    }

    // --------------------------------------------------------- progression

    /** Fait avancer un duel : phase de choix, resolution, pause. */
    private void advance(Session session) {
        if (session.state == State.FINISHED) return;
        Battle battle = session.battle;

        checkLeash(session);
        if (session.state == State.FINISHED) return;

        if (session.state == State.PLANNING) {
            boolean everyoneChosen = battle.allChosen();
            if (!everyoneChosen && --session.timer > 0) return;

            // Les joueurs qui n'ont rien choisi passent leur tour
            for (Combatant combatant : battle.living()) {
                if (!combatant.isPlayer()) continue;
                if (battle.actionOf(combatant.id()) == null) battle.submit(combatant.id(), BattleAction.pass());
            }

            List<BattleEvent> events = battle.resolveRound();
            session.recentEvents.clear();
            session.recentEvents.addAll(events);
            session.state = State.RESOLVING;
            session.timer = ArcanumConfig.RESOLVE_DELAY_TICKS.get();

            for (UUID uuid : session.wizards.keySet()) {
                ServerPlayer sp = session.level.getServer().getPlayerList().getPlayer(uuid);
                if (sp == null) continue;
                for (BattleEvent e : events) {
                    if (e instanceof BattleEvent.Planning || e instanceof BattleEvent.RoundStart) continue;
                    sp.displayClientMessage(Component.literal(e.text(true)), false);
                }
            }
            NetworkHandler.broadcastState(session);
            return;
        }

        if (session.state == State.RESOLVING && --session.timer <= 0) {
            if (battle.isFinished()) {
                endSession(session, true);
            } else {
                session.state = State.PLANNING;
                session.timer = ArcanumConfig.PLANNING_SECONDS.get() * 20;
                NetworkHandler.broadcastState(session);
            }
        }
    }

    private void checkLeash(Session session) {
        int radius = ArcanumConfig.LEASH_RADIUS.get();
        for (UUID uuid : new ArrayList<>(session.wizards.keySet())) {
            ServerPlayer player = session.level.getServer().getPlayerList().getPlayer(uuid);
            if (player == null) continue;
            if (player.distanceToSqr(session.center) > (double) radius * radius) {
                player.displayClientMessage(Component.translatable("arcanum_realms.message.fled_too_far"), false);
                leaveDuel(session, uuid, true);
            }
        }
    }

    /** Un joueur quitte le duel (fuite, deconnexion, eloignement). */
    public void leaveDuel(Session session, UUID uuid, boolean flee) {
        Combatant combatant = session.battle.get(uuid.toString());
        if (combatant == null) return;
        if (flee && combatant.alive()) {
            combatant.flee();
            session.recentEvents.add(new BattleEvent.Fled(combatant.name()));
            ServerPlayer player = session.level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                player.displayClientMessage(Component.translatable("arcanum_realms.message.you_fled"), false);
                restorePlayerHealth(player, session);
            }
        }
        session.wizards.remove(uuid);
        session.playerIds.remove(uuid.toString());
        byPlayer.remove(uuid);
        if (session.wizards.isEmpty()) {
            endSession(session, false);
        } else {
            NetworkHandler.broadcastState(session);
        }
    }

    /** Fin du duel : recompenses, liberation des creatures, nettoyage. */
    private void endSession(Session session, boolean grantRewards) {
        if (session.state == State.FINISHED) return;
        session.state = State.FINISHED;
        sessions.remove(session.id);

        boolean playersWon = session.battle.winner() == Team.PLAYERS;

        for (UUID uuid : session.wizards.keySet()) {
            byPlayer.remove(uuid);
            ServerPlayer player = session.level.getServer().getPlayerList().getPlayer(uuid);
            cooldowns.put(uuid, session.level.getServer().getTickCount() + ArcanumConfig.TRIGGER_COOLDOWN_SECONDS.get() * 20L);
            if (player == null) continue;

            if (ArcanumConfig.RESTORE_HEALTH_AFTER_DUEL.get()) restorePlayerHealth(player, session);

            if (grantRewards && playersWon) {
                int xp = 0;
                for (CreatureDef def : session.creatures.values()) {
                    xp += def.level() * ArcanumConfig.XP_PER_CREATURE_LEVEL.get();
                }
                player.giveExperiencePoints(Math.max(1, xp / 2));
                WizardData data = session.wizards.get(uuid);
                int levels = data.grantXp(xp);
                data.save(player);
                player.displayClientMessage(Component.translatable("arcanum_realms.message.victory", xp), false);
                if (levels > 0) {
                    player.displayClientMessage(Component.translatable("arcanum_realms.message.level_up", data.level()), true);
                }
            } else if (!playersWon) {
                player.displayClientMessage(Component.translatable("arcanum_realms.message.defeat"), false);
                float damage = (float) (ArcanumConfig.DEFEAT_DAMAGE.get() / 2.0);
                if (damage > 0) {
                    player.invulnerableTime = 0;
                    player.hurt(player.damageSources().generic(), damage);
                }
            }
        }

        // Sort des creatures
        for (Map.Entry<String, LivingEntity> entry : session.entities.entrySet()) {
            LivingEntity entity = entry.getValue();
            Combatant combatant = session.battle.get(entry.getKey());
            boolean defeated = combatant == null || !combatant.alive();
            if (defeated || (grantRewards && playersWon)) {
                entity.discard();
            } else {
                releaseEntity(entity);
            }
        }
        session.entities.clear();
        NetworkHandler.broadcastEnd(session);
    }

    private void restorePlayerHealth(ServerPlayer player, Session session) {
        Combatant combatant = session == null ? null : session.battle.get(player.getUUID().toString());
        double percent = combatant == null ? 1.0 : combatant.healthPercent();
        float health = (float) Math.max(1.0, percent * player.getMaxHealth());
        player.setHealth(Math.min(health, player.getMaxHealth()));
    }

    // ------------------------------------------------------ etat du monde

    private void arrangeCircle(Session session) {
        double radius = ArcanumConfig.ARENA_RADIUS.get();
        int total = session.entities.size();
        int index = 0;
        for (Map.Entry<String, LivingEntity> entry : session.entities.entrySet()) {
            LivingEntity entity = entry.getValue();
            double angle = (Math.PI * 2 / Math.max(1, total)) * index + Math.PI / 2;
            double x = session.center.x + Math.cos(angle) * radius;
            double z = session.center.z + Math.sin(angle) * radius;
            entity.teleportTo(x, session.center.y, z);
            index++;
        }
        int players = session.wizards.size();
        int pIndex = 0;
        for (UUID uuid : session.wizards.keySet()) {
            ServerPlayer player = session.level.getServer().getPlayerList().getPlayer(uuid);
            if (player == null) continue;
            double angle = (Math.PI * 2 / Math.max(1, players)) * pIndex - Math.PI / 2;
            double x = session.center.x + Math.cos(angle) * radius;
            double z = session.center.z + Math.sin(angle) * radius;
            player.teleportTo(x, session.center.y, z);
            pIndex++;
        }
    }

    private void prepareEntity(LivingEntity entity, Session session, int index) {
        entity.setInvulnerable(true);
        entity.setGlowingTag(true);
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
            mob.setTarget(null);
            mob.setPersistenceRequired();
        }
        entity.setDeltaMovement(Vec3.ZERO);
        entity.hurtMarked = true;
    }

    private void releaseEntity(LivingEntity entity) {
        entity.setInvulnerable(false);
        entity.setGlowingTag(false);
        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
            mob.setPersistenceRequired();
        }
    }

    /** La barre de vie Minecraft affiche le pourcentage de vie du duel. */
    private void syncHealth(Session session) {
        for (Combatant combatant : session.battle.combatants()) {
            double percent = Math.max(0, Math.min(1, combatant.healthPercent()));
            if (combatant.isPlayer()) {
                ServerPlayer player = session.level.getServer().getPlayerList().getPlayer(java.util.UUID.fromString(combatant.id()));
                if (player == null) continue;
                float health = (float) Math.max(0.5, percent * player.getMaxHealth());
                if (Math.abs(player.getHealth() - health) > 0.05f) player.setHealth(health);
            } else {
                LivingEntity entity = session.entities.get(combatant.id());
                if (entity == null) continue;
                if (!combatant.alive()) {
                    entity.discard();
                    session.entities.remove(combatant.id());
                    continue;
                }
                float health = (float) Math.max(0.5, percent * entity.getMaxHealth());
                if (Math.abs(entity.getHealth() - health) > 0.05f) entity.setHealth(health);
            }
        }
    }

    // ------------------------------------------------------------ actions

    public boolean submitAction(ServerPlayer player, BattleAction action) {
        Session session = byPlayer.get(player.getUUID());
        if (session == null || session.state != State.PLANNING) return false;
        Combatant combatant = session.battle.get(player.getUUID().toString());
        if (combatant == null || !combatant.alive()) return false;
        boolean ok = session.battle.submit(combatant.id(), action);
        if (ok) NetworkHandler.broadcastState(session);
        return ok;
    }

    public boolean discardCard(ServerPlayer player, int handIndex) {
        Session session = byPlayer.get(player.getUUID());
        if (session == null || session.state != State.PLANNING) return false;
        boolean ok = session.battle.discard(player.getUUID().toString(), handIndex);
        if (ok) NetworkHandler.broadcastState(session);
        return ok;
    }

    public Session sessionOf(Player player) { return byPlayer.get(player.getUUID()); }
    public Session sessionById(String id) { return sessions.get(id); }
    public Collection<Session> sessions() { return Collections.unmodifiableCollection(sessions.values()); }

    /** Force le passage au round suivant (commande de test). */
    public void forceResolve(Session session) {
        if (session.state == State.PLANNING) session.timer = 0;
    }

    // --------------------------------------------------------- utilitaires

    private List<LivingEntity> collectNearbyCreatures(ServerPlayer player, LivingEntity first) {
        double radius = ArcanumConfig.TRIGGER_RADIUS.get();
        List<LivingEntity> found = new ArrayList<>();
        if (first != null && isDuelable(first)) found.add(first);
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius))) {
            if (found.size() >= ArcanumConfig.MAX_CREATURES.get()) break;
            if (mob == first) continue;
            if (isDuelable(mob) && !isBound(mob) && mob.hasLineOfSight(player)) found.add(mob);
        }
        return found;
    }

    private List<ServerPlayer> collectNearbyWizards(ServerPlayer player) {
        double radius = ArcanumConfig.TRIGGER_RADIUS.get();
        List<ServerPlayer> allies = new ArrayList<>();
        for (ServerPlayer other : player.level().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(radius))) {
            if (other == player) continue;
            if (byPlayer.containsKey(other.getUUID())) continue;
            if (other.isCreative() || other.isSpectator()) continue;
            allies.add(other);
        }
        return allies;
    }

    private boolean isBound(LivingEntity entity) {
        for (Session session : sessions.values()) if (session.hasEntity(entity)) return true;
        return false;
    }

    /** Une creature peut provoquer un duel si c'est un monstre hostile "normal". */
    private boolean isDuelable(LivingEntity entity) {
        if (!(entity instanceof Monster)) return false;
        if (!entity.isAlive()) return false;
        if (entity.isInvulnerable()) return false;
        if (entity.getPersistentData().getBoolean("arcanum_realms:no_duel")) return false;
        return !isBound(entity);
    }

    /** Detecte les creatures hostiles autour des sorciers et lance un duel. */
    private void autoTrigger(MinecraftServer server) {
        if (!ArcanumConfig.AUTO_TRIGGER.get()) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (byPlayer.containsKey(player.getUUID())) continue;
            if (player.isCreative() || player.isSpectator()) continue;
            Long ready = cooldowns.get(player.getUUID());
            if (ready != null && ready > server.getTickCount()) continue;
            if (player.getPersistentData().getBoolean("arcanum_realms:no_duel")) continue;

            List<LivingEntity> found = collectNearbyCreatures(player, null);
            if (found.isEmpty()) continue;
            List<ServerPlayer> allies = collectNearbyWizards(player);
            startDuel(player, found, allies, player.position());
            return;
        }
    }

    private List<String> pickEncounter() {
        List<? extends String> encounters = ArcanumConfig.ARENA_ENCOUNTERS.get();
        if (encounters.isEmpty()) return List.of();
        String raw = encounters.get(new Random().nextInt(encounters.size()));
        List<String> out = new ArrayList<>();
        for (String part : raw.split(",")) {
            String id = part.trim();
            if (id.isEmpty()) continue;
            if (Creatures.all().stream().anyMatch(d -> d.id().equals(id))) out.add(id);
        }
        return out;
    }

    /** Choisit un modele de creature adapte au niveau du sorcier. */
    private CreatureDef defFor(LivingEntity entity, int playerLevel) {
        String id = TYPE_TO_CREATURE.get(entity.getType());
        CreatureDef def;
        if (id == null) {
            List<CreatureDef> wanderers = Creatures.wanderers();
            def = wanderers.get(Math.floorMod(entity.getId(), wanderers.size()));
        } else {
            def = Creatures.get(id);
        }
        // On evite de jeter un sorcier de niveau 2 contre un boss de niveau 20
        if (def.level() > playerLevel + 4) {
            CreatureDef closest = null;
            for (CreatureDef candidate : Creatures.all()) {
                if (candidate.boss()) continue;
                if (candidate.level() > playerLevel + 4) continue;
                if (closest == null || candidate.level() > closest.level()) closest = candidate;
            }
            if (closest != null) def = closest;
        }
        return def;
    }

    private void spawnArenaCreature(Session session, CreatureDef def, int index, int total) {
        double radius = ArcanumConfig.ARENA_RADIUS.get();
        double angle = (Math.PI * 2 / Math.max(1, total)) * index + Math.PI / 2;
        double x = session.center.x + Math.cos(angle) * radius;
        double z = session.center.z + Math.sin(angle) * radius;
        EntityType<?> type = switch (def.school()) {
            case PYROMANCY, NECROMANCY -> EntityType.ZOMBIE;
            case TEMPEST, FROST -> EntityType.SKELETON;
            case RUNOMANCY -> EntityType.SPIDER;
            case SYLVAN -> EntityType.CAVE_SPIDER;
            case EQUILIBRIUM -> EntityType.PILLAGER;
        };
        net.minecraft.world.entity.Entity spawned = type.create(session.level);
        if (!(spawned instanceof LivingEntity living)) return;
        living.moveTo(x, session.center.y, z, session.level.getRandom().nextFloat() * 360f, 0f);
        living.setCustomName(Component.literal(def.name(true)));
        living.setCustomNameVisible(true);
        session.level.addFreshEntity(living);
        Combatant combatant = buildCreatureCombatant(living, def);
        session.battle.addCombatant(combatant);
        session.entities.put(combatant.id(), living);
        session.creatures.put(combatant.id(), def);
        prepareEntity(living, session, index);
    }

    /** Pont moteur -&gt; serveur (sons, effets, journal). */
    private static final class Host implements BattleHost {
        @Override
        public void onDefeated(Battle battle, Combatant combatant) {
            if (combatant.isPlayer()) return;
            ArcanumRealms.LOGGER.debug("[Arcanum Realms] {} est vaincu", combatant.name());
        }
    }
}
