package com.arcanumrealms.core.battle;

import com.arcanumrealms.core.school.School;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Statistiques hors combat d'un sorcier ou d'une creature : exactement les memes
 * caracteristiques qu'un personnage de Wizard101.
 *
 * @param damageBonus      % de degats par ecole (equipement)
 * @param resist           % de resistance par ecole
 * @param pierce           % de perce-resistance par ecole
 * @param flatResist       resistance fixe (soustraite aux degats finaux)
 * @param accuracyBonus    % de precision en plus (equipement)
 * @param healBoost        % de soins sortants
 * @param incomingHealBoost % de soins recus
 * @param critRating       score de critique
 * @param blockRating      score de blocage
 * @param powerPipChance   % de chance d'obtenir un pip de pouvoir
 * @param masteries        ecoles dont les pips de pouvoir comptent double (amulette de maitrise)
 */
public record CombatantStats(Map<School, Integer> damageBonus,
                             Map<School, Integer> resist,
                             Map<School, Integer> pierce,
                             int flatResist,
                             int accuracyBonus,
                             int healBoost,
                             int incomingHealBoost,
                             int critRating,
                             int blockRating,
                             int powerPipChance,
                             Set<School> masteries) {

    public static final CombatantStats EMPTY = builder().build();

    public int damageBonus(School school) { return damageBonus.getOrDefault(school, 0); }
    public int resist(School school) { return resist.getOrDefault(school, 0); }
    public int pierce(School school) { return pierce.getOrDefault(school, 0); }
    public boolean mastered(School school) { return masteries.contains(school); }

    /** Les pips de pouvoir comptent-ils double pour cette ecole ? */
    public boolean powerPipsCountDouble(School spellSchool, School ownerSchool) {
        return spellSchool == ownerSchool || masteries.contains(spellSchool);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final Map<School, Integer> damageBonus = new EnumMap<>(School.class);
        private final Map<School, Integer> resist = new EnumMap<>(School.class);
        private final Map<School, Integer> pierce = new EnumMap<>(School.class);
        private final Set<School> masteries = new HashSet<>();
        private int flatResist;
        private int accuracyBonus;
        private int healBoost;
        private int incomingHealBoost;
        private int critRating;
        private int blockRating;
        private int powerPipChance;

        public Builder damage(School school, int percent) { damageBonus.put(school, percent); return this; }
        public Builder resist(School school, int percent) { resist.put(school, percent); return this; }
        public Builder pierce(School school, int percent) { pierce.put(school, percent); return this; }
        public Builder flatResist(int value) { this.flatResist = value; return this; }
        public Builder accuracy(int percent) { this.accuracyBonus = percent; return this; }
        public Builder healBoost(int percent) { this.healBoost = percent; return this; }
        public Builder incomingHealBoost(int percent) { this.incomingHealBoost = percent; return this; }
        public Builder crit(int rating) { this.critRating = rating; return this; }
        public Builder block(int rating) { this.blockRating = rating; return this; }
        public Builder powerPipChance(int percent) { this.powerPipChance = percent; return this; }
        public Builder mastery(School school) { masteries.add(school); return this; }

        public CombatantStats build() {
            return new CombatantStats(
                    Collections.unmodifiableMap(new EnumMap<>(damageBonus)),
                    Collections.unmodifiableMap(new EnumMap<>(resist)),
                    Collections.unmodifiableMap(new EnumMap<>(pierce)),
                    flatResist, accuracyBonus, healBoost, incomingHealBoost,
                    critRating, blockRating, powerPipChance,
                    Collections.unmodifiableSet(new HashSet<>(masteries)));
        }
    }
}
