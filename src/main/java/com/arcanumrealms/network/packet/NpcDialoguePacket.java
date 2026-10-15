package com.arcanumrealms.network.packet;

import com.arcanumrealms.client.ClientNpcDialogue;
import com.arcanumrealms.core.world.ContentLine;
import com.arcanumrealms.network.ContentCodec;
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
 * <p>Les repliques voyagent avec leur cle de traduction : le client affiche le
 * texte du joueur s'il en a un (pack de ressources, fichier de langue), sinon le
 * texte par defaut ecrit par le serveur.
 */
public record NpcDialoguePacket(String npcId, List<ContentLine> lines, List<Action> actions) {

    /** Bouton affiche sous le dialogue (voir les constantes KIND_* de DialogueService). */
    public record Action(String id, ContentLine label, List<ContentLine> goals,
                         ContentLine rewards, int kind) {}

    public static void encode(NpcDialoguePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.npcId, 128);
        ContentCodec.writeLines(buffer, packet.lines);
        buffer.writeVarInt(packet.actions.size());
        for (Action action : packet.actions) {
            buffer.writeUtf(action.id, 256);
            ContentCodec.writeLine(buffer, action.label());
            ContentCodec.writeLines(buffer, action.goals());
            ContentCodec.writeLine(buffer, action.rewards());
            buffer.writeVarInt(action.kind());
        }
    }

    public static NpcDialoguePacket decode(FriendlyByteBuf buffer) {
        String npcId = buffer.readUtf(128);
        List<ContentLine> lines = ContentCodec.readLines(buffer);
        int actionCount = buffer.readVarInt();
        List<Action> actions = new ArrayList<>(actionCount);
        for (int i = 0; i < actionCount; i++) {
            String id = buffer.readUtf(256);
            ContentLine label = ContentCodec.readLine(buffer);
            List<ContentLine> goals = ContentCodec.readLines(buffer);
            ContentLine rewards = ContentCodec.readLine(buffer);
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
