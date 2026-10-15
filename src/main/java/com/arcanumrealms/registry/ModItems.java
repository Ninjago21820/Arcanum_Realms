package com.arcanumrealms.registry;

import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.item.DuelWandItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Objets du mod. */
public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ArcanumRealms.MOD_ID);

    /** La baguette sert a provoquer les duels et a ouvrir le grimoire. */
    public static final RegistryObject<Item> DUEL_WAND = ITEMS.register("duel_wand",
            () -> new DuelWandItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> ARCANE_CIRCLE_ITEM = ITEMS.register("arcane_circle",
            () -> new BlockItem(ModBlocks.ARCANE_CIRCLE.get(), new Item.Properties()));

    private ModItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
