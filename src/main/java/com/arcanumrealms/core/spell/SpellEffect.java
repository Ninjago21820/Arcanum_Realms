package com.arcanumrealms.core.spell;

import com.arcanumrealms.core.school.School;

import java.util.List;
import java.util.Set;

/**
 * Effets elementaires composant un sort.
 *
 * <p>Les effets sont des donnees pures : c'est le moteur de duel qui les resout
 * (voir {@code com.arcanumrealms.core.battle.Battle}). Cela permet de decrire
 * les sorts dans un format compact, testable et facile a reequilibrer.
 */
public sealed interface SpellEffect {

    /** Degats directs. {@code school} a {@code null} = ecole du lanceur. */
    record Damage(int min, int max, School school, boolean perPip) implements SpellEffect {
        public Damage(int min, int max, School school) { this(min, max, school, false); }
    }

    /** Degats sur plusieurs tours (comme Fire Elf ou Frostbite). */
    record Dot(int min, int max, School school, int rounds) implements SpellEffect {}

    /**
     * Degats qui rendent des points de vie au lanceur.
     * @param fraction part des degats transformee en soin (0.5 = la moitie, comme Ghoul)
     */
    record Drain(int min, int max, School school, double fraction) implements SpellEffect {}

    /** Soin direct. */
    record Heal(int min, int max, boolean perPip) implements SpellEffect {
        public Heal(int min, int max) { this(min, max, false); }
    }

    /** Soin reparti sur plusieurs tours. */
    record Hot(int min, int max, int rounds) implements SpellEffect {}

    /** Absorbe un montant fixe de degats (comme Ice Armor). */
    record Absorb(int amount) implements SpellEffect {}

    /**
     * Lame : bonus de degats sur les prochains sorts offensifs du porteur.
     * @param schools ecoles concernees, vide = toutes (lame universelle)
     */
    record Blade(int percent, Set<School> schools, int uses) implements SpellEffect {}

    /** Piege : bonus de degats sur les prochains sorts offensifs subis par la cible. */
    record Trap(int percent, Set<School> schools, int uses) implements SpellEffect {}

    /** Bouclier : reduction des degats subis. */
    record Shield(int percent, Set<School> schools, int uses) implements SpellEffect {}

    /** Malus d'attaque : reduit les degats infliges par la cible. */
    record Weakness(int percent, int uses) implements SpellEffect {}

    /** Modifie la precision de la cible (valeur negative = malus). */
    record AccuracyMod(int percent, int uses) implements SpellEffect {}

    /** Etourdit la cible pendant N tours (les degats la reveillent). */
    record Stun(int rounds) implements SpellEffect {}

    /** Invoque un serviteur. */
    record Summon(String minionId) implements SpellEffect {}

    /** Donne des pips immediatement. */
    record GainPips(int pips) implements SpellEffect {}

    /** Pioche des cartes. */
    record DrawCards(int count) implements SpellEffect {}

    /** Remelange la defausse dans le deck (sort Reshuffle). */
    record Reshuffle() implements SpellEffect {}

    /** Retire un effet nefaste du porteur. */
    record Cleanse(CleanseTarget target) implements SpellEffect {}

    /** Vole une lame a l'ennemi. */
    record StealCharm() implements SpellEffect {}

    /** Detruit une lame de l'ennemi. */
    record Disarm() implements SpellEffect {}

    /** Perce-resistance temporaire. */
    record PierceAura(int percent, int uses) implements SpellEffect {}

    /** Aura de duel qui affecte tout le monde pendant N tours. */
    record Aura(AuraType type, int percent, Set<School> schools, int rounds) implements SpellEffect {}

    /** Lame de soin : bonus sur les prochains sorts de soin du porteur (Guiding Light). */
    record HealBlade(int percent, int uses) implements SpellEffect {}

    /** Piege pose sur le lanceur (Feint). Les degats subis ensuite sont augmentes. */
    record SelfTrap(int percent, Set<School> schools, int uses) implements SpellEffect {}

    /** N'applique qu'un seul des effets proposes, tire au hasard (Eclair Fracture). */
    record OneOf(List<SpellEffect> options) implements SpellEffect {}

    /** Degats dont l'ecole est tiree au hasard dans une liste (Spectral Blast). */
    record RandomSchoolDamage(int min, int max, Set<School> schools) implements SpellEffect {}

    /** Le lanceur se blesse (Blood Offering, Sacrifice). */
    record SelfDamage(int flat, int percentOfMax) implements SpellEffect {}

    /** Transforme des pips en lame : +X% par pip depense (Supercharge). */
    record PipBlade(int percentPerPip, School school) implements SpellEffect {}

    enum CleanseTarget {
        /** Retire un malus offensif (affaiblissement, malus de precision). */
        HARMFUL_CHARM,
        /** Retire un sort de degats sur plusieurs tours. */
        DAMAGE_OVER_TIME,
        /** Retire tous les effets nefastes retirables. */
        ALL
    }

    enum AuraType {
        /** +X% de degats pour les ecoles indiquees, pour tous les participants. */
        DAMAGE_BOOST,
        /** +X% de soins pour tous les participants. */
        HEAL_BOOST,
        /** -X% de soins pour tous les participants (Doom and Gloom). */
        HEAL_REDUCTION,
        /** +X% de precision pour tous les participants. */
        ACCURACY_BOOST
    }
}
