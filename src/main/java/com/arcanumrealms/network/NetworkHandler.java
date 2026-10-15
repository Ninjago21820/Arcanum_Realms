package com.arcanumrealms.network;

import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.battle.ServerBattleManager;
import com.arcanumrealms.network.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Reseau du mod : etat du duel vers le client, actions du client vers le serveur.
 * Volontairement minimaliste (6 paquets) pour rester simple a faire evoluer.
 */
public final class NetworkHandler {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ArcanumRealms.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    private NetworkHandler() {}

    @SuppressWarnings("unchecked")
    public static void register() {
        int id = 0;
        register(id++, BattleStatePacket.class, BattleStatePacket::encode, BattleStatePacket::decode, BattleStatePacket::handle);
        register(id++, BattleEndPacket.class, BattleEndPacket::encode, BattleEndPacket::decode, BattleEndPacket::handle);
        register(id++, WizardDataPacket.class, WizardDataPacket::encode, WizardDataPacket::decode, WizardDataPacket::handle);
        register(id++, BattleActionPacket.class, BattleActionPacket::encode, BattleActionPacket::decode, BattleActionPacket::handle);
        register(id++, RequestWizardDataPacket.class, RequestWizardDataPacket::encode, RequestWizardDataPacket::decode, RequestWizardDataPacket::handle);
        register(id++, SaveDeckPacket.class, SaveDeckPacket::encode, SaveDeckPacket::decode, SaveDeckPacket::handle);
    }

    private static <T> void register(int id, Class<T> type,
                                     BiConsumer<T, net.minecraft.network.FriendlyByteBuf> encoder,
                                     Function<net.minecraft.network.FriendlyByteBuf, T> decoder,
                                     BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
        CHANNEL.registerMessage(id, type, encoder, decoder, handler);
    }

    // ------------------------------------------------------------ envois

    public static void sendToPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    /** Diffuse l'etat complet du duel a tous les participants. */
    public static void broadcastState(ServerBattleManager.Session session) {
        for (java.util.UUID uuid : session.wizards.keySet()) {
            ServerPlayer player = session.level.getServer().getPlayerList().getPlayer(uuid);
            if (player == null) continue;
            sendToPlayer(player, BattleStatePacket.of(session, player));
        }
    }

    public static void broadcastEnd(ServerBattleManager.Session session) {
        boolean playersWon = session.battle.winner() == com.arcanumrealms.core.battle.Team.PLAYERS;
        for (java.util.UUID uuid : session.wizards.keySet()) {
            ServerPlayer player = session.level.getServer().getPlayerList().getPlayer(uuid);
            if (player == null) continue;
            sendToPlayer(player, new BattleEndPacket(session.id, playersWon, session.battle.isDraw(),
                    session.battle.round()));
        }
    }

    public static void sendWizardData(ServerPlayer player) {
        sendToPlayer(player, WizardDataPacket.of(player));
    }

    /** Envoie un paquet au client, uniquement si la connexion accepte le protocole. */
    public static void sendToClient(Object message, ServerPlayer player) {
        if (CHANNEL.isRemotePresent(player.connection.connection)) {
            sendToPlayer(player, message);
        }
    }
}
