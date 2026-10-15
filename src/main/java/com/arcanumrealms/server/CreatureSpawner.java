package com.arcanumrealms.server;

import com.arcanumrealms.core.world.Bestiary;
import com.arcanumrealms.core.world.WorldCreature;
import com.arcanumrealms.core.world.ZoneDef;
import com.arcanumrealms.core.world.Zones;
import com.arcanumrealms.world.ZoneMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Fait apparaitre les creatures du bestiaire dans le monde.
 *
 * <p>Chaque creature du bestiaire est liee a une entite Minecraft (zombie,
 * squelette, ...) : cela evite d'enregistrer trente entites supplementaires
 * tout en gardant les statistiques, les sorts et le butin de la fiche d'origine.
 * La creature marquee dans ses donnees persistantes est reconnue a sa mort par
 * {@link QuestService}, meme hors duel.
 */
public final class CreatureSpawner {

    /** Nombre de creatures posees en cercle autour du joueur. */
    public static final int DEFAULT_PACK_SIZE = 4;
    private static final double SPAWN_RADIUS = 8.0;
    private static final int ATTEMPTS = 12;

    private CreatureSpawner() {}

    /** Fait apparaitre un groupe de creatures de la zone du joueur. */
    public static int spawnAround(ServerPlayer player, String creatureId) {
        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();
        ZoneMap map = ZoneMap.get(level);
        String zoneId = map.zoneAt(level, origin)
                .orElseGet(() -> map.nearestZone(level, origin).orElse(null));

        List<WorldCreature> pool = new ArrayList<>();
        if (creatureId != null) {
            WorldCreature exact = Bestiary.find(creatureId).orElse(null);
            if (exact == null) return 0;
            pool.add(exact);
        } else if (zoneId != null) {
            pool.addAll(Bestiary.ofZone(zoneId));
        }
        if (pool.isEmpty()) return 0;

        ZoneDef zone = zoneId == null ? null : Zones.get(zoneId);
        if (zone != null && zone.minLevel() > player.experienceLevel + 40) return 0;

        var random = level.getRandom();
        int count = 0;
        for (int i = 0; i < DEFAULT_PACK_SIZE; i++) {
            WorldCreature creature = pool.get(random.nextInt(pool.size()));
            if (spawnOne(level, origin, creature) != null) count++;
        }
        return count;
    }

    /** Cree une creature a une position aleatoire proche, sur un sol valide. */
    public static LivingEntity spawnOne(ServerLevel level, BlockPos origin, WorldCreature creature) {
        EntityType<?> type = entityTypeOf(creature);
        if (type == null) return null;
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2;
            double distance = SPAWN_RADIUS * (0.5 + level.getRandom().nextDouble() * 0.5);
            BlockPos pos = origin.offset((int) Math.round(Math.cos(angle) * distance),
                    level.getRandom().nextInt(5) - 2,
                    (int) Math.round(Math.sin(angle) * distance));
            BlockPos ground = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
            if (level.getBlockState(ground.below()).isAir()) continue;
            var entity = type.create(level);
            if (!(entity instanceof LivingEntity living)) continue;
            if (living instanceof Mob mob) {
                mob.moveTo(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5,
                        level.getRandom().nextFloat() * 360f, 0f);
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(ground),
                        MobSpawnType.COMMAND, null, null);
            } else {
                living.moveTo(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5);
            }
            tag(living, creature);
            level.addFreshEntity(living);
            return living;
        }
        return null;
    }

    /** Marque la creature : identifiant de fiche, nom affiche et butin. */
    public static void tag(LivingEntity entity, WorldCreature creature) {
        entity.getPersistentData().putString("arcanum_realms:creature", creature.id());
        entity.setCustomName(Component.literal(creature.name(false) + (creature.boss() ? " (boss)" : "")));
        entity.setCustomNameVisible(creature.boss());
        entity.setPersistenceRequired();
        if (entity instanceof Mob mob && creature.boss()) {
            mob.setPersistenceRequired();
        }
    }

    /** Entite Minecraft utilisee pour une fiche du bestiaire. */
    public static EntityType<?> entityTypeOf(WorldCreature creature) {
        var id = net.minecraft.resources.ResourceLocation.tryParse(creature.entity());
        if (id == null) return null;
        return net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(id);
    }

    /** Enleve les creatures du mod autour d'une position (nettoyage des arenes). */
    public static int clearAround(ServerLevel level, BlockPos center, double radius) {
        int removed = 0;
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center).inflate(radius))) {
            if (level.dimension() != Level.OVERWORLD) continue;
            if (!living.getPersistentData().contains("arcanum_realms:creature")) continue;
            living.discard();
            removed++;
        }
        return removed;
    }
}
