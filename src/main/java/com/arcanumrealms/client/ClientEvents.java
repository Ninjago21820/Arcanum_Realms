package com.arcanumrealms.client;

import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.client.hud.DuelHudOverlay;
import com.arcanumrealms.client.screen.DuelScreen;
import com.arcanumrealms.network.NetworkHandler;
import com.arcanumrealms.network.packet.RequestWizardDataPacket;
import com.arcanumrealms.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Evenements client : touches, baguette, affichage de l'interface. */
@Mod.EventBusSubscriber(modid = ArcanumRealms.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (ClientSetup.OPEN_DUEL.consumeClick()) {
            if (ClientBattleState.inBattle()) {
                if (!(mc.screen instanceof DuelScreen)) mc.setScreen(new DuelScreen(ClientBattleState.battle()));
            }
        }
        while (ClientSetup.OPEN_GRIMOIRE.consumeClick()) {
            NetworkHandler.sendToServer(new RequestWizardDataPacket());
        }
    }

    /** Clic droit avec la baguette : ouvre le grimoire (hors duel). */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof LocalPlayer)) return;
        if (!event.getItemStack().is(ModItems.DUEL_WAND.get())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        if (ClientBattleState.inBattle()) {
            mc.setScreen(new DuelScreen(ClientBattleState.battle()));
        } else {
            NetworkHandler.sendToServer(new RequestWizardDataPacket());
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        DuelHudOverlay.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientBattleState.reset();
    }
}
