package com.arcanumrealms.client;

import com.arcanumrealms.ArcanumRealms;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Touches du mod, cote client. */
@Mod.EventBusSubscriber(modid = ArcanumRealms.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    public static final String CATEGORY = "key.categories.arcanum_realms";

    /** Rouvre l'interface de duel (touche R par defaut). */
    public static final KeyMapping OPEN_DUEL = new KeyMapping(
            "key.arcanum_realms.open_duel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);

    /** Ouvre le grimoire (touche G par defaut). */
    public static final KeyMapping OPEN_GRIMOIRE = new KeyMapping(
            "key.arcanum_realms.open_grimoire", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);

    private ClientSetup() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_DUEL);
        event.register(OPEN_GRIMOIRE);
    }
}
