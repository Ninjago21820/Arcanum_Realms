package com.arcanumrealms.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Fin du duel : permet au client de fermer l'interface et d'afficher le resultat. */
public final class BattleEndPacket {

    public final String battleId;
    public final boolean playersWon;
    public final boolean draw;
    public final int rounds;

    public BattleEndPacket(String battleId, boolean playersWon, boolean draw, int rounds) {
        this.battleId = battleId;
        this.playersWon = playersWon;
        this.draw = draw;
        this.rounds = rounds;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(battleId);
        buf.writeBoolean(playersWon);
        buf.writeBoolean(draw);
        buf.writeVarInt(rounds);
    }

    public static BattleEndPacket decode(FriendlyByteBuf buf) {
        return new BattleEndPacket(buf.readUtf(), buf.readBoolean(), buf.readBoolean(), buf.readVarInt());
    }

    public static void handle(BattleEndPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                com.arcanumrealms.client.ClientBattleBridge.acceptEnd(message);
            }
        });
        context.setPacketHandled(true);
    }
}
