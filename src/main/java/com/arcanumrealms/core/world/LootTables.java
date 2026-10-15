package com.arcanumrealms.core.world;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tables de butin.
 *
 * <p>Les fourchettes d'or et les objets reprennent la logique des sections
 * "Drops" du wiki : l'or depend du rang de la creature (2 a 5 pieces pour une
 * creature de rang 2, par exemple), les equipements tombent par palier et les
 * reagents (os, poudre de blaze, eclats de amethyste...) servent aux quetes de
 * recolte.
 */
public final class LootTables {

    private static final Map<String, LootTable> TABLES = new LinkedHashMap<>();

    private LootTables() {}

    public static LootTable get(String id) {
        LootTable table = TABLES.get(id);
        if (table == null) throw new IllegalArgumentException("Table de butin inconnue: " + id);
        return table;
    }

    public static Collection<LootTable> all() { return Collections.unmodifiableCollection(TABLES.values()); }

    private static void add(LootTable table) {
        if (TABLES.put(table.id(), table) != null) throw new IllegalStateException("Table en double: " + table.id());
    }

    static {
        // ---------------------------------------------------------- creatures errantes
        add(LootTable.builder("rank1").gold(1, 3)
                .gear("novice_hat", 2).gear("novice_boots", 2).gear("novice_ring", 2)
                .item("minecraft:bone", 12, 1, 2)
                .item("minecraft:rotten_flesh", 10, 1, 2)
                .build());

        add(LootTable.builder("rank2").gold(2, 5)
                .gear("novice_robe", 3).gear("novice_wand", 3).gear("novice_athame", 3)
                .gear("grove_hood", 2).gear("grove_boots", 2)
                .item("minecraft:bone", 15, 1, 3)
                .item("minecraft:amethyst_shard", 5)
                .build());

        add(LootTable.builder("rank3").gold(4, 9)
                .gear("grove_robe", 3).gear("grove_wand", 3).gear("grove_ring", 3)
                .gear("ember_cowl", 2).gear("ember_boots", 2)
                .item("minecraft:bone", 18, 2, 3)
                .item("minecraft:blaze_powder", 10, 1, 2)
                .item("minecraft:amethyst_shard", 8)
                .build());

        add(LootTable.builder("rank4").gold(6, 12)
                .gear("ember_robe", 3).gear("ember_wand", 3).gear("ember_athame", 3)
                .gear("tide_crown", 2).gear("tide_boots", 2)
                .item("minecraft:blaze_powder", 12, 1, 3)
                .item("minecraft:prismarine_shard", 12, 1, 3)
                .item("minecraft:amethyst_shard", 10, 1, 2)
                .build());

        add(LootTable.builder("rank5").gold(9, 16)
                .gear("tide_robe", 3).gear("tide_wand", 3).gear("tide_ring", 3)
                .gear("hollow_crown", 2)
                .item("minecraft:bone", 20, 2, 4)
                .item("minecraft:blue_ice", 12, 1, 2)
                .item("minecraft:diamond", 4)
                .build());

        add(LootTable.builder("rank6").gold(14, 24)
                .gear("hollow_robe", 3).gear("hollow_wand", 3).gear("hollow_athame", 3)
                .item("minecraft:diamond", 8)
                .item("minecraft:netherite_scrap", 3)
                .build());

        // ------------------------------------------------------------------ boss
        add(LootTable.builder("boss_alley").gold(25, 45)
                .gear("ember_robe", 25).gear("ember_wand", 25).gear("ember_amulet", 20)
                .gear("ember_athame", 20).gear("pyromancy_athame", 12).gear("necromancy_athame", 12)
                .item("minecraft:blaze_rod", 40, 1, 2)
                .item("minecraft:amethyst_shard", 30, 1, 3)
                .build());

        add(LootTable.builder("boss_avenue").gold(30, 55)
                .gear("tide_robe", 25).gear("tide_wand", 25).gear("tide_amulet", 20)
                .gear("tide_deck", 20).gear("tempest_athame", 12).gear("runomancy_athame", 12)
                .item("minecraft:prismarine_shard", 40, 2, 4)
                .item("minecraft:heart_of_the_sea", 15)
                .build());

        add(LootTable.builder("boss_road").gold(40, 70)
                .gear("hollow_robe", 25).gear("hollow_wand", 25).gear("hollow_amulet", 20)
                .gear("hollow_ring", 20).gear("frost_athame", 12).gear("sylvan_athame", 12)
                .item("minecraft:blue_ice", 45, 2, 4)
                .item("minecraft:diamond", 25, 1, 3)
                .build());

        add(LootTable.builder("boss_hollow").gold(60, 110)
                .gear("hollow_crown", 30).gear("hollow_robe", 30).gear("hollow_wand", 30)
                .gear("hollow_athame", 25).gear("hollow_amulet", 25).gear("hollow_deck", 25)
                .gear("necromancy_athame", 20).gear("equilibrium_athame", 20)
                .item("minecraft:netherite_scrap", 30, 1, 2)
                .item("minecraft:diamond", 40, 2, 4)
                .item("minecraft:echo_shard", 20, 1, 2)
                .build());
    }
}
