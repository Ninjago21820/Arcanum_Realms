package com.arcanumrealms.core.minion;

import com.arcanumrealms.core.school.School;

import java.util.*;

import static com.arcanumrealms.core.school.School.*;

/**
 * Catalogue des creatures du mod : serviteurs invoques et ennemis errants.
 *
 * <p>Les decks sont exprimes avec les identifiants de sorts du {@code SpellBook} :
 * une creature se bat donc avec les memes cartes que les joueurs, comme les
 * creatures de Wizard101.
 */
public final class Creatures {

    private static final Map<String, CreatureDef> DEFS = new LinkedHashMap<>();

    private Creatures() {}

    public static CreatureDef get(String id) {
        CreatureDef def = DEFS.get(id);
        if (def == null) throw new IllegalArgumentException("Creature inconnue: " + id);
        return def;
    }

    public static Collection<CreatureDef> all() { return Collections.unmodifiableCollection(DEFS.values()); }

    /** Ennemis qui peuvent apparaitre dans le monde (hors invocations). */
    public static List<CreatureDef> wanderers() {
        return DEFS.values().stream().filter(d -> !d.boss() && !d.id().endsWith("_minion") && !isSummon(d)).toList();
    }

    private static boolean isSummon(CreatureDef d) {
        return SUMMONS.contains(d.id());
    }

    private static final Set<String> SUMMONS = Set.of(
            "cinderling", "wave_sentinel", "rime_sentinel", "runestone_minion", "rune_colossus");

    private static void def(CreatureDef def) {
        if (DEFS.put(def.id(), def) != null) throw new IllegalStateException("Creature en double: " + def.id());
    }

    private static List<String> deck(String... spells) {
        List<String> out = new ArrayList<>();
        for (String s : spells) out.add(s);
        return out;
    }

    private static void summon(String id, String en, String fr, School school, int level, int health, String... spells) {
        def(new CreatureDef(id, en, fr, "arcanum_realms:" + id, school, level, health,
                Map.of(school, 0), 0, 0, 25, 0, 0, deck(spells), false));
    }

    private static void enemy(String id, String en, String fr, School school, int level, int health,
                              int resist, int crit, int block, int powerPips, int accuracy, int damage, String... spells) {
        def(new CreatureDef(id, en, fr, "arcanum_realms:" + id, school, level, health,
                Map.of(school, resist), crit, block, powerPips, accuracy, damage, deck(spells), false));
    }

    private static void boss(String id, String en, String fr, School school, int level, int health,
                             int resist, int crit, int block, int powerPips, int accuracy, int damage, String... spells) {
        def(new CreatureDef(id, en, fr, "arcanum_realms:" + id, school, level, health,
                Map.of(school, resist), crit, block, powerPips, accuracy, damage, deck(spells), true));
    }

    static {
        // ---------------------------------------------------------- serviteurs
        summon("cinderling", "Cinderling", "Braisillon", PYROMANCY, 4, 250,
                "ember_kitten", "ember_kitten", "ember_kitten", "pyre_mark", "emberedge");
        summon("wave_sentinel", "Wave Sentinel", "Sentinelle des Flots", TEMPEST, 5, 220,
                "spark_serpent", "spark_serpent", "spark_serpent", "gale_bats", "galeedge");
        summon("rime_sentinel", "Rime Sentinel", "Sentinelle de Givre", FROST, 6, 420,
                "frost_beetle", "frost_beetle", "snow_serpent", "snow_serpent", "rimeedge", "rimeedge");
        summon("runestone_minion", "Runestone Minion", "Serviteur Runique", RUNOMANCY, 5, 300,
                "rune_bat", "rune_bat", "rune_bat", "stone_troll", "runestone_minion", "glyph_edge");
        summon("rune_colossus", "Rune Colossus", "Colosse Runique", RUNOMANCY, 12, 700,
                "rune_cyclops", "rune_cyclops", "stone_troll", "stone_troll", "glyph_edge", "rune_mark", "rune_cyclops");

        // ------------------------------------------------------------ ennemis
        enemy("ember_imp", "Ember Imp", "Diablotin de Braise", PYROMANCY, 4, 350, 0, 0, 0, 25, 0, 0,
                "ember_kitten", "ember_kitten", "ember_kitten", "pyre_mark", "emberedge", "wand_pyromancy");
        enemy("static_wisp", "Static Wisp", "Follet Statique", TEMPEST, 6, 320, 0, 0, 0, 40, 0, 5,
                "spark_serpent", "spark_serpent", "spark_serpent", "gale_bats", "galeedge", "wand_tempest");
        enemy("bramble_thrall", "Bramble Thrall", "Serviteur des Ronces", SYLVAN, 5, 380, 0, 0, 0, 25, 0, 0,
                "sprout_imp", "sprout_imp", "cloverkin", "cloverkin", "minor_blessing", "wand_sylvan");
        enemy("rune_gnome", "Rune Gnome", "Gnome Runique", RUNOMANCY, 6, 400, 0, 0, 0, 30, 0, 0,
                "rune_bat", "rune_bat", "stone_troll", "stone_troll", "rune_mark", "wand_runomancy");
        enemy("grave_shade", "Grave Shade", "Ombre Sepulcrale", NECROMANCY, 7, 420, 0, 0, 0, 30, 0, 5,
                "wisp_wraith", "wisp_wraith", "ghoul", "ghoul", "grave_edge", "wand_necromancy");
        enemy("glacier_hound", "Glacier Hound", "Molosse des Glaciers", FROST, 8, 520, 5, 0, 10, 30, 0, 0,
                "frost_beetle", "frost_beetle", "snow_serpent", "snow_serpent", "rimeedge", "warding_shield", "wand_frost");
        enemy("balance_scribe", "Balance Scribe", "Scribe de l'Equilibre", EQUILIBRIUM, 9, 450, 0, 0, 0, 30, 0, 0,
                "scarab", "scarab", "scorpion", "scorpion", "weakness", "equilibrium_edge", "wand_equilibrium");
        enemy("frost_wraith", "Frost Wraith", "Spectre de Givre", FROST, 12, 700, 10, 5, 15, 40, 0, 10,
                "snow_serpent", "snow_serpent", "snow_serpent", "rime_chains", "rimeedge", "warding_shield", "wand_frost");

        // -------------------------------------------------------------- boss
        boss("cinder_tyrant", "Cinder Tyrant", "Tyran des Braises", PYROMANCY, 15, 1500, 15, 20, 30, 50, 0, 20,
                "sunhawk", "sunhawk", "sunhawk", "ashfall", "cinder_wisp", "pyre_mark", "emberedge", "cinder_wisp");
        boss("frost_matriarch", "Frost Matriarch", "Matriarche de Givre", FROST, 20, 2200, 25, 20, 40, 60, 0, 25,
                "ice_wyvern", "ice_wyvern", "blizzard", "snow_serpent", "snow_serpent",
                "glacial_aegis", "rimeedge", "frost_mark", "rime_chains");
    }
}
