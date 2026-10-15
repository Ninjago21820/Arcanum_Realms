package com.arcanumrealms.entity;

import com.arcanumrealms.core.world.NpcDef;
import com.arcanumrealms.core.world.Npcs;
import com.arcanumrealms.server.DialogueService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * PNJ du mod : un habitant immortel qui donne des quetes et ouvre un dialogue.
 *
 * <p>L'entite ne stocke qu'un identifiant de PNJ ; tout le reste (nom, ecole,
 * dialogues, quetes) vient du catalogue {@link Npcs}. Comme le catalogue est
 * partage entre le client et le serveur, l'apparence peut etre choisie cote
 * client a partir du meme identifiant.
 */
public class WizardNpcEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> DATA_NPC_ID =
            SynchedEntityData.defineId(WizardNpcEntity.class, EntityDataSerializers.STRING);

    /** Poste du PNJ : il se promene autour, sans jamais s'en eloigner. */
    private net.minecraft.core.BlockPos post;
    /** Compteur avant la prochaine salutation. */
    private int greeting = 40;

    public WizardNpcEntity(EntityType<? extends WizardNpcEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.45)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 12.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_NPC_ID, "");
    }

    /**
     * Comportement des PNJ : regarder le joueur qui approche, se retourner de
     * temps en temps, et faire quelques pas autour de son poste quand la
     * promenade est activee dans la configuration.
     *
     * <p>Le regard et la marche sont ceux des villageois : c'est ce qui evite
     * l'effet mannequin.
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f, 1.0f));
        this.goalSelector.addGoal(2, new WanderNearPostGoal(this, 0.45));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    /**
     * Promenade limitee a quelques blocs autour du poste du PNJ.
     *
     * <p>La destination est tiree au hasard dans un carre autour du poste, puis
     * posee sur le premier sol libre trouve dans la colonne : le PNJ ne part donc
     * jamais se promener dans un mur ou dans le vide.
     */
    private static class WanderNearPostGoal extends RandomStrollGoal {

        WanderNearPostGoal(PathfinderMob mob, double speed) {
            super(mob, speed, 120);
        }

        /** La promenade se lit dans la configuration a chaque tentative. */
        @Override
        public boolean canUse() {
            return com.arcanumrealms.ArcanumConfig.wanderEnabled() && super.canUse();
        }

        @Override
        protected Vec3 getPosition() {
            int radius = com.arcanumrealms.ArcanumConfig.wanderRadius();
            WizardNpcEntity npc = (WizardNpcEntity) this.mob;
            net.minecraft.core.BlockPos home = npc.postOrCurrent();
            for (int attempt = 0; attempt < 10; attempt++) {
                int dx = this.mob.getRandom().nextInt(2 * radius + 1) - radius;
                int dz = this.mob.getRandom().nextInt(2 * radius + 1) - radius;
                net.minecraft.core.BlockPos target = ground(home.offset(dx, 0, dz));
                if (target != null) return Vec3.atBottomCenterOf(target);
            }
            return null;
        }

        /** Cherche un sol libre autour de la colonne visee, quelques blocs plus haut ou plus bas. */
        private net.minecraft.core.BlockPos ground(net.minecraft.core.BlockPos around) {
            for (int dy = 2; dy >= -3; dy--) {
                net.minecraft.core.BlockPos feet = around.above(dy);
                if (!this.mob.level().getBlockState(feet.below()).isAir()
                        && this.mob.level().getBlockState(feet).isAir()
                        && this.mob.level().getBlockState(feet.above()).isAir()) {
                    return feet;
                }
            }
            return null;
        }
    }

    // ------------------------------------------------------------ donnees

    public String npcId() {
        return this.entityData.get(DATA_NPC_ID);
    }

    public void setNpcId(String npcId) {
        this.entityData.set(DATA_NPC_ID, npcId);
        if (this.post == null) setPost(this.blockPosition());
        Npcs.find(npcId).ifPresent(def -> {
            setCustomName(Component.translatable(npcLangKey(npcId)));
            setCustomNameVisible(true);
        });
    }

    public NpcDef def() {
        return Npcs.find(npcId()).orElse(null);
    }

    /** Position autour de laquelle le PNJ se promene. */
    public net.minecraft.core.BlockPos postOrCurrent() {
        return this.post == null ? this.blockPosition() : this.post;
    }

    public void setPost(net.minecraft.core.BlockPos pos) {
        this.post = pos.immutable();
    }

    /**
     * Un signe de vie toutes les quelques secondes : le PNJ salue d'un geste de
     * la main quand un joueur passe a portee. C'est le meme geste que celui du
     * joueur qui agite les bras, donc il s'anime naturellement.
     */
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (--this.greeting > 0) return;
        this.greeting = 100 + this.random.nextInt(140);
        Player nearby = this.level().getNearestPlayer(this, 6.0);
        if (nearby != null && !nearby.isSpectator() && this.hasLineOfSight(nearby)) {
            this.swing(InteractionHand.MAIN_HAND);
        }
    }

    /** Cle de traduction du nom du PNJ. */
    public static String npcLangKey(String npcId) {
        return "npc.arcanum_realms." + npcId;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("npc", npcId());
        if (this.post != null) {
            tag.putInt("postX", this.post.getX());
            tag.putInt("postY", this.post.getY());
            tag.putInt("postZ", this.post.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("npc")) setNpcId(tag.getString("npc"));
        if (tag.contains("postX")) {
            setPost(new net.minecraft.core.BlockPos(tag.getInt("postX"), tag.getInt("postY"),
                    tag.getInt("postZ")));
        }
    }

    // --------------------------------------------------------- interaction

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        NpcDef def = def();
        if (def == null) return InteractionResult.PASS;
        if (this.level().isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            DialogueService.open(serverPlayer, def.id());
        }
        return InteractionResult.CONSUME;
    }

    // ------------------------------------------------------------ regles

    /** Les PNJ ne disparaissent pas et ne peuvent pas etre blesses. */
    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    /** Les PNJ ne prennent pas de degats, mais reagissent aux coups par une replique. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof ServerPlayer player) {
            NpcDef def = def();
            if (def != null) {
                player.displayClientMessage(Component.translatable("arcanum_realms.npc.dont_hit",
                        Component.translatable(npcLangKey(def.id()))), false);
            }
        }
        return false;
    }
}
