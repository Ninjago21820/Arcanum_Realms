package com.arcanumrealms.item;

import com.arcanumrealms.battle.ServerBattleManager;
import com.arcanumrealms.registry.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * La baguette de duel.
 *
 * <ul>
 *   <li>Clic droit sur une creature : provoque un duel avec les creatures proches.</li>
 *   <li>Clic droit sur un cercle arcanique : lance la rencontre de l'arene.</li>
 *   <li>Clic droit dans le vide : ouvre le grimoire (cote client, voir ClientEvents).</li>
 * </ul>
 */
public class DuelWandItem extends Item {

    public DuelWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Le grimoire est ouvert par le client ; ici on se contente d'un retour visuel.
            serverPlayer.displayClientMessage(Component.translatable("item.arcanum_realms.duel_wand.hint"), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return level.getBlockState(context.getBlockPos()).is(ModBlocks.ARCANE_CIRCLE.get())
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;
        if (!level.getBlockState(context.getBlockPos()).is(ModBlocks.ARCANE_CIRCLE.get())) return InteractionResult.PASS;
        ServerBattleManager.INSTANCE.startArenaDuel(player, context.getBlockPos());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        ServerBattleManager.INSTANCE.startDuelWithNearby(serverPlayer, target);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.arcanum_realms.duel_wand.tooltip1"));
        tooltip.add(Component.translatable("item.arcanum_realms.duel_wand.tooltip2"));
        tooltip.add(Component.translatable("item.arcanum_realms.duel_wand.tooltip3"));
    }
}
