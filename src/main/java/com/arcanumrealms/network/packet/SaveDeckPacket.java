package com.arcanumrealms.network.packet;

import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.player.WizardData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Enregistrement du deck construit dans le grimoire. */
public final class SaveDeckPacket {

    public final Map<String, Integer> deck;

    public SaveDeckPacket(Map<String, Integer> deck) {
        this.deck = deck;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(deck.size());
        for (Map.Entry<String, Integer> e : deck.entrySet()) {
            buf.writeUtf(e.getKey());
            buf.writeVarInt(e.getValue());
        }
    }

    public static SaveDeckPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, Integer> deck = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) deck.put(buf.readUtf(), buf.readVarInt());
        return new SaveDeckPacket(deck);
    }

    public static void handle(SaveDeckPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            WizardData data = WizardData.load(player);
            Map<String, Integer> safe = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> e : message.deck.entrySet()) {
                if (SpellBook.find(e.getKey()).isEmpty()) continue;
                safe.put(e.getKey(), Math.max(0, e.getValue()));
            }
            data.setDeck(safe);
            data.save(player);
            player.displayClientMessage(Component.translatable("arcanum_realms.message.deck_saved",
                    data.deckSize(), WizardData.deckLimit()), false);
        });
        context.setPacketHandled(true);
    }
}
