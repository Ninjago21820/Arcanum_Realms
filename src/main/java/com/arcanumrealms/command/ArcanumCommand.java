package com.arcanumrealms.command;

import com.arcanumrealms.ArcanumConfig;
import com.arcanumrealms.battle.ServerBattleManager;
import com.arcanumrealms.core.minion.Creatures;
import com.arcanumrealms.core.world.Bestiary;
import com.arcanumrealms.core.world.GearDef;
import com.arcanumrealms.core.world.GearSlot;
import com.arcanumrealms.core.world.Gears;
import com.arcanumrealms.core.world.NpcDef;
import com.arcanumrealms.core.world.Npcs;
import com.arcanumrealms.core.world.QuestDef;
import com.arcanumrealms.core.world.QuestText;
import com.arcanumrealms.core.world.Quests;
import com.arcanumrealms.core.world.WorldCreature;
import com.arcanumrealms.core.world.ZoneDef;
import com.arcanumrealms.core.world.Zones;
import com.arcanumrealms.entity.WizardNpcEntity;
import com.arcanumrealms.item.GearItem;
import com.arcanumrealms.network.packet.QuestActionPacket;
import com.arcanumrealms.server.QuestService;
import com.arcanumrealms.util.Language;
import com.arcanumrealms.world.ZoneMap;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.network.NetworkHandler;
import com.arcanumrealms.player.WizardData;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Commandes du mod : {@code /arcanum ...}
 *
 * <p>Toutes les commandes sont accessibles a n'importe quel joueur (usage entre amis) :
 * c'est ce qui permet de tester et de regler les duels facilement.
 */
public class ArcanumCommand {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("arcanum")
                .executes(ctx -> help(ctx.getSource()));

        root.then(Commands.literal("help").executes(ctx -> help(ctx.getSource())));
        root.then(Commands.literal("info").executes(ctx -> info(ctx.getSource())));

