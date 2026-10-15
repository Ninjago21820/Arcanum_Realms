package com.arcanumrealms.core.spell;

/** Categorie d'un sort : sert a l'affichage, a l'IA et aux statistiques. */
public enum SpellKind {
    /** Attaque de baguette : 0 pip, toujours disponible pour ne jamais rester bloque. */
    WAND,
    /** Attaque sur une cible. */
    DAMAGE,
    /** Attaque sur toutes les cibles ennemies. */
    DAMAGE_ALL,
    /** Attaque qui soigne le lanceur d'un pourcentage des degats infliges. */
    DRAIN,
    /** Degats sur plusieurs tours. */
    DAMAGE_OVER_TIME,
    /** Soin sur une cible alliee. */
    HEAL,
    /** Soin de toute l'equipe. */
    HEAL_ALL,
    /** Soin sur plusieurs tours. */
    HEAL_OVER_TIME,
    /** Absorbe des degats (bouclier de points de vie). */
    ABSORB,
    /** Lame : bonus sur les prochains sorts offensifs. */
    BLADE,
    /** Piege : bonus sur les prochains sorts offensifs recus par la cible. */
    TRAP,
    /** Bouclier : reduction des degats recus. */
    SHIELD,
    /** Malus de degats infliges. */
    WEAKNESS,
    /** Malus de precision. */
    ACCURACY_DEBUFF,
    /** Etourdissement : la cible passe son tour. */
    STUN,
    /** Invocation d'un serviteur. */
    SUMMON,
    /** Gestion du deck (pioche, reshuffle). */
    DECK,
    /** Aura globale, gain de pips, vol de lame, etc. */
    UTILITY
}
