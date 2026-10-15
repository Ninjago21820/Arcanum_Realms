package com.arcanumrealms.core.world;

/**
 * Emplacement d'equipement. Reprend les emplacements de Wizard101 : chapeau,
 * robe, bottes, baguette, dague (athame), anneau, amulette et deck.
 */
public enum GearSlot {
    HAT("chapeau", "hat"),
    ROBE("robe", "robe"),
    BOOTS("bottes", "boots"),
    WAND("baguette", "wand"),
    ATHAME("athame", "athame"),
    RING("anneau", "ring"),
    AMULET("amulette", "amulet"),
    DECK("deck", "deck");

    private final String fr;
    private final String en;

    GearSlot(String fr, String en) {
        this.fr = fr;
        this.en = en;
    }

    public String label(boolean french) { return french ? fr : en; }

    /** Ordre d'affichage dans l'interface d'equipement. */
    public int slotId() { return ordinal(); }
}
