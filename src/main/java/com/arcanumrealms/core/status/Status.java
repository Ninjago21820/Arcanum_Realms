package com.arcanumrealms.core.status;

import com.arcanumrealms.core.school.School;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Un effet actif sur un combattant : lame, piege, bouclier, faiblesse, DoT, etourdissement...
 *
 * <p>Regles reprises de Wizard101 :
 * <ul>
 *   <li>Les lames ({@link Type#BLADE}) et les pieges ({@link Type#TRAP}) n'expirent pas :
 *       ils restent jusqu'a ce qu'ils servent ({@code uses} decremente a chaque sort).</li>
 *   <li>Les boucliers ({@link Type#SHIELD}) consomment une utilisation par attaque subie.</li>
 *   <li>Les effets sur la duree ({@link Type#DOT}, {@link Type#HOT}) et les auras ont un
 *       compteur de rounds qui diminue a la fin de chaque round.</li>
 *   <li>Un etourdissement ({@link Type#STUN}) disparait des que sa cible subit des degats.</li>
 * </ul>
 */
public final class Status {

    public enum Type {
        /** Bonus sur les prochains sorts offensifs du porteur. */
        BLADE,
        /** Bonus sur les prochains sorts offensifs subis par le porteur. */
        TRAP,
        /** Reduction des prochains degats subis. */
        SHIELD,
        /** Malus sur les prochains sorts offensifs du porteur. */
        WEAKNESS,
        /** Modifie la precision (valeur negative = malus). */
        ACCURACY_CHARM,
        /** Bonus sur les prochains sorts de soin du porteur. */
        HEAL_BLADE,
        /** Perce-resistance temporaire. */
        PIERCE_AURA,
        /** Degats sur plusieurs rounds. */
        DOT,
        /** Soins sur plusieurs rounds. */
        HOT,
        /** Reserve de degats absorbes. */
        ABSORB,
        /** Le porteur passe son tour. */
        STUN
    }

    private final Type type;
    private final String sourceSpellId;
    private final String sourceName;
    private final School school;          // ecole des degats (DoT) ou ecole mise en avant, nullable
    private final Set<School> schools;    // filtre d'ecoles, vide = universel
    private final int percent;
    private final int minValue;
    private final int maxValue;
    private int rounds;
    private int uses;
    private int remainingValue;           // pour ABSORB

    private Status(Type type, String sourceSpellId, String sourceName, School school, Set<School> schools,
                   int percent, int minValue, int maxValue, int rounds, int uses, int remainingValue) {
        this.type = Objects.requireNonNull(type);
        this.sourceSpellId = sourceSpellId;
        this.sourceName = sourceName;
        this.school = school;
        this.schools = schools == null ? Set.of() : Collections.unmodifiableSet(schools);
        this.percent = percent;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.rounds = rounds;
        this.uses = uses;
        this.remainingValue = remainingValue;
    }

    // ------------------------------------------------------------ factories

    public static Status blade(int percent, Set<School> schools, int uses, String spellId, String name) {
        return new Status(Type.BLADE, spellId, name, null, schools, percent, 0, 0, -1, uses, 0);
    }

    public static Status trap(int percent, Set<School> schools, int uses, String spellId, String name) {
        return new Status(Type.TRAP, spellId, name, null, schools, percent, 0, 0, -1, uses, 0);
    }

    public static Status shield(int percent, Set<School> schools, int uses, String spellId, String name) {
        return new Status(Type.SHIELD, spellId, name, null, schools, percent, 0, 0, -1, uses, 0);
    }

    public static Status weakness(int percent, int uses, String spellId, String name) {
        return new Status(Type.WEAKNESS, spellId, name, null, Set.of(), percent, 0, 0, -1, uses, 0);
    }

    public static Status accuracy(int percent, int uses, String spellId, String name) {
        return new Status(Type.ACCURACY_CHARM, spellId, name, null, Set.of(), percent, 0, 0, -1, uses, 0);
    }

    public static Status healBlade(int percent, int uses, String spellId, String name) {
        return new Status(Type.HEAL_BLADE, spellId, name, null, Set.of(), percent, 0, 0, -1, uses, 0);
    }

    public static Status pierceAura(int percent, int uses, String spellId, String name) {
        return new Status(Type.PIERCE_AURA, spellId, name, null, Set.of(), percent, 0, 0, -1, uses, 0);
    }

    public static Status dot(int min, int max, School school, int rounds, String spellId, String name) {
        return new Status(Type.DOT, spellId, name, school, Set.of(), 0, min, max, rounds, rounds, 0);
    }

    public static Status hot(int min, int max, int rounds, String spellId, String name) {
        return new Status(Type.HOT, spellId, name, null, Set.of(), 0, min, max, rounds, rounds, 0);
    }

    public static Status absorb(int amount, String spellId, String name) {
        return new Status(Type.ABSORB, spellId, name, null, Set.of(), 0, amount, amount, -1, -1, amount);
    }

    public static Status stun(int rounds, String spellId, String name) {
        return new Status(Type.STUN, spellId, name, null, Set.of(), 0, 0, 0, rounds, rounds, 0);
    }

    // ------------------------------------------------------------ accessors

    public Type type() { return type; }
    public String sourceSpellId() { return sourceSpellId; }
    public String sourceName() { return sourceName; }
    public School school() { return school; }
    public Set<School> schools() { return schools; }
    public int percent() { return percent; }
    public int minValue() { return minValue; }
    public int maxValue() { return maxValue; }
    public int rounds() { return rounds; }
    public int uses() { return uses; }
    public int remainingValue() { return remainingValue; }

    /** Vrai si l'effet s'applique a un sort de l'ecole donnee. */
    public boolean matchesSchool(School s) {
        return schools.isEmpty() || s == null || schools.contains(s);
    }

    /** Effet positif pour son porteur (lame, soin, bouclier). */
    public boolean isPositive() {
        return switch (type) {
            case BLADE, SHIELD, HEAL_BLADE, PIERCE_AURA, HOT, ABSORB -> true;
            default -> false;
        };
    }

    /** Effet nefaste pour son porteur. */
    public boolean isHarmful() {
        return switch (type) {
            case WEAKNESS, ACCURACY_CHARM, DOT, STUN, TRAP -> true;
            default -> false;
        };
    }

    public boolean consumable() {
        return type != Type.DOT && type != Type.HOT && type != Type.ABSORB;
    }

    public void consumeUse() { if (uses > 0) uses--; }
    public void tickRound() { if (rounds > 0) rounds--; }
    public boolean expired() {
        if (type == Type.ABSORB) return remainingValue <= 0;
        return rounds == 0 || (consumable() && uses == 0);
    }

    public int absorb(int amount) {
        int used = Math.min(amount, remainingValue);
        remainingValue -= used;
        return used;
    }

    @Override
    public String toString() {
        return type + "[" + (schools.isEmpty() ? "universel" : schools) + " " + percent + "% uses=" + uses
                + (rounds > 0 ? " rounds=" + rounds : "") + "]";
    }
}
