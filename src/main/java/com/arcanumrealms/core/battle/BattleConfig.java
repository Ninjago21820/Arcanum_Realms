package com.arcanumrealms.core.battle;

/**
 * Regles d'un duel. Les valeurs par defaut reproduisent Wizard101.
 *
 * @param handSize         taille de la main (7)
 * @param maxPipSlots      emplacements de pips maximum (7)
 * @param fizzleEnabled    active les echecs de sort
 * @param critMode         mode de calcul du critique
 * @param flatCritChance   chance de critique (%) si {@code critMode == FLAT}
 * @param flatBlockChance  chance de blocage (%) si {@code critMode == FLAT}
 * @param critMultiplier   multiplicateur de degats d'un critique (2.0 dans Wizard101)
 * @param damageScale      multiplicateur global de degats (reequilibrage maison)
 * @param healScale        multiplicateur global de soins
 * @param turnOrder        ordre de passage des combattants
 * @param planningSeconds  duree de la phase de choix (-1 = pas de limite)
 * @param allowFlee        autorise l'abandon en plein duel
 * @param pvp              duel joueur contre joueur
 * @param maxRounds        garde-fou : au dela, match nul
 */
public record BattleConfig(int handSize,
                           int maxPipSlots,
                           boolean fizzleEnabled,
                           CritMode critMode,
                           int flatCritChance,
                           int flatBlockChance,
                           double critMultiplier,
                           double damageScale,
                           double healScale,
                           TurnOrderStrategy turnOrder,
                           int planningSeconds,
                           boolean allowFlee,
                           boolean pvp,
                           int maxRounds) {

    public enum CritMode {
        /** Formule de Wizard101 : chance = 3*l*c/(3c+b), multiplicateur = 2 - 3b/(c+3b). */
        RATING,
        /** Mode simple : pourcentages fixes. */
        FLAT
    }

    public enum TurnOrderStrategy {
        /** Un joueur, un ennemi, un joueur... (cercle de duel). */
        ALTERNATING,
        /** Tous les joueurs, puis tous les ennemis. */
        PLAYERS_FIRST,
        /** Tous les ennemis, puis tous les joueurs. */
        ENEMIES_FIRST
    }

    public static BattleConfig defaults() {
        return new BattleConfig(DeckConstants.HAND_SIZE, 7, true, CritMode.RATING, 0, 0, 2.0, 1.0, 1.0,
                TurnOrderStrategy.ALTERNATING, 60, true, false, 60);
    }

    private static final class DeckConstants {
        static final int HAND_SIZE = 7;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int handSize = 7;
        private int maxPipSlots = 7;
        private boolean fizzleEnabled = true;
        private CritMode critMode = CritMode.RATING;
        private int flatCritChance = 0;
        private int flatBlockChance = 0;
        private double critMultiplier = 2.0;
        private double damageScale = 1.0;
        private double healScale = 1.0;
        private TurnOrderStrategy turnOrder = TurnOrderStrategy.ALTERNATING;
        private int planningSeconds = 60;
        private boolean allowFlee = true;
        private boolean pvp = false;
        private int maxRounds = 60;

        public Builder handSize(int v) { this.handSize = v; return this; }
        public Builder maxPipSlots(int v) { this.maxPipSlots = v; return this; }
        public Builder fizzle(boolean v) { this.fizzleEnabled = v; return this; }
        public Builder critMode(CritMode v) { this.critMode = v; return this; }
        public Builder flatCritChance(int v) { this.flatCritChance = v; return this; }
        public Builder flatBlockChance(int v) { this.flatBlockChance = v; return this; }
        public Builder critMultiplier(double v) { this.critMultiplier = v; return this; }
        public Builder damageScale(double v) { this.damageScale = v; return this; }
        public Builder healScale(double v) { this.healScale = v; return this; }
        public Builder turnOrder(TurnOrderStrategy v) { this.turnOrder = v; return this; }
        public Builder planningSeconds(int v) { this.planningSeconds = v; return this; }
        public Builder allowFlee(boolean v) { this.allowFlee = v; return this; }
        public Builder pvp(boolean v) { this.pvp = v; return this; }
        public Builder maxRounds(int v) { this.maxRounds = v; return this; }

        public BattleConfig build() {
            return new BattleConfig(handSize, maxPipSlots, fizzleEnabled, critMode, flatCritChance, flatBlockChance,
                    critMultiplier, damageScale, healScale, turnOrder, planningSeconds, allowFlee, pvp, maxRounds);
        }
    }
}
