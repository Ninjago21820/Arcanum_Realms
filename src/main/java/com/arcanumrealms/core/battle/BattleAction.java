package com.arcanumrealms.core.battle;

/**
 * Choix d'un combattant pour le round en cours.
 *
 * @param type        nature de l'action
 * @param handIndex   index de la carte dans la main (pour {@code CAST})
 * @param targetId    identifiant de la cible (peut etre {@code null} : premier ennemi)
 * @param pipsToSpend pips investis (sorts a pips variables comme Tempete ou Jugement)
 */
public record BattleAction(Type type, int handIndex, String targetId, int pipsToSpend) {

    public enum Type { CAST, PASS, FLEE }

    public static BattleAction cast(int handIndex, String targetId, int pipsToSpend) {
        return new BattleAction(Type.CAST, handIndex, targetId, pipsToSpend);
    }

    public static BattleAction cast(int handIndex, String targetId) {
        return new BattleAction(Type.CAST, handIndex, targetId, 0);
    }

    public static BattleAction pass() {
        return new BattleAction(Type.PASS, -1, null, 0);
    }

    public static BattleAction flee() {
        return new BattleAction(Type.FLEE, -1, null, 0);
    }
}
