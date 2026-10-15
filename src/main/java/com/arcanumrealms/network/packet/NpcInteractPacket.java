package com.arcanumrealms.network.packet;

import com.arcanumrealms.server.DialogueService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Le joueur clique sur un bouton du dialogue d'un PNJ.
 *
 * @param npcId  identifiant du PNJ
 * @param action 0 = accepter la quete, 1 = remettre la quete, 2 = fermer, 3 = acheter
 * @param arg    quete ou objet vise (peut etre vide)
 */
public record NpcInteractPacket(String npcId, int action, String arg) {

    public static final int ACCEPT = 0;
    public static final int HAND_IN = 1;
    public static final int CLOSE = 2;
    public static final int BUY = 3;

    public static void encode(NpcInteractPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.npcId, 128);
        buffer.writeVarInt(packet.action);
        buffer.writeUtf(packet.arg == null ? "" : packet.arg, 256);
    }

    public static NpcInteractPacket decode(FriendlyByteBuf buffer) {
        return new NpcInteractPacket(buffer.readUtf(128), buffer.readVarInt(), buffer.readUtf(256));
    }

    public static void handle(NpcInteractPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> DialogueService.handle(player, packet.npcId, packet.action, packet.arg));
        }
        ctx.setPacketHandled(true);
    }
}
