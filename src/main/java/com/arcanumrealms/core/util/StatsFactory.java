package com.arcanumrealms.core.util;

import com.arcanumrealms.core.battle.CombatantStats;
import com.arcanumrealms.core.minion.CreatureDef;
import com.arcanumrealms.core.school.School;

import java.util.Map;

/**
 * Fabrique de statistiques : traduit un niveau et un equipement en
 * caracteristiques de duel, comme le fait Wizard101.
 *
 * <p>Les valeurs de "base" (400 points de vie au niveau 1, +45 par niveau) sont
 * calibrees pour que les degats des sorts (65-105 pour un sort a 1 pip) donnent
 * la meme impression qu'en jeu : environ 6 a 8 sorts pour vider une barre de vie.
 */
public final class StatsFactory {

    public static final int BASE_HEALTH = 400;
    public static final int HEALTH_PER_LEVEL = 45;

    private StatsFactory() {}

    public static int healthFor(School school, int level) {
        int raw = BASE_HEALTH + Math.max(0, level - 1) * HEALTH_PER_LEVEL;
        return Math.round(raw * school.healthMultiplier());
    }

    /**
     * Equipement d'un sorcier. Tout est optionnel : les valeurs par defaut
     * correspondent a un sorcier sans equipement mais avec un peu de chance de pip de pouvoir.
     */
    public record WizardGear(int powerPipChance,
                             int critRating,
                             int blockRating,
                             int accuracyBonus,
                             int flatResist,
                             int healBoost,
                             int incomingHealBoost,
                             Map<School, Integer> damageBonus,
                             Map<School, Integer> resist,
                             Map<School, Integer> pierce) {

        public static WizardGear none() {
            return new WizardGear(25, 0, 0, 0, 0, 0, 0, Map.of(), Map.of(), Map.of());
        }

        public WizardGear withPowerPipChance(int value) { return new WizardGear(value, critRating, blockRating, accuracyBonus, flatResist, healBoost, incomingHealBoost, damageBonus, resist, pierce); }
        public WizardGear withCrit(int value) { return new WizardGear(powerPipChance, value, blockRating, accuracyBonus, flatResist, healBoost, incomingHealBoost, damageBonus, resist, pierce); }
    }

    public static CombatantStats forWizard(School school, int level, WizardGear gear) {
        CombatantStats.Builder b = CombatantStats.builder()
                .powerPipChance(gear.powerPipChance())
                .crit(gear.critRating())
                .block(gear.blockRating())
                .accuracy(gear.accuracyBonus())
                .flatResist(gear.flatResist())
                .healBoost(gear.healBoost())
                .incomingHealBoost(gear.incomingHealBoost());
        gear.damageBonus().forEach(b::damage);
        gear.resist().forEach(b::resist);
        gear.pierce().forEach(b::pierce);
        return b.build();
    }

    /** Statistiques d'une creature sauvage ou d'un serviteur. */
    public static CombatantStats forCreature(CreatureDef def) {
        CombatantStats.Builder b = CombatantStats.builder()
                .powerPipChance(def.powerPipChance())
                .crit(def.critRating())
                .block(def.blockRating())
                .accuracy(def.accuracyBonus())
                .damage(def.school(), def.damageBonus());
        def.resist().forEach(b::resist);
        return b.build();
    }
}
