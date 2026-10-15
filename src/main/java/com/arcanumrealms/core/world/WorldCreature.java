package com.arcanumrealms.core.world;

import com.arcanumrealms.core.minion.CreatureDef;
import com.arcanumrealms.core.school.School;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Une creature du monde, telle que la decrit le wiki : rang, PV, ecole, pips de
 * depart, faiblesse ("Incoming Boost"), resistance ("Incoming Resist"), sorts
 * utilises, classification et zone d'apparition.
 *
 * <p>Le moteur de duel ne voit que {@link CreatureDef} : {@link #toCreatureDef()}
 * fait la conversion, si bien que les creatures du monde se battent exactement
 * comme avant.
 */
public record WorldCreature(String id,
                            String nameEn,
                            String nameFr,
                            School school,
                            int rank,
                            int level,
                            int health,
                            int startingPips,
                            Map<School, Integer> resist,
                            Map<School, Integer> boost,
                            boolean stunnable,
                            boolean beguilable,
                            String classification,
                            List<String> zoneIds,
                            String entity,
                            int critRating,
                            int blockRating,
                            int powerPipChance,
                            List<String> deck,
                            String lootTable,
                            boolean boss) {

    public WorldCreature {
        resist = Collections.unmodifiableMap(copy(resist));
        boost = Collections.unmodifiableMap(copy(boost));
        zoneIds = Collections.unmodifiableList(new ArrayList<>(zoneIds));
        deck = Collections.unmodifiableList(new ArrayList<>(deck));
    }

    private static Map<School, Integer> copy(Map<School, Integer> source) {
        Map<School, Integer> out = new EnumMap<>(School.class);
        if (source != null) out.putAll(source);
        return out;
    }

    public String name(boolean french) { return french ? nameFr : nameEn; }

    public int resist(School school) { return resist.getOrDefault(school, 0); }

    public int boost(School school) { return boost.getOrDefault(school, 0); }

    /** Pips de depart, convertis en pips de puissance quand ils sont pairs au-dela de 1. */
    public int startingPowerPips() { return startingPips / 2; }

    /** Conversion vers la creature utilisee par le moteur de duel. */
    public CreatureDef toCreatureDef() {
        return new CreatureDef(id, nameEn, nameFr, entity, school, level, health, resist,
                critRating, blockRating, powerPipChance, 0, 0, deck, boss);
    }

    public static Builder builder(String id) { return new Builder(id); }

    /** Constructeur pas a pas pour garder le bestiaire lisible. */
    public static final class Builder {
        private final String id;
        private String nameEn, nameFr;
        private School school = School.EQUILIBRIUM;
        private int rank = 1, level = 1, health = 50, startingPips;
        private final Map<School, Integer> resist = new EnumMap<>(School.class);
        private final Map<School, Integer> boost = new EnumMap<>(School.class);
        private boolean stunnable = true, beguilable = true;
        private String classification = "Creature";
        private final List<String> zoneIds = new ArrayList<>();
        private String entity = "minecraft:zombie";
        private int critRating, blockRating, powerPipChance = 25;
        private final List<String> deck = new ArrayList<>();
        private String lootTable;
        private boolean boss;

        Builder(String id) { this.id = id; this.nameEn = id; }

        public Builder name(String en, String fr) { this.nameEn = en; this.nameFr = fr; return this; }
        public Builder school(School school) { this.school = school; return this; }
        public Builder rank(int rank) { this.rank = rank; return this; }
        public Builder level(int level) { this.level = level; return this; }
        public Builder health(int health) { this.health = health; return this; }
        public Builder pips(int startingPips) { this.startingPips = startingPips; return this; }
        public Builder resist(School school, int percent) { resist.put(school, percent); return this; }
        public Builder boost(School school, int percent) { boost.put(school, percent); return this; }
        public Builder stunnable(boolean value) { this.stunnable = value; return this; }
        public Builder beguilable(boolean value) { this.beguilable = value; return this; }
        public Builder classification(String value) { this.classification = value; return this; }
        public Builder zones(String... ids) { Collections.addAll(zoneIds, ids); return this; }
        public Builder entity(String entity) { this.entity = entity; return this; }
        public Builder crit(int value) { this.critRating = value; return this; }
        public Builder block(int value) { this.blockRating = value; return this; }
        public Builder powerPip(int value) { this.powerPipChance = value; return this; }
        public Builder deck(String... spellIds) { Collections.addAll(deck, spellIds); return this; }
        public Builder loot(String lootTableId) { this.lootTable = lootTableId; return this; }
        public Builder boss(boolean value) { this.boss = value; return this; }

        public WorldCreature build() {
            if (nameFr == null) nameFr = nameEn;
            if (lootTable == null) lootTable = "rank" + rank;
            return new WorldCreature(id, nameEn, nameFr, school, rank, level, health, startingPips,
                    resist, boost, stunnable, beguilable, classification, zoneIds, entity,
                    critRating, blockRating, powerPipChance, deck, lootTable, boss);
        }
    }
}
