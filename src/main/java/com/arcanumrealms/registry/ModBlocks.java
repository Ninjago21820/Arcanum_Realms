package com.arcanumrealms.registry;

import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.block.ArcaneCircleBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Blocs du mod. */
public final class ModBlocks {

    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ArcanumRealms.MOD_ID);

    /** Le cercle arcanique : la ou l'on provoque les duels d'arene. */
    public static final RegistryObject<net.minecraft.world.level.block.Block> ARCANE_CIRCLE = BLOCKS.register(
            "arcane_circle",
            () -> new ArcaneCircleBlock(BlockBehaviour.Properties.copy(Blocks.PURPUR_BLOCK)
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.AMETHYST_BLOCK)
                    .lightLevel(state -> 10)));

    private ModBlocks() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
