package com.arcanumrealms.network.packet;

import com.arcanumrealms.client.ClientNpcDialogue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Dialogue d'un PNJ, du serveur vers le client.
 *
 * <p>Le serveur envoie les lignes deja mises en forme et la liste des actions
 * possibles ; le client ne fait que les afficher et renvoyer le choix du joueur.
 */
public record NpcDialoguePacket(String npcId, List<String> lines, List<Action> actions) {

    /** Bouton affiche sous le dialogue (voir les constantes KIND_* de DialogueService). */
    public record Action(String id, String label, List<String> goals, String rewards, int kind) {}

    public static void encode(NpcDialoguePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.npcId, 128);
        buffer.writeVarInt(packet.lines.size());
        for (String line : packet.lines) buffer.writeUtf(line, 512);
        buffer.writeVarInt(packet.actions.size());
        for (Action action : packet.actions) {
            buffer.writeUtf(action.id, 256);
            buffer.writeUtf(action.label, 512);
            buffer.writeVarInt(action.goals.size());
            for (String goal : action.goals) buffer.writeUtf(goal, 256);
            buffer.writeUtf(action.rewards, 256);
            buffer.writeVarInt(action.kind);
        }
    }

    public static NpcDialoguePacket decode(FriendlyByteBuf buffer) {
        String npcId = buffer.readUtf(128);
        int lineCount = buffer.readVarInt();
        List<String> lines = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++) lines.add(buffer.readUtf(512));
        int actionCount = buffer.readVarInt();
        List<Action> actions = new ArrayList<>(actionCount);
        for (int i = 0; i < actionCount; i++) {
            String id = buffer.readUtf(256);
            String label = buffer.readUtf(512);
            int goalCount = buffer.readVarInt();
            List<String> goals = new ArrayList<>(goalCount);
            for (int g = 0; g < goalCount; g++) goals.add(buffer.readUtf(256));
            String rewards = buffer.readUtf(256);
            int kind = buffer.readVarInt();
            actions.add(new Action(id, label, goals, rewards, kind));
        }
        return new NpcDialoguePacket(npcId, lines, actions);
    }

    public static void handle(NpcDialoguePacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientNpcDialogue.open(packet)));
        ctx.setPacketHandled(true);
    }
}
