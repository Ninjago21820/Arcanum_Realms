package com.arcanumrealms;

import com.arcanumrealms.battle.ServerBattleManager;
import com.arcanumrealms.core.ArcanumCore;
import com.arcanumrealms.network.NetworkHandler;
import com.arcanumrealms.registry.ModBlocks;
import com.arcanumrealms.registry.ModEntities;
import com.arcanumrealms.registry.ModCreativeTabs;
import com.arcanumrealms.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

/**
 * Entree du mod Arcanum Realms : un systeme de duel par cartes a pips,
 * inspire de Wizard101, prevu pour jouer entre amis.
 */
@Mod(ArcanumRealms.MOD_ID)
public class ArcanumRealms {

    public static final String MOD_ID = ArcanumCore.MOD_ID;
    public static final Logger LOGGER = LogUtils.getLogger();

    public ArcanumRealms() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModEntities.register(modBus);
        ModCreativeTabs.register(modBus);

        modBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ArcanumConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(ServerBattleManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new com.arcanumrealms.command.ArcanumCommand());
        MinecraftForge.EVENT_BUS.register(new com.arcanumrealms.server.WorldEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.register();
            List<String> problems = ArcanumCore.init();
            if (problems.isEmpty()) {
                LOGGER.info("[Arcanum Realms] {} sorts charges, {} creatures disponibles.",
                        com.arcanumrealms.core.spell.SpellBook.size(),
                        com.arcanumrealms.core.minion.Creatures.all().size());
            } else {
                problems.forEach(p -> LOGGER.warn("[Arcanum Realms] Donnee invalide : {}", p));
            }
        });
    }
}
