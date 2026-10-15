package com.arcanumrealms.core.world;

import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.util.StatsFactory;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * Additionne les statistiques d'un ensemble d'equipement a celles du sorcier.
 *
 * <p>C'est ce qui relie les butins du wiki au moteur de duel : un chapeau trouve
 * sur une creature augmente vraiment les PV, la precision ou la chance de pip de
 * puissance du joueur.
 */
public final class GearStats {

    private GearStats() {}

    /** Ajoute l'equipement a une base (celle de la configuration du serveur). */
    public static StatsFactory.WizardGear merge(StatsFactory.WizardGear base, Collection<GearDef> gear) {
        if (gear == null || gear.isEmpty()) return base;

        int powerPip = base.powerPipChance();
        int crit = base.critRating();
        int block = base.blockRating();
        int accuracy = base.accuracyBonus();
        int flatResist = base.flatResist();
        int heal = base.healBoost();
        int incomingHeal = base.incomingHealBoost();
        int health = base.healthBonus();

        Map<School, Integer> damage = new EnumMap<>(School.class);
        damage.putAll(base.damageBonus());
        Map<School, Integer> resist = new EnumMap<>(School.class);
        resist.putAll(base.resist());
        Map<School, Integer> pierce = new EnumMap<>(School.class);
        pierce.putAll(base.pierce());

        for (GearDef def : gear) {
            if (def == null) continue;
            powerPip += def.powerPipChance();
            crit += def.critRating();
            block += def.blockRating();
            accuracy += def.accuracyBonus();
            flatResist += def.flatResist();
            health += def.healthBonus();
            def.damageBonus().forEach((school, value) -> damage.merge(school, value, Integer::sum));
            def.resist().forEach((school, value) -> resist.merge(school, value, Integer::sum));
            if (def.pierce() != 0) {
                for (School school : School.values()) pierce.merge(school, def.pierce(), Integer::sum);
            }
        }

        return new StatsFactory.WizardGear(powerPip, crit, block, accuracy, flatResist, heal, incomingHeal,
                health, damage, resist, pierce);
    }

    /** PV maximum d'un sorcier, equipement inclus (plafonne a 1 pour rester jouable). */
    public static int maxHealth(School school, int level, StatsFactory.WizardGear gear) {
        return StatsFactory.healthFor(school, level, gear.healthBonus());
    }
}
