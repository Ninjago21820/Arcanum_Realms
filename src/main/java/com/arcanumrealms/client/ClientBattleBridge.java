package com.arcanumrealms.client;

import com.arcanumrealms.client.screen.DuelScreen;
import com.arcanumrealms.client.screen.SpellbookScreen;
import com.arcanumrealms.network.packet.BattleEndPacket;
import com.arcanumrealms.network.packet.BattleStatePacket;
import com.arcanumrealms.network.packet.WizardDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Reception des paquets cote client.
 *
 * <p>Cette classe n'est chargee que sur un client (elle n'est appelee que si
 * le paquet arrive sur le cote client), donc aucun risque sur un serveur dedie.
 */
public final class ClientBattleBridge {

    private static String currentBattleId;

    private ClientBattleBridge() {}

    public static void acceptState(BattleStatePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        boolean newBattle = currentBattleId == null || !currentBattleId.equals(packet.battleId);
        currentBattleId = packet.battleId;
        ClientBattleState.setBattle(packet);
        if (newBattle) ClientBattleState.clearSelection();

        if (mc.screen instanceof DuelScreen duel) {
            duel.setState(packet);
        } else if (newBattle) {
            mc.setScreen(new DuelScreen(packet));
        }
    }

    public static void acceptEnd(BattleEndPacket packet) {
        ClientBattleState.setLastResult(packet.draw ? "draw" : packet.playersWon ? "victory" : "defeat");
        ClientBattleState.setBattle(null);
        currentBattleId = null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            Component message = Component.translatable(packet.draw ? "arcanum_realms.message.draw"
                    : packet.playersWon ? "arcanum_realms.message.victory_short" : "arcanum_realms.message.defeat_short");
            mc.player.displayClientMessage(message, false);
        }
        if (mc.screen instanceof DuelScreen) mc.setScreen(null);
    }

    public static void acceptWizardData(WizardDataPacket packet) {
        ClientBattleState.setWizard(packet);
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof SpellbookScreen) {
            // l'ecran se met a jour tout seul
            return;
        }
        if (!ClientBattleState.inBattle()) {
            mc.setScreen(new SpellbookScreen(packet));
        }
    }
}
