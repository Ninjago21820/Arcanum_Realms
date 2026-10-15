package com.arcanumrealms.client;

import com.arcanumrealms.client.screen.QuestJournalScreen;
import com.arcanumrealms.network.packet.QuestJournalPacket;
import net.minecraft.client.Minecraft;

/** Reception du journal de quetes (cote client uniquement). */
public final class ClientQuestJournal {

    private ClientQuestJournal() {}

    public static void open(QuestJournalPacket packet) {
        Minecraft.getInstance().setScreen(new QuestJournalScreen(packet));
    }
}
