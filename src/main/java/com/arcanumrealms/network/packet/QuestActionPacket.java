package com.arcanumrealms.network.packet;

import com.arcanumrealms.server.QuestService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Action du joueur sur une quete : accepter, abandonner ou remettre.
 *
 * @param action 0 = accepter, 1 = abandonner, 2 = remettre
 * @param questId identifiant de la quete visee
 */
public record QuestActionPacket(int action, String questId) {

    public static final int ACCEPT = 0;
    public static final int ABANDON = 1;
    public static final int HAND_IN = 2;

    public static void encode(QuestActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.action);
        buffer.writeUtf(packet.questId, 128);
    }

    public static QuestActionPacket decode(FriendlyByteBuf buffer) {
        return new QuestActionPacket(buffer.readVarInt(), buffer.readUtf(128));
    }

    public static void handle(QuestActionPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> QuestService.handleAction(player, packet.action, packet.questId));
        }
        ctx.setPacketHandled(true);
    }
}
