package com.arcanumrealms.registry;

import com.arcanumrealms.ArcanumRealms;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Onglet creatif du mod. */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArcanumRealms.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.arcanum_realms.main"))
                    .icon(() -> new ItemStack(ModItems.DUEL_WAND.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.DUEL_WAND.get());
                        output.accept(ModItems.ARCANE_CIRCLE_ITEM.get());
                    })
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
