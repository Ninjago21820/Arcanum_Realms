package com.arcanumrealms.client;

import com.arcanumrealms.network.packet.BattleStatePacket;
import com.arcanumrealms.network.packet.WizardDataPacket;
import net.minecraft.client.Minecraft;

/** Memoire du client : dernier etat recu du serveur + fiche du sorcier. */
public final class ClientBattleState {

    private static BattleStatePacket battle;
    private static WizardDataPacket wizard;
    /** Index de carte choisi dans l'interface de duel. */
    private static int selectedCard = -1;
    private static String selectedTarget;
    private static int pipsToSpend;
    /** Dernier duel termine (pour l'ecran de resultat). */
    private static String lastResult;

    private ClientBattleState() {}

    public static BattleStatePacket battle() { return battle; }
    public static void setBattle(BattleStatePacket packet) { battle = packet; }
    public static WizardDataPacket wizard() { return wizard; }
    public static void setWizard(WizardDataPacket packet) { wizard = packet; }
    public static boolean inBattle() { return battle != null; }
    public static int selectedCard() { return selectedCard; }
    public static void selectCard(int index) { selectedCard = index; selectedTarget = null; }
    public static String selectedTarget() { return selectedTarget; }
    public static void selectTarget(String id) { selectedTarget = id; }
    public static int pipsToSpend() { return pipsToSpend; }
    public static void setPipsToSpend(int value) { pipsToSpend = Math.max(0, value); }
    public static String lastResult() { return lastResult; }
    public static void setLastResult(String value) { lastResult = value; }

    public static void clearSelection() {
        selectedCard = -1;
        selectedTarget = null;
        pipsToSpend = 0;
    }

    public static void reset() {
        battle = null;
        clearSelection();
    }

    /** Vrai si le client est configure en francais. */
    public static boolean french() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getLanguageManager() == null) return true;
        return mc.getLanguageManager().getSelected().toLowerCase(java.util.Locale.ROOT).startsWith("fr");
    }
}
