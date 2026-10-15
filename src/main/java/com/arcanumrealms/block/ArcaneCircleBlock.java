package com.arcanumrealms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Cercle arcanique : la dalle sur laquelle on provoque les duels d'arene.
 * Le sort lance depend de la configuration ({@code arena.encounters}).
 */
public class ArcaneCircleBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);

    public ArcaneCircleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            player.displayClientMessage(Component.translatable("block.arcanum_realms.arcane_circle.hint"), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** Empeche les creatures de se generer sur le cercle. */
    @Override
    public boolean isPossibleToRespawnInThis(BlockState state) {
        return false;
    }
}
