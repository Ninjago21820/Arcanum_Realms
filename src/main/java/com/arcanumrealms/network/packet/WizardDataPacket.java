package com.arcanumrealms.network.packet;

import com.arcanumrealms.core.school.School;
import com.arcanumrealms.player.WizardData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** Fiche du sorcier : ecole, niveau, sorts appris, deck. Sert au grimoire client. */
public final class WizardDataPacket {

    public final String school;
    public final int level;
    public final int xp;
    public final int xpNext;
    public final int powerPipChance;
    public final int critRating;
    public final Set<String> learned;
    public final Map<String, Integer> deck;

    public WizardDataPacket(String school, int level, int xp, int xpNext, int powerPipChance, int critRating,
                            Set<String> learned, Map<String, Integer> deck) {
        this.school = school;
        this.level = level;
        this.xp = xp;
        this.xpNext = xpNext;
        this.powerPipChance = powerPipChance;
        this.critRating = critRating;
        this.learned = learned;
        this.deck = deck;
    }

    public static WizardDataPacket of(ServerPlayer player) {
        WizardData data = WizardData.load(player);
        data.ensureStarterDeck();
        return new WizardDataPacket(data.school().id(), data.level(), data.xp(), data.xpForNextLevel(),
                data.powerPipChance(), data.critRating(), new LinkedHashSet<>(data.learnedIds()),
                new LinkedHashMap<>(data.deck()));
    }

    public School parsedSchool() {
        try {
            return School.byId(school);
        } catch (IllegalArgumentException e) {
            return School.EQUILIBRIUM;
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(school);
        buf.writeVarInt(level);
        buf.writeVarInt(xp);
        buf.writeVarInt(xpNext);
        buf.writeVarInt(powerPipChance);
        buf.writeVarInt(critRating);
        buf.writeVarInt(learned.size());
        for (String id : learned) buf.writeUtf(id);
        buf.writeVarInt(deck.size());
        for (Map.Entry<String, Integer> e : deck.entrySet()) {
            buf.writeUtf(e.getKey());
            buf.writeVarInt(e.getValue());
        }
    }

    public static WizardDataPacket decode(FriendlyByteBuf buf) {
        String school = buf.readUtf();
        int level = buf.readVarInt();
        int xp = buf.readVarInt();
        int xpNext = buf.readVarInt();
        int powerPipChance = buf.readVarInt();
        int critRating = buf.readVarInt();
        int learnedCount = buf.readVarInt();
        Set<String> learned = new LinkedHashSet<>();
        for (int i = 0; i < learnedCount; i++) learned.add(buf.readUtf());
        int deckCount = buf.readVarInt();
        Map<String, Integer> deck = new LinkedHashMap<>();
        for (int i = 0; i < deckCount; i++) deck.put(buf.readUtf(), buf.readVarInt());
        return new WizardDataPacket(school, level, xp, xpNext, powerPipChance, critRating, learned, deck);
    }

    public static void handle(WizardDataPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                com.arcanumrealms.client.ClientBattleBridge.acceptWizardData(message);
            }
        });
        context.setPacketHandled(true);
    }
}
