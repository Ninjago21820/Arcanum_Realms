package com.arcanumrealms.network.packet;

import com.arcanumrealms.battle.ServerBattleManager;
import com.arcanumrealms.core.battle.BattleAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Action d'un joueur pendant la phase de choix : lancer une carte, passer, abandonner ou defausser. */
public final class BattleActionPacket {

    public static final int CAST = 0;
    public static final int PASS = 1;
    public static final int FLEE = 2;
    public static final int DISCARD = 3;

    public final int action;
    public final int handIndex;
    public final String targetId;
    public final int pipsToSpend;

    public BattleActionPacket(int action, int handIndex, String targetId, int pipsToSpend) {
        this.action = action;
        this.handIndex = handIndex;
        this.targetId = targetId == null ? "" : targetId;
        this.pipsToSpend = pipsToSpend;
    }

    public static BattleActionPacket cast(int handIndex, String targetId, int pips) {
        return new BattleActionPacket(CAST, handIndex, targetId, pips);
    }

    public static BattleActionPacket pass() {
        return new BattleActionPacket(PASS, -1, "", 0);
    }

    public static BattleActionPacket flee() {
        return new BattleActionPacket(FLEE, -1, "", 0);
    }

    public static BattleActionPacket discard(int handIndex) {
        return new BattleActionPacket(DISCARD, handIndex, "", 0);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(action);
        buf.writeVarInt(handIndex);
        buf.writeUtf(targetId);
        buf.writeVarInt(pipsToSpend);
    }

    public static BattleActionPacket decode(FriendlyByteBuf buf) {
        return new BattleActionPacket(buf.readVarInt(), buf.readVarInt(), buf.readUtf(), buf.readVarInt());
    }

    public static void handle(BattleActionPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            switch (message.action) {
                case CAST -> ServerBattleManager.INSTANCE.submitAction(player,
                        BattleAction.cast(message.handIndex, message.targetId.isEmpty() ? null : message.targetId,
                                message.pipsToSpend));
                case PASS -> ServerBattleManager.INSTANCE.submitAction(player, BattleAction.pass());
                case FLEE -> ServerBattleManager.INSTANCE.submitAction(player, BattleAction.flee());
                case DISCARD -> ServerBattleManager.INSTANCE.discardCard(player, message.handIndex);
                default -> { }
            }
        });
        context.setPacketHandled(true);
    }
}
