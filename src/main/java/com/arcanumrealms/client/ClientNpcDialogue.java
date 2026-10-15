package com.arcanumrealms.client;

import com.arcanumrealms.client.screen.NpcDialogueScreen;
import com.arcanumrealms.network.packet.NpcDialoguePacket;
import net.minecraft.client.Minecraft;

/** Reception du dialogue d'un PNJ (cote client uniquement). */
public final class ClientNpcDialogue {

    private ClientNpcDialogue() {}

    public static void open(NpcDialoguePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (packet.lines().isEmpty()) {
            mc.setScreen(null);
            return;
        }
        mc.setScreen(new NpcDialogueScreen(packet));
    }
}
