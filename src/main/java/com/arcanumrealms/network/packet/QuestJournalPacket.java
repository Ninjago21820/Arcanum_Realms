package com.arcanumrealms.network.packet;

import com.arcanumrealms.client.ClientNpcDialogue;
import com.arcanumrealms.client.ClientQuestJournal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Journal de quetes envoye au client (commande {@code /arcanum quetes}). */
public record QuestJournalPacket(List<String> lines) {

    public static void encode(QuestJournalPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.lines.size());
        for (String line : packet.lines) buffer.writeUtf(line, 512);
    }

    public static QuestJournalPacket decode(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        List<String> lines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) lines.add(buffer.readUtf(512));
        return new QuestJournalPacket(lines);
    }

    public static void handle(QuestJournalPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientQuestJournal.open(packet)));
        ctx.setPacketHandled(true);
    }
}
