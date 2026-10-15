package com.arcanumrealms.core.spell;

/** Cibles possibles d'un sort, calquees sur les regles de duel de Wizard101. */
public enum TargetMode {
    /** Un ennemi (ou un allie si le sort est bienveillant). */
    SINGLE,
    /** Tous les ennemis. */
    ALL_ENEMIES,
    /** Soi-meme. */
    SELF,
    /** Un allie (ou soi-meme). */
    ALLY,
    /** Toute l'equipe (lanceur inclus). */
    ALL_ALLIES;

    public boolean affectsEnemies() {
        return this == SINGLE || this == ALL_ENEMIES;
    }

    public boolean affectsAllies() {
        return this == SELF || this == ALLY || this == ALL_ALLIES;
    }
}