        // --- duels ---
        root.then(Commands.literal("duel")
                .executes(ctx -> duel(ctx.getSource())));
        root.then(Commands.literal("arena")
                .executes(ctx -> arena(ctx.getSource())));
        root.then(Commands.literal("resolve")
                .executes(ctx -> resolve(ctx.getSource())));
        root.then(Commands.literal("leave")
                .executes(ctx -> leave(ctx.getSource())));
        root.then(Commands.literal("noauto")
                .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ctx -> noAuto(ctx.getSource(), BoolArgumentType.getBool(ctx, "value")))));

        // --- sorcier ---
        root.then(Commands.literal("school")
                .then(Commands.argument("ecole", StringArgumentType.word())
                        .executes(ctx -> setSchool(ctx.getSource(), StringArgumentType.getString(ctx, "ecole")))));
        root.then(Commands.literal("level")
                .then(Commands.argument("niveau", IntegerArgumentType.integer(1, 100))
                        .executes(ctx -> setLevel(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "niveau")))));
        root.then(Commands.literal("powerpips")
                .then(Commands.argument("pourcent", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> setPowerPips(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "pourcent")))));
        root.then(Commands.literal("crit")
                .then(Commands.argument("score", IntegerArgumentType.integer(0, 10000))
                        .executes(ctx -> setCrit(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "score")))));
        root.then(Commands.literal("grimoire")
                .executes(ctx -> grimoire(ctx.getSource())));
        root.then(Commands.literal("deck")
                .then(Commands.literal("reset").executes(ctx -> resetDeck(ctx.getSource())))
                .then(Commands.literal("info").executes(ctx -> deckInfo(ctx.getSource()))));
        root.then(Commands.literal("spells")
                .executes(ctx -> listSpells(ctx.getSource(), null))
                .then(Commands.argument("ecole", StringArgumentType.word())
                        .executes(ctx -> listSpells(ctx.getSource(), StringArgumentType.getString(ctx, "ecole")))));
        root.then(Commands.literal("creatures")
                .executes(ctx -> listCreatures(ctx.getSource()))
                .then(Commands.literal("sauvages")
                        .executes(ctx -> spawnCreature(ctx.getSource(), null))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> spawnCreature(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "id"))))));

        // ---- monde : zones, PNJ, quetes, equipement
        root.then(Commands.literal("zones")
                .executes(ctx -> listZones(ctx.getSource()))
                .then(Commands.literal("set")
                        .then(Commands.argument("zone", StringArgumentType.greedyString())
                                .executes(ctx -> setZone(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "zone"), ZoneMap.DEFAULT_RADIUS))
                                .then(Commands.argument("rayon", IntegerArgumentType.integer(16, 512))
                                        .executes(ctx -> setZone(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "zone"),
                                                IntegerArgumentType.getInteger(ctx, "rayon"))))))
                .then(Commands.literal("del")
                        .then(Commands.argument("zone", StringArgumentType.greedyString())
                                .executes(ctx -> delZone(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "zone"))))));

        root.then(Commands.literal("pnj")
                .executes(ctx -> listNpcs(ctx.getSource()))
                .then(Commands.argument("id", StringArgumentType.word())
                        .executes(ctx -> spawnNpc(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id"))))
                .then(Commands.literal("ici")
                        .executes(ctx -> spawnZoneNpcs(ctx.getSource()))));

        root.then(Commands.literal("quetes")
                .executes(ctx -> journal(ctx.getSource()))
                .then(Commands.argument("id", StringArgumentType.word())
                        .executes(ctx -> questInfo(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id")))));

        root.then(Commands.literal("quete")
                .then(Commands.literal("accepter")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> questAction(ctx.getSource(), QuestActionPacket.ACCEPT,
                                        StringArgumentType.getString(ctx, "id")))))
                .then(Commands.literal("abandonner")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> questAction(ctx.getSource(), QuestActionPacket.ABANDON,
                                        StringArgumentType.getString(ctx, "id")))))
                .then(Commands.literal("remettre")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> questAction(ctx.getSource(), QuestActionPacket.HAND_IN,
                                        StringArgumentType.getString(ctx, "id"))))));

        root.then(Commands.literal("or")
                .then(Commands.argument("montant", IntegerArgumentType.integer(-100000, 100000))
                        .executes(ctx -> gold(ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "montant")))));

        root.then(Commands.literal("equipement")
                .executes(ctx -> listGear(ctx.getSource()))
                .then(Commands.argument("id", StringArgumentType.word())
                        .executes(ctx -> giveGear(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id"))))
                .then(Commands.literal("palier")
                        .then(Commands.argument("palier", IntegerArgumentType.integer(0, 4))
                                .executes(ctx -> giveTier(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "palier")))))
                .then(Commands.literal("retirer")
                        .then(Commands.argument("emplacement", StringArgumentType.word())
                                .executes(ctx -> unequip(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "emplacement"))))));

        event.getDispatcher().register(root);
    }


    // ------------------------------------------------------------ monde

    private static int listZones(CommandSourceStack source) {
        boolean french = Language.isFrench();
        ServerPlayer sp = player(source);
        ZoneMap map = sp == null ? null : ZoneMap.get(sp.serverLevel());
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.zones.header",
                Zones.all().size()), false);
        for (ZoneDef zone : Zones.all()) {
            boolean placed = map != null && map.anchor(zone.id()).isPresent();
            String mark = placed ? "[x]" : "[ ]";
            String unlock = zone.unlockQuest() == null ? "-"
                    : Quests.find(zone.unlockQuest()).map(q -> q.name(french)).orElse(zone.unlockQuest());
            source.sendSuccess(() -> Component.literal(String.format("%s %s (%s) nv%s - ouvre par : %s",
                    mark, zone.id(), zone.name(french), zone.levelRange(), unlock)), false);
        }
        if (map != null && !map.missing().isEmpty()) {
            source.sendSuccess(() -> Component.translatable("arcanum_realms.command.zones.missing",
                    String.join(", ", map.missing())), false);
        }
        return 1;
    }

    private static int setZone(CommandSourceStack source, String name, int radius) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        ZoneDef zone = ZoneMap.resolve(name).orElse(null);
        if (zone == null) {
            source.sendFailure(Component.translatable("arcanum_realms.command.zone_unknown", name));
            return 0;
        }
        ZoneMap map = ZoneMap.get(sp.serverLevel());
        map.set(zone.id(), sp.blockPosition(), radius);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.zone_set",
                zone.name(Language.isFrench()), zone.id(), radius), true);
        return 1;
    }

    private static int delZone(CommandSourceStack source, String name) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        ZoneDef zone = ZoneMap.resolve(name).orElse(null);
        if (zone == null) {
            source.sendFailure(Component.translatable("arcanum_realms.command.zone_unknown", name));
            return 0;
        }
        boolean removed = ZoneMap.get(sp.serverLevel()).clear(zone.id());
        if (!removed) {
            source.sendFailure(Component.translatable("arcanum_realms.command.zone_absent", zone.id()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.zone_deleted", zone.id()), true);
        return 1;
    }

    private static int listNpcs(CommandSourceStack source) {
        boolean french = Language.isFrench();
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.npcs.header",
                Npcs.all().size()), false);
        for (NpcDef npc : Npcs.all()) {
            ZoneDef zone = Zones.find(npc.zoneId()).orElse(null);
            source.sendSuccess(() -> Component.literal(String.format("%s - %s (%s) %s",
                    npc.id(), npc.name(french),
                    zone == null ? npc.zoneId() : zone.name(french),
                    npc.givesQuests().isEmpty() ? "" : "-> " + String.join(", ", npc.givesQuests()))), false);
        }
        return 1;
    }

    private static int spawnNpc(CommandSourceStack source, String npcId) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        NpcDef def = Npcs.find(npcId).orElse(null);
        if (def == null) {
            source.sendFailure(Component.translatable("arcanum_realms.command.npc_unknown", npcId));
            return 0;
        }
        if (spawn(sp, def, sp.blockPosition()) == null) {
            source.sendFailure(Component.translatable("arcanum_realms.command.npc_failed"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.npc_spawned",
                def.name(Language.isFrench())), true);
        return 1;
    }

    /** Fait apparaitre tous les PNJ de la zone ou se trouve le joueur, en cercle. */
    private static int spawnZoneNpcs(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        ZoneMap map = ZoneMap.get(sp.serverLevel());
        String zoneId = map.zoneAt(sp.serverLevel(), sp.blockPosition())
                .orElseGet(() -> map.nearestZone(sp.serverLevel(), sp.blockPosition()).orElse(null));
        if (zoneId == null) {
            source.sendFailure(Component.translatable("arcanum_realms.command.no_zone"));
            return 0;
        }
        ZoneDef zone = Zones.get(zoneId);
        List<NpcDef> npcs = Npcs.ofZone(zoneId);
        if (npcs.isEmpty()) {
            source.sendFailure(Component.translatable("arcanum_realms.command.zone_no_npc",
                    zone.name(Language.isFrench())));
            return 0;
        }
        int index = 0;
        int spawned = 0;
        for (NpcDef def : npcs) {
            double angle = (Math.PI * 2 / npcs.size()) * index++;
            int x = sp.blockPosition().getX() + (int) Math.round(Math.cos(angle) * 4);
            int z = sp.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * 4);
            if (spawn(sp, def, new net.minecraft.core.BlockPos(x, sp.blockPosition().getY(), z)) != null) spawned++;
        }
        int total = spawned;
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.npcs_placed",
                total, zone.name(Language.isFrench())), true);
        return 1;
    }

    /** Cree l'entite d'un PNJ a une position donnee. */
    private static WizardNpcEntity spawn(ServerPlayer player, NpcDef def, net.minecraft.core.BlockPos pos) {
        net.minecraft.server.level.ServerLevel level = player.serverLevel();
        WizardNpcEntity npc = com.arcanumrealms.registry.ModEntities.WIZARD_NPC.get().create(level);
        if (npc == null) return null;
        npc.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot() + 180f, 0f);
        npc.setNpcId(def.id());
        // Les PNJ deja presents sont remplaces pour eviter les doublons.
        for (WizardNpcEntity existing : level.getEntitiesOfClass(WizardNpcEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(2.5))) {
            if (def.id().equals(existing.npcId())) existing.discard();
        }
        level.addFreshEntity(npc);
        return npc;
    }

    private static int spawnCreature(CommandSourceStack source, String creatureId) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        int spawned = com.arcanumrealms.server.CreatureSpawner.spawnAround(sp, creatureId);
        int total = spawned;
        if (total == 0) {
            source.sendFailure(Component.translatable("arcanum_realms.command.no_creature"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.creatures_spawned", total), true);
        return 1;
    }

    // ----------------------------------------------------------- quetes

    private static int journal(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        List<com.arcanumrealms.core.world.ContentLine> lines = QuestService.journal(sp);
        NetworkHandler.sendQuestJournal(sp, lines);
        // Les lignes portent leurs cles : elles s'affichent dans la langue du joueur,
        // et un pack de ressources peut les reecrire.
        lines.forEach(line -> source.sendSuccess(() -> QuestService.lineComponent(line), false));
        return 1;
    }

    private static int questInfo(CommandSourceStack source, String questId) {
        boolean french = Language.isFrench();
        QuestDef quest = Quests.find(questId).orElse(null);
        if (quest == null) {
            source.sendFailure(Component.translatable("arcanum_realms.command.quest_unknown", questId));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(quest.name(french) + " (" + quest.id() + ")"), false);
        source.sendSuccess(() -> Component.literal("  " + quest.summary(french)), false);
        source.sendSuccess(() -> Component.literal("  " + (french ? "niveau" : "level") + " "
                + quest.levelRequired() + " - " + (quest.side() ? "annexe" : "principale")), false);
        source.sendSuccess(() -> Component.literal("  " + (french ? "donneur" : "giver") + " : "
                + Npcs.find(quest.giverNpc()).map(n -> n.name(french)).orElse(quest.giverNpc())), false);
        for (String goal : QuestText.goals(quest, french)) {
            source.sendSuccess(() -> Component.literal("  - " + goal), false);
        }
        source.sendSuccess(() -> Component.literal("  " + (french ? "recompense" : "rewards") + " : "
                + QuestText.rewards(quest, french)), false);
        source.sendSuccess(() -> Component.literal("  " + (french ? "suite" : "leads to") + " : "
                + (quest.leadsTo() == null ? "-" : quest.leadsTo())), false);
        return 1;
    }

    private static int questAction(CommandSourceStack source, int action, String questId) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        QuestService.handleAction(sp, action, questId);
        return 1;
    }

    private static int gold(CommandSourceStack source, int amount) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        WizardData data = WizardData.load(sp);
        data.addGold(amount);
        data.save(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.gold", data.gold()), false);
        return 1;
    }

    // ------------------------------------------------------- equipement

    private static int listGear(CommandSourceStack source) {
        boolean french = Language.isFrench();
        ServerPlayer sp = player(source);
        WizardData data = sp == null ? null : WizardData.load(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.gear.header",
                Gears.all().size()), false);
        for (GearDef gear : Gears.all()) {
            boolean worn = data != null && gear.id().equals(data.equippedId(gear.slot()));
            source.sendSuccess(() -> Component.literal(String.format("%s %s (%s, nv%d) %s",
                    worn ? "[x]" : "[ ]", gear.id(), gear.slot().name().toLowerCase(), gear.level(),
                    gear.stats(french))), false);
        }
        if (data != null) {
            source.sendSuccess(() -> Component.translatable("arcanum_realms.command.gear.gold", data.gold()), false);
        }
        return 1;
    }

    private static int giveGear(CommandSourceStack source, String gearId) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        GearDef gear = Gears.find(gearId).orElse(null);
        if (gear == null) {
            source.sendFailure(Component.translatable("arcanum_realms.command.gear_unknown", gearId));
            return 0;
        }
        sp.getInventory().placeItemBackInInventory(GearItem.create(gear, 1));
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.gear_given",
                gear.name(Language.isFrench())), false);
        return 1;
    }

    private static int giveTier(CommandSourceStack source, int tier) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        for (GearSlot slot : GearSlot.values()) {
            GearDef gear = Gears.get(Gears.reward(tier, slot));
            sp.getInventory().placeItemBackInInventory(GearItem.create(gear, 1));
        }
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.gear_tier", tier), false);
        return 1;
    }

    private static int unequip(CommandSourceStack source, String slotName) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        GearSlot slot = null;
        for (GearSlot candidate : GearSlot.values()) {
            if (candidate.name().equalsIgnoreCase(slotName) || candidate.slotId() == parseSlot(slotName)) {
                slot = candidate;
                break;
            }
        }
        if (slot == null) {
            source.sendFailure(Component.translatable("arcanum_realms.command.slot_unknown", slotName));
            return 0;
        }
        WizardData data = WizardData.load(sp);
        String removed = data.unequip(slot);
        data.save(sp);
        if (removed == null) {
            source.sendFailure(Component.translatable("arcanum_realms.command.slot_empty"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.gear_removed", removed), false);
        return 1;
    }

    private static int parseSlot(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static ServerPlayer player(CommandSourceStack source) {
        return source.getPlayer();
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.help_header"), false);
        for (String key : List.of("duel", "arena", "resolve", "leave", "noauto", "school", "level",
                "powerpips", "crit", "grimoire", "deck", "spells", "creatures", "zones", "pnj",
                "quetes", "quete", "or", "equipement", "info")) {
            source.sendSuccess(() -> Component.translatable("arcanum_realms.command.help." + key), false);
        }
        return 1;
    }

    private static int info(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("[Arcanum Realms] " + SpellBook.size() + " sorts, "
                + Creatures.all().size() + " creatures, "
                + ServerBattleManager.INSTANCE.sessions().size() + " duel(s) en cours."), false);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.info.help"), false);
        return 1;
    }

    private static int duel(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        ServerBattleManager.INSTANCE.startDuelWithNearby(sp, null);
        return 1;
    }

    private static int arena(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        ServerBattleManager.INSTANCE.startArenaDuel(sp, sp.blockPosition());
        return 1;
    }

    private static int resolve(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        ServerBattleManager.Session session = ServerBattleManager.INSTANCE.sessionOf(sp);
        if (session == null) {
            source.sendFailure(Component.translatable("arcanum_realms.message.no_duel"));
            return 0;
        }
        ServerBattleManager.INSTANCE.forceResolve(session);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.resolve.done"), false);
        return 1;
    }

    private static int leave(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        ServerBattleManager.Session session = ServerBattleManager.INSTANCE.sessionOf(sp);
        if (session == null) {
            source.sendFailure(Component.translatable("arcanum_realms.message.no_duel"));
            return 0;
        }
        ServerBattleManager.INSTANCE.leaveDuel(session, sp.getUUID(), true);
        return 1;
    }

    private static int noAuto(CommandSourceStack source, boolean value) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        sp.getPersistentData().putBoolean("arcanum_realms:no_duel", value);
        source.sendSuccess(() -> Component.translatable(value
                ? "arcanum_realms.command.noauto.on" : "arcanum_realms.command.noauto.off"), false);
        return 1;
    }

    private static int setSchool(CommandSourceStack source, String schoolId) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        School school;
        try {
            school = School.byId(schoolId);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.translatable("arcanum_realms.command.school.unknown", schoolId));
            return 0;
        }
        WizardData data = WizardData.load(sp);
        data.setSchool(school);
        data.clearDeck();
        List<Spell> learned = data.refreshLearnedSpells();
        data.ensureStarterDeck();
        data.save(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.school.done",
                Component.translatable(school.langKey()), learned.size()), false);
        return 1;
    }

    private static int setLevel(CommandSourceStack source, int level) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        WizardData data = WizardData.load(sp);
        data.setLevel(level);
        List<Spell> learned = data.refreshLearnedSpells();
        data.save(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.level.done", level, learned.size()), false);
        return 1;
    }

    private static int setPowerPips(CommandSourceStack source, int percent) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        WizardData data = WizardData.load(sp);
        data.setPowerPipChance(percent);
        data.save(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.powerpips.done", percent), false);
        return 1;
    }

    private static int setCrit(CommandSourceStack source, int crit) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        WizardData data = WizardData.load(sp);
        data.setCritRating(crit);
        data.save(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.crit.done", crit), false);
        return 1;
    }

    private static int grimoire(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        NetworkHandler.sendWizardData(sp);
        return 1;
    }

    private static int resetDeck(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        WizardData data = WizardData.load(sp);
        data.clearDeck();
        data.ensureStarterDeck();
        data.save(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.deck.reset", data.deckSize()), false);
        return 1;
    }

    private static int deckInfo(CommandSourceStack source) {
        ServerPlayer sp = player(source);
        if (sp == null) return 0;
        WizardData data = WizardData.load(sp);
        source.sendSuccess(() -> Component.translatable("arcanum_realms.command.deck.info",
                data.deckSize(), WizardData.deckLimit(), data.learnedIds().size()), false);
        data.deck().forEach((id, count) -> SpellBook.find(id).ifPresent(spell ->
                source.sendSuccess(() -> Component.literal(" - " + spell.nameFr() + " x" + count), false)));
        return 1;
    }

    private static int listSpells(CommandSourceStack source, String schoolId) {
        School school = null;
        if (schoolId != null) {
            try {
                school = School.byId(schoolId);
            } catch (IllegalArgumentException e) {
                source.sendFailure(Component.translatable("arcanum_realms.command.school.unknown", schoolId));
                return 0;
            }
        }
        School filter = school;
        List<Spell> spells = filter == null ? List.copyOf(SpellBook.all()) : SpellBook.bySchool(filter);
        source.sendSuccess(() -> Component.literal("[Arcanum Realms] " + spells.size() + " sorts"), false);
        for (Spell spell : spells) {
            String cost = spell.variablePips() ? ("X+" + spell.minPips()) : String.valueOf(spell.pipCost());
            source.sendSuccess(() -> Component.literal(" - " + spell.nameFr() + " (" + cost + " pips, "
                    + spell.accuracy() + "%, niv. " + spell.minLevel() + ")"), false);
        }
        return 1;
    }

    private static int listCreatures(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("[Arcanum Realms] " + Creatures.all().size() + " creatures"), false);
        Creatures.all().forEach(def -> source.sendSuccess(() -> Component.literal(" - " + def.id()
                + " : " + def.nameFr() + " (niv. " + def.level() + ", " + def.health() + " PV, "
                + def.school().displayFr() + ")"), false));
        return 1;
    }

    /** Utilitaire : borne une valeur dans la configuration. */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static {
        // Rappel des bornes de configuration par defaut, utile en debug.
        assert ArcanumConfig.MAX_ROUNDS.get() > 0;
    }
}
