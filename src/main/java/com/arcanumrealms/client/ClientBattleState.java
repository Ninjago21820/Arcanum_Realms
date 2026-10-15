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
    /**
     * Nouvel etat de duel.
     *
     * <p>Les noms de creatures, d'etats et les lignes du journal sont composes par
     * le serveur : on y remplace les noms connus par ceux du joueur (sorts,
     * creatures, PNJ, equipement, ecoles). Renommer un sort se voit donc partout,
     * y compris dans le journal de combat.
     */
    public static void setBattle(BattleStatePacket packet) {
        battle = packet == null ? null : localize(packet);
    }

    private static BattleStatePacket localize(BattleStatePacket packet) {
        java.util.List<BattleStatePacket.Actor> actors = new java.util.ArrayList<>(packet.actors.size());
        for (BattleStatePacket.Actor actor : packet.actors) {
            java.util.List<String> statuses = new java.util.ArrayList<>(actor.statuses.size());
            for (String status : actor.statuses) statuses.add(ContentResolver.substitute(status));
            actors.add(new BattleStatePacket.Actor(actor.id, ContentResolver.substitute(actor.name),
                    actor.school, actor.health, actor.maxHealth, actor.player, actor.self,
                    actor.defeated, actor.fled, actor.pips, statuses));
        }
        java.util.List<BattleStatePacket.Line> events = new java.util.ArrayList<>(packet.events.size());
        for (BattleStatePacket.Line line : packet.events) {
            events.add(new BattleStatePacket.Line(ContentResolver.substitute(line.fr),
                    ContentResolver.substitute(line.en)));
        }
        return new BattleStatePacket(packet.battleId, packet.round, packet.phase, packet.secondsLeft,
                packet.selfId, packet.canAct, packet.canFlee, actors, packet.hand, events,
                ContentResolver.substitute(packet.aura));
    }
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
