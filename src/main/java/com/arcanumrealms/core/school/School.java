package com.arcanumrealms.core.school;

import java.util.Locale;

/**
 * Les sept ecoles d'Arcanum Realms.
 *
 * <p>Chaque ecole reprend exactement le profil d'une ecole de Wizard101 :
 * <ul>
 *   <li>{@code baseAccuracy} : precision de base des sorts (chance de ne pas fizzle)</li>
 *   <li>{@code damagePerPip} : budget de degats par pip (DPP) servant a calibrer les sorts</li>
 *   <li>{@code bladePercent} / {@code trapPercent} : valeur des lames et des pieges d'ecole</li>
 *   <li>{@code healthMultiplier} : facteur de vie (Glacier tanky, Tempete fragile)</li>
 * </ul>
 *
 * <p>Correspondance avec Wizard101 (le mod est volontairement "inspire" et non copie) :
 * Pyromancie = Fire, Tempete = Storm, Givre = Ice, Runomancie = Myth,
 * Sylve = Life, Necromancie = Death, Equilibre = Balance.
 */
public enum School {

    //                 id             acc  dpp  blade trap  health  couleur   EN            FR
    PYROMANCY      ("pyromancy",      75, 100,  35,  25,  0.95f, 0xE4592B, "Pyromancy",    "Pyromancie"),
    TEMPEST        ("tempest",        70, 125,  35,  25,  0.85f, 0x7A55D6, "Tempest",      "Tempete"),
    FROST          ("frost",          80,  85,  35,  30,  1.25f, 0x74C4E8, "Frost",        "Givre"),
    RUNOMANCY      ("runomancy",      80,  90,  35,  25,  1.00f, 0xD9A93C, "Runomancy",    "Runomancie"),
    SYLVAN         ("sylvan",         90,  85,  35,  25,  1.10f, 0x5FBF63, "Sylvan",       "Sylve"),
    NECROMANCY     ("necromancy",     85,  85,  35,  30,  1.00f, 0x6B7280, "Necromancy",   "Necromancie"),
    EQUILIBRIUM    ("equilibrium",    85,  85,  25,  30,  1.05f, 0xD7C08D, "Equilibrium",  "Equilibre");

    private final String id;
    private final int baseAccuracy;
    private final int damagePerPip;
    private final int bladePercent;
    private final int trapPercent;
    private final float healthMultiplier;
    private final int color;
    private final String displayEn;
    private final String displayFr;

    School(String id, int baseAccuracy, int damagePerPip, int bladePercent, int trapPercent,
           float healthMultiplier, int color, String displayEn, String displayFr) {
        this.id = id;
        this.baseAccuracy = baseAccuracy;
        this.damagePerPip = damagePerPip;
        this.bladePercent = bladePercent;
        this.trapPercent = trapPercent;
        this.healthMultiplier = healthMultiplier;
        this.color = color;
        this.displayEn = displayEn;
        this.displayFr = displayFr;
    }

    public String id() { return id; }
    public int baseAccuracy() { return baseAccuracy; }
    public int damagePerPip() { return damagePerPip; }
    public int bladePercent() { return bladePercent; }
    public int trapPercent() { return trapPercent; }
    public float healthMultiplier() { return healthMultiplier; }
    /** Couleur RGB (0xRRGGBB) utilisee par l'interface des cartes. */
    public int color() { return color; }
    public String displayEn() { return displayEn; }
    public String displayFr() { return displayFr; }

    public String display(boolean french) { return french ? displayFr : displayEn; }

    /** Cle de langue utilisee dans les fichiers lang du mod. */
    public String langKey() { return "school.arcanum_realms." + id; }

    /** L'ecole opposee : sert aux prismes et aux bonus d'ecole (Feu &lt;-&gt; Givre, etc.). */
    public School opposing() {
        return switch (this) {
            case PYROMANCY -> FROST;
            case FROST -> PYROMANCY;
            case TEMPEST -> RUNOMANCY;
            case RUNOMANCY -> TEMPEST;
            case SYLVAN -> NECROMANCY;
            case NECROMANCY -> SYLVAN;
            case EQUILIBRIUM -> EQUILIBRIUM;
        };
    }

    public static School byId(String id) {
        String needle = id.toLowerCase(Locale.ROOT);
        for (School s : values()) {
            if (s.id.equals(needle) || s.name().toLowerCase(Locale.ROOT).equals(needle)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Ecole inconnue: " + id);
    }
}
