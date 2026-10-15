package com.arcanumrealms.core.world;

import com.arcanumrealms.core.util.Rng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Table de butin d'une creature, comme les sections "Drops" du wiki :
 * une fourchette d'or et une liste d'objets avec leur chance de tomber.
 */
public final class LootTable {

    /**
     * @param kind   equipement ou objet
     * @param id     identifiant
     * @param chance chance de tomber, en pourcentage
     * @param min    quantite minimum
     * @param max    quantite maximum
     */
    public record Entry(LootDrop.LootKind kind, String id, int chance, int min, int max) {

        public LootDrop roll(Rng rng, int luckPercent) {
            int finalChance = Math.max(0, Math.min(100, chance + luckPercent));
            if (!rng.chance(finalChance)) return null;
            int count = max <= min ? min : rng.between(min, max);
            if (count <= 0) return null;
            return new LootDrop(kind, id, count);
        }
    }

    private final String id;
    private final int goldMin;
    private final int goldMax;
    private final List<Entry> entries;

    public LootTable(String id, int goldMin, int goldMax, List<Entry> entries) {
        this.id = id;
        this.goldMin = goldMin;
        this.goldMax = goldMax;
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public String id() { return id; }

    public int goldMin() { return goldMin; }

    public int goldMax() { return goldMax; }

    public List<Entry> entries() { return entries; }

    /** Or lache par une creature (le wiki donne par exemple "2 - 5"). */
    public int rollGold(Rng rng) {
        return goldMax <= goldMin ? goldMin : rng.between(goldMin, goldMax);
    }

    /** Objets laches : chaque entree est tiree independamment. */
    public List<LootDrop> roll(Rng rng, int luckPercent) {
        List<LootDrop> out = new ArrayList<>();
        for (Entry entry : entries) {
            LootDrop drop = entry.roll(rng, luckPercent);
            if (drop != null) out.add(drop);
        }
        return out;
    }

    public List<LootDrop> roll(Rng rng) {
        return roll(rng, 0);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    /** Constructeur pas a pas pour garder les tables lisibles. */
    public static final class Builder {
        private final String id;
        private int goldMin, goldMax;
        private final List<Entry> entries = new ArrayList<>();

        Builder(String id) { this.id = id; }

        public Builder gold(int min, int max) { this.goldMin = min; this.goldMax = max; return this; }

        public Builder gear(String gearId, int chance) { return gear(gearId, chance, 1, 1); }

        public Builder gear(String gearId, int chance, int min, int max) {
            entries.add(new Entry(LootDrop.LootKind.GEAR, gearId, chance, min, max));
            return this;
        }

        public Builder item(String itemId, int chance, int min, int max) {
            entries.add(new Entry(LootDrop.LootKind.ITEM, itemId, chance, min, max));
            return this;
        }

        public Builder item(String itemId, int chance) { return item(itemId, chance, 1, 1); }

        public LootTable build() {
            return new LootTable(id, goldMin, goldMax, entries);
        }
    }
}
