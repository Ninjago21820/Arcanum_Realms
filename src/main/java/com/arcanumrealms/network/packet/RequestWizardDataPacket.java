package com.arcanumrealms.network.packet;

import com.arcanumrealms.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Demande d'ouverture du grimoire : le client veut sa fiche de sorcier. */
public final class RequestWizardDataPacket {

    public RequestWizardDataPacket() {}

    public void encode(FriendlyByteBuf buf) {}

    public static RequestWizardDataPacket decode(FriendlyByteBuf buf) {
        return new RequestWizardDataPacket();
    }

    public static void handle(RequestWizardDataPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) NetworkHandler.sendWizardData(player);
        });
        context.setPacketHandled(true);
    }
}
