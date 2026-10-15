package com.arcanumrealms.network.packet;

import com.arcanumrealms.client.ClientQuestJournal;
import com.arcanumrealms.core.world.ContentLine;
import com.arcanumrealms.network.ContentCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/** Journal de quetes envoye au client (commande {@code /arcanum quetes}). */
public record QuestJournalPacket(List<ContentLine> lines) {

    public static void encode(QuestJournalPacket packet, FriendlyByteBuf buffer) {
        ContentCodec.writeLines(buffer, packet.lines());
    }

    public static QuestJournalPacket decode(FriendlyByteBuf buffer) {
        return new QuestJournalPacket(ContentCodec.readLines(buffer));
    }

    public static void handle(QuestJournalPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientQuestJournal.open(packet)));
        ctx.setPacketHandled(true);
    }
}
