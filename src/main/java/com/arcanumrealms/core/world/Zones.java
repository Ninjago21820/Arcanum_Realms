package com.arcanumrealms.core.world;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Les zones de la Cite des Arcanes.
 *
 * <p>La progression suit celle du premier monde de Wizard101 : une place
 * centrale, le bosquet des sept ecoles, puis quatre rues qui montent en niveau
 * (le Sentier des Lucioles, l'Allee des Braises, l'Avenue des Marees, le Chemin
 * des Colosses) et enfin le donjon de la Grotte Hantee.
 */
public final class Zones {

    private static final Map<String, ZoneDef> DEFS = new LinkedHashMap<>();

    private Zones() {}

    public static ZoneDef get(String id) {
        ZoneDef def = DEFS.get(id);
        if (def == null) throw new IllegalArgumentException("Zone inconnue: " + id);
        return def;
    }

    public static Optional<ZoneDef> find(String id) {
        return Optional.ofNullable(DEFS.get(id));
    }

    public static Collection<ZoneDef> all() {
        return Collections.unmodifiableCollection(DEFS.values());
    }

    /** Zones accessibles a un niveau donne (le donjon se debloque plus tard). */
    public static List<ZoneDef> unlockedFor(int level) {
        return DEFS.values().stream().filter(z -> level >= z.unlockLevel()).toList();
    }

    private static void add(ZoneDef def) {
        if (DEFS.put(def.id(), def) != null) throw new IllegalStateException("Zone en double: " + def.id());
    }

    static {
        add(new ZoneDef("grand_place",
                "La Grand-Place", "The Great Square",
                "Le coeur de la cite : la fontaine aux pips, le bureau du directeur et les marchands.",
                "The heart of the city: the pip fountain, the headmaster's office and the shops.",
                1, 5,
                List.of(), List.of("headmaster_alderwise", "shopkeeper_beatrice", "guard_quentin"),
                List.of("first_steps", "welcome_to_the_city"), false, null, 1));

        add(new ZoneDef("bosquet",
                "Le Bosquet des Sept", "The Sevenfold Grove",
                "Sept clairieres, une par ecole : c'est ici que les professeurs enseignent leurs sorts.",
                "Seven glades, one per school: this is where the professors teach their spells.",
                1, 20,
                List.of(), List.of("prof_cinderlune", "prof_stormvale", "prof_frostbloom", "prof_cyren",
                        "prof_greenearth", "prof_ossuary", "prof_whitequill"),
                List.of("school_trial_pyromancy", "school_trial_tempest", "school_trial_frost",
                        "school_trial_runomancy", "school_trial_sylvan", "school_trial_necromancy",
                        "school_trial_equilibrium"),
                false, null, 1));

        add(new ZoneDef("sentier_lucioles",
                "Le Sentier des Lucioles", "Firefly Way",
                "Un chemin de campagne ou les premiers esprits se sont mis a roder.",
                "A country path where the first spirits have started to prowl.",
                1, 6,
                List.of("lost_soul", "haunted_minion", "skeletal_warrior", "pesky_pixie",
                        "thorn_sprite", "scarab_swarm"),
                List.of("lea_nightchant", "melisande_whitehorn"),
                List.of("first_steps", "unwelcome_spirits", "bones_and_badges", "pixie_problem",
                        "sprite_trouble", "scarabs_in_the_grass"),
                false, null, 1));

        add(new ZoneDef("allee_braises",
                "L'Allee des Braises", "Ember Alley",
                "Des elfes de feu ont investi les ruelles ; leurs flammes ont fait fondre les ponts.",
                "Fire elves have taken over the alleys; their flames melted the bridges.",
                5, 14,
                List.of("ember_elf_hunter", "ember_elf_pathfinder", "magma_man", "haunted_minion",
                        "skeletal_warrior", "gretta_darkkettle", "wormguts", "melweena_smite",
                        "bastilla_gravewynd", "alicane_swiftarrow"),
                List.of("hanna_emberwright", "private_quinn", "volcano_spirit"),
                List.of("report_to_hanna", "burning_books", "the_pathfinders", "magma_rising",
                        "quinns_orders", "gravewynds_bastion", "swiftarrow_ambush", "the_spirit_speaks"),
                false, "report_to_hanna", 5));

        add(new ZoneDef("avenue_marees",
                "L'Avenue des Marees", "Tide Avenue",
                "Le vieux port est sous l'eau, et ce qui s'y est noye n'a pas l'intention de rester tranquille.",
                "The old harbour is flooded, and what drowned there has no intention of resting.",
                12, 18,
                List.of("rotting_fodder", "crypt_walker", "loathsome_creeper", "rotten_rob",
                        "electric_eel", "scarlet_screamer", "spellwrit_screamer", "fear_of_drowning",
                        "kestrel_blackstorm", "sergeant_skullsplitter", "tide_kraken", "the_harvest_lord"),
                List.of("captain_marrow", "fisherman_pike"),
                List.of("tide_trouble", "screaming_shores", "drowning_fears", "the_eel_deal",
                        "pikes_net", "the_harvest_lord_quest", "kraken_wakes"),
                false, "swiftarrow_ambush", 12));

        add(new ZoneDef("chemin_colosses",
                "Le Chemin des Colosses", "Colossus Road",
                "La grande avenue des statues : leurs gardiens de glace se sont reveilles.",
                "The great avenue of statues: their ice guardians have awakened.",
                15, 19,
                List.of("ice_troll", "marauder_imp", "colossus_sentinel", "night_shade", "rune_marauder"),
                List.of("warden_ashwind"),
                List.of("road_of_colossi", "cold_shoulders", "sentinels_stone", "shades_of_night",
                        "ashes_of_the_road"),
                false, "the_harvest_lord_quest", 15));

        add(new ZoneDef("grotte_hantee",
                "La Grotte Hantee", "Haunted Hollow",
                "Un boyau de pierre noire ou le Grand Necromancien rassemble ses os.",
                "A tunnel of black stone where the Grand Necromancer gathers his bones.",
                17, 22,
                List.of("corsair_bones", "bone_legion", "hollow_lord", "grand_necromancer"),
                List.of("lost_apprentice"),
                List.of("hollow_beckons", "lost_apprentice_quest", "the_hollow_lord", "end_of_the_hollow"),
                true, "ashes_of_the_road", 17));
    }
}
