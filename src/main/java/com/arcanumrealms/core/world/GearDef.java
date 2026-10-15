package com.arcanumrealms.core.world;

import com.arcanumrealms.core.school.School;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Une piece d'equipement. Les statistiques reprennent celles de Wizard101 :
 * PV, chance de pip de puissance, critique, blocage, precision, degats,
 * resistance et percee.
 *
 * @param tier   palier (0 = novice, 1 = apprenti, 2 = adepte, 3 = expert, 4 = maitre)
 * @param school ecole du stuff (les bonus de degats/resistance ne s'appliquent qu'a cette ecole), null = toutes
 */
public record GearDef(String id,
                      String nameEn,
                      String nameFr,
                      GearSlot slot,
                      int tier,
                      int level,
                      int healthBonus,
                      int powerPipChance,
                      int critRating,
                      int blockRating,
                      int accuracyBonus,
                      Map<School, Integer> damageBonus,
                      Map<School, Integer> resist,
                      int pierce,
                      int flatResist,
                      School school) {

    public GearDef {
        damageBonus = Collections.unmodifiableMap(copySchools(damageBonus));
        resist = Collections.unmodifiableMap(copySchools(resist));
    }

    /** Copie une table ecole -&gt; valeur (EnumMap refuse les tables vides en copie). */
    private static Map<School, Integer> copySchools(Map<School, Integer> source) {
        Map<School, Integer> out = new EnumMap<>(School.class);
        if (source != null) out.putAll(source);
        return out;
    }

    public String name(boolean french) { return french ? nameFr : nameEn; }

    /** Valeur du bonus sur une ecole donnee (ou sur toutes si le stuff est neutre). */
    public int damageFor(School target) {
        if (damageBonus.isEmpty()) return 0;
        return damageBonus.getOrDefault(target, 0);
    }

    public int resistFor(School source) {
        if (resist.isEmpty()) return 0;
        return resist.getOrDefault(source, 0);
    }

    /** Resume lisible des statistiques, pour les infobulles. */
    public String stats(boolean french) {
        StringBuilder sb = new StringBuilder();
        append(sb, healthBonus, french ? " PV" : " HP");
        append(sb, powerPipChance, french ? " % pip de puissance" : " % power pip");
        append(sb, critRating, french ? " critique" : " critical");
        append(sb, blockRating, french ? " blocage" : " block");
        append(sb, accuracyBonus, french ? " % precision" : " % accuracy");
        append(sb, pierce, french ? " % percee" : " % pierce");
        append(sb, flatResist, french ? " resistance plate" : " flat resist");
        if (!damageBonus.isEmpty()) {
            int value = damageBonus.values().iterator().next();
            append(sb, value, french ? " % degats" : " % damage");
        }
        if (!resist.isEmpty()) {
            int value = resist.values().iterator().next();
            append(sb, value, french ? " % resistance" : " % resist");
        }
        return sb.toString().trim();
    }

    private static void append(StringBuilder sb, int value, String suffix) {
        if (value == 0) return;
        sb.append(value > 0 ? "+" : "").append(value).append(suffix).append(" ");
    }

    public static Builder builder(String id) { return new Builder(id); }

    /** Constructeur pas a pas pour garder le catalogue lisible. */
    public static final class Builder {
        private final String id;
        private String nameEn, nameFr;
        private GearSlot slot = GearSlot.HAT;
        private int tier, level = 1;
        private int healthBonus, powerPipChance, critRating, blockRating, accuracyBonus, pierce, flatResist;
        private Integer flatDamage;
        private final Map<School, Integer> damageBonus = new EnumMap<>(School.class);
        private final Map<School, Integer> resist = new EnumMap<>(School.class);
        private School school;

        Builder(String id) { this.id = id; this.nameEn = id; }

        public Builder name(String en, String fr) { this.nameEn = en; this.nameFr = fr; return this; }
        public Builder slot(GearSlot slot) { this.slot = slot; return this; }
        public Builder tier(int tier) { this.tier = tier; return this; }
        public Builder level(int level) { this.level = level; return this; }
        public Builder health(int value) { this.healthBonus = value; return this; }
        public Builder powerPip(int value) { this.powerPipChance = value; return this; }
        public Builder crit(int value) { this.critRating = value; return this; }
        public Builder block(int value) { this.blockRating = value; return this; }
        public Builder accuracy(int value) { this.accuracyBonus = value; return this; }
        public Builder pierce(int value) { this.pierce = value; return this; }
        public Builder flatResist(int value) { this.flatResist = value; return this; }
        /** Bonus valable pour toutes les ecoles. */
        public Builder damage(int value) { this.flatDamage = value; return this; }
        public Builder damage(School school, int value) { damageBonus.put(school, value); return this; }
        public Builder resist(School school, int value) { resist.put(school, value); return this; }
        public Builder school(School school) { this.school = school; return this; }

        public GearDef build() {
            if (nameFr == null) nameFr = nameEn;
            Map<School, Integer> damage = flatDamage != null ? allSchools(flatDamage) : damageBonus;
            return new GearDef(id, nameEn, nameFr, slot, tier, level, healthBonus,
                    powerPipChance, critRating, blockRating, accuracyBonus,
                    damage, resist, pierce, flatResist, school);
        }

        private static Map<School, Integer> allSchools(int value) {
            Map<School, Integer> out = new EnumMap<>(School.class);
            for (School s : School.values()) out.put(s, value);
            return out;
        }
    }
}
