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

    public WizardNpcEntity(EntityType<? extends WizardNpcEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 12.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_NPC_ID, "");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    // ------------------------------------------------------------ donnees

    public String npcId() {
        return this.entityData.get(DATA_NPC_ID);
    }

    public void setNpcId(String npcId) {
        this.entityData.set(DATA_NPC_ID, npcId);
        Npcs.find(npcId).ifPresent(def -> {
            setCustomName(Component.translatable(npcLangKey(npcId)));
            setCustomNameVisible(true);
        });
    }

    public NpcDef def() {
        return Npcs.find(npcId()).orElse(null);
    }

    /** Cle de traduction du nom du PNJ. */
    public static String npcLangKey(String npcId) {
        return "npc.arcanum_realms." + npcId;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("npc", npcId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("npc")) setNpcId(tag.getString("npc"));
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
