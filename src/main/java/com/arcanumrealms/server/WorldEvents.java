package com.arcanumrealms.server;

import com.arcanumrealms.ArcanumConfig;
import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.core.world.Bestiary;
import com.arcanumrealms.core.world.Npcs;
import com.arcanumrealms.core.world.WorldCreature;
import com.arcanumrealms.core.world.ZoneDef;
import com.arcanumrealms.core.world.Zones;
import com.arcanumrealms.network.NetworkHandler;
import com.arcanumrealms.player.WizardData;
import com.arcanumrealms.util.Language;
import com.arcanumrealms.world.ZoneMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Vie du monde : entree dans une zone, ramassage d'objets, creatures abattues.
 *
 * <p>C'est le lien entre le jeu (Minecraft) et les quetes : chaque evenement
 * interessant est traduit en progression de quete par {@link QuestService}.
 */
public class WorldEvents {

    private static final String ZONE_KEY = "arcanum_realms:zone";

    /** Distance maximale a laquelle les allies d'un combattant valident leur quete. */
    private static final double SHARE_RADIUS = 40.0;

    private int tickCounter;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (++tickCounter % 20 != 0) return;
        if (player.level().isClientSide) return;

        ServerLevel level = player.serverLevel();
        ZoneMap map = ZoneMap.get(level);
        BlockPos pos = player.blockPosition();
        String current = map.zoneAt(level, pos).orElse(null);

        var data = player.getPersistentData();
        String previous = data.contains(ZONE_KEY) ? data.getString(ZONE_KEY) : null;
        if (java.util.Objects.equals(previous, current)) return;

        if (current == null) {
            data.remove(ZONE_KEY);
            return;
        }
        data.putString(ZONE_KEY, current);

        ZoneDef zone = Zones.find(current).orElse(null);
        if (zone == null) return;

        boolean french = Language.isFrench();
        WizardData wizard = WizardData.load(player);
        boolean unlocked = wizard.level() >= zone.unlockLevel();

        if (unlocked) {
            player.displayClientMessage(
                    Component.literal(zone.name(french) + " - " + zone.levelRange())
                            .withStyle(ChatFormatting.GOLD), true);
            player.displayClientMessage(Component.literal(zone.description(french))
                    .withStyle(ChatFormatting.GRAY), false);
            QuestService.onZoneEntered(player, current);
            if (zone.dungeon()) {
                player.displayClientMessage(Component.translatable("arcanum_realms.zone.dungeon")
                        .withStyle(ChatFormatting.DARK_PURPLE), false);
            }
        } else {
            player.displayClientMessage(Component.translatable("arcanum_realms.zone.locked",
                    zone.name(french), zone.unlockLevel()).withStyle(ChatFormatting.RED), false);
        }
    }

    @SubscribeEvent
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;
        String itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        QuestService.onItemCollected(player, itemId, stack.getCount());
    }

    /**
     * Creature abattue.
     *
     * <p>Les duels gerent leurs propres recompenses : ici on ne traite que les
     * creatures tuees au corps a corps, pour que les quetes avancent aussi hors
     * duel. Tous les joueurs proches qui ont la quete en profitent.
     */
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide) return;
        if (dead instanceof ServerPlayer) return;
        if (ServerBattleManager.INSTANCE.isDuelCreature(dead)) return;

        String entityKey = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                .getKey(dead.getType()).toString();
        if (entityKey == null) return;

        if (dead.getPersistentData().contains("arcanum_realms:creature")) {
            entityKey = dead.getPersistentData().getString("arcanum_realms:creature");
        }

        if (!(dead.level() instanceof ServerLevel level)) return;
        WorldCreature creature = resolveCreature(entityKey, level, dead.blockPosition());
        if (creature == null) return;

        List<ServerPlayer> nearby = level.getEntitiesOfClass(ServerPlayer.class,
                dead.getBoundingBox().inflate(SHARE_RADIUS));
        for (ServerPlayer player : nearby) {
            QuestService.onCreatureDefeated(player, creature.id(), creature.classification());
        }
    }

    /** Retrouve la fiche de creature : par identifiant, sinon par zone. */
    static WorldCreature resolveCreature(String entityKey, ServerLevel level, BlockPos pos) {
        var byId = Bestiary.find(entityKey);
        if (byId.isPresent()) return byId.get();
        String zoneId = ZoneMap.get(level).nearestZone(level, pos).orElse(null);
        if (zoneId == null) return null;
        return Bestiary.forEntity(entityKey, zoneId).orElse(null);
    }

    /** Un joueur parle a un PNJ : l'objectif "parler a" avance. */
    @SubscribeEvent
    public void onEntityInteract(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof com.arcanumrealms.entity.WizardNpcEntity npc)) return;
        String npcId = npc.npcId();
        if (npcId == null || npcId.isEmpty()) return;
        QuestService.onTalked(player, npcId);
    }

    /** A la connexion : on envoie les donnees du sorcier et on annonce la zone. */
    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        NetworkHandler.sendWizardData(player);
        ServerLevel level = player.serverLevel();
        ZoneMap.get(level).zoneAt(level, player.blockPosition()).ifPresent(zoneId ->
                Zones.find(zoneId).ifPresent(zone -> player.displayClientMessage(
                        Component.literal(zone.name(Language.isFrench())).withStyle(ChatFormatting.GOLD), true)));
        player.getPersistentData().remove(ZONE_KEY);
    }

    /**
     * La mort ne doit pas effacer la progression.
     *
     * <p>Les donnees de sorcier vivent dans l'entite joueur (donnees persistantes
     * Forge) : il faut donc explicitement les recopier sur la nouvelle entite.
     */
    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (event.getEntity().level().isClientSide) return;
        net.minecraft.nbt.CompoundTag old = event.getOriginal().getPersistentData().copy();
        if (old.contains(WizardData.NBT_ROOT)) {
            event.getEntity().getPersistentData().put(WizardData.NBT_ROOT, old.getCompound(WizardData.NBT_ROOT));
        }
    }

    /** Aide : la fiche d'un PNJ porte-t-elle ce nom ? */
    static boolean isNpc(String npcId) {
        return Npcs.find(npcId).isPresent();
    }

    static String entityKey(Entity entity) {
        return net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
    }

    /** Rayon de partage de la progression, en blocs (configuration). */
    public static double shareRadius() {
        return Math.max(16.0, ArcanumConfig.TRIGGER_RADIUS.get() * 3.0);
    }
}
