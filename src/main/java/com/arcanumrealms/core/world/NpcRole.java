package com.arcanumrealms.core.world;

/** Role d'un PNJ : ce que le joueur peut faire en lui parlant. */
public enum NpcRole {
    /** Donne et rend les quetes. */
    QUEST_GIVER,
    /** Enseigne les sorts de son ecole (voir les quetes d'ecole). */
    TRAINER,
    /** Vend de l'equipement contre de l'or. */
    SHOPKEEPER,
    /** Garde, soldat : donne des indications. */
    GUARD,
    /** Habitant : dialogue d'ambiance uniquement. */
    VILLAGER
}
