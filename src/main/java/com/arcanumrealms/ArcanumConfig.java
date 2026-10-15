package com.arcanumrealms;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Configuration du mod (fichier {@code config/arcanum_realms-common.toml}).
 *
 * <p>Tout ce qui touche a l'equilibrage est ici : les valeurs par defaut
 * reproduisent Wizard101, mais rien n'empeche votre serveur d'avoir sa propre sauce.
 */
public final class ArcanumConfig {

    public static final ForgeConfigSpec SPEC;

    // --- declenchement des duels ---
    public static final ForgeConfigSpec.BooleanValue AUTO_TRIGGER;
    public static final ForgeConfigSpec.IntValue TRIGGER_RADIUS;
    public static final ForgeConfigSpec.IntValue TRIGGER_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue MAX_CREATURES;
    public static final ForgeConfigSpec.IntValue MAX_PLAYERS;

    // --- regles de duel ---
    public static final ForgeConfigSpec.BooleanValue FIZZLE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue CRITICAL_ENABLED;
    public static final ForgeConfigSpec.DoubleValue DAMAGE_SCALE;
    public static final ForgeConfigSpec.DoubleValue HEAL_SCALE;
    public static final ForgeConfigSpec.IntValue PLANNING_SECONDS;
    public static final ForgeConfigSpec.IntValue RESOLVE_DELAY_TICKS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_FLEE;
    public static final ForgeConfigSpec.IntValue MAX_ROUNDS;
    public static final ForgeConfigSpec.IntValue LEASH_RADIUS;

    // --- sorciers ---
    public static final ForgeConfigSpec.IntValue BASE_POWER_PIP_CHANCE;
    public static final ForgeConfigSpec.IntValue CRIT_RATING_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue ACCURACY_BONUS;
    public static final ForgeConfigSpec.IntValue BASE_RESIST;
    public static final ForgeConfigSpec.IntValue BASE_DAMAGE_BONUS;
    public static final ForgeConfigSpec.IntValue DECK_SIZE_LIMIT;
    public static final ForgeConfigSpec.IntValue XP_PER_CREATURE_LEVEL;
    public static final ForgeConfigSpec.IntValue XP_TO_LEVEL_UP;
    public static final ForgeConfigSpec.BooleanValue AUTO_LEARN_SPELLS;
    public static final ForgeConfigSpec.BooleanValue INVULNERABLE_DURING_DUEL;
    public static final ForgeConfigSpec.BooleanValue RESTORE_HEALTH_AFTER_DUEL;
    public static final ForgeConfigSpec.DoubleValue DEFEAT_DAMAGE;

    // --- arene ---
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ARENA_ENCOUNTERS;
    public static final ForgeConfigSpec.IntValue ARENA_RADIUS;

    /** Comportement des personnages (PNJ). */
    public static final ForgeConfigSpec.BooleanValue NPC_WANDER;
    public static final ForgeConfigSpec.IntValue NPC_WANDER_RADIUS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Declenchement des duels").push("duel_trigger");
        AUTO_TRIGGER = b.comment("Lance automatiquement un duel quand un sorcier s'approche de creatures hostiles.")
                .define("autoTrigger", true);
        TRIGGER_RADIUS = b.comment("Distance (en blocs) a laquelle une creature provoque un duel.")
                .defineInRange("triggerRadius", 12, 4, 64);
        TRIGGER_COOLDOWN_SECONDS = b.comment("Delai minimum entre deux duels automatiques pour un meme joueur.")
                .defineInRange("triggerCooldownSeconds", 20, 0, 600);
        MAX_CREATURES = b.comment("Nombre maximum de creatures dans un duel.")
                .defineInRange("maxCreatures", 4, 1, 8);
        MAX_PLAYERS = b.comment("Nombre maximum de sorciers dans un duel.")
                .defineInRange("maxPlayers", 4, 1, 8);
        b.pop();

        b.comment("Regles du duel (valeurs de Wizard101 par defaut)").push("duel_rules");
        FIZZLE_ENABLED = b.comment("Active les echecs de sort (fizzle).")
                .define("fizzleEnabled", true);
        CRITICAL_ENABLED = b.comment("Active les coups critiques.")
                .define("criticalEnabled", true);
        DAMAGE_SCALE = b.comment("Multiplicateur global des degats (1.0 = valeurs de Wizard101).")
                .defineInRange("damageScale", 1.0, 0.1, 10.0);
        HEAL_SCALE = b.comment("Multiplicateur global des soins.")
                .defineInRange("healScale", 1.0, 0.1, 10.0);
        PLANNING_SECONDS = b.comment("Duree de la phase de choix d'une carte, en secondes.")
                .defineInRange("planningSeconds", 60, 10, 600);
        RESOLVE_DELAY_TICKS = b.comment("Petite pause avant le round suivant, pour lire le journal (en ticks).")
                .defineInRange("resolveDelayTicks", 40, 0, 400);
        ALLOW_FLEE = b.comment("Autorise l'abandon en plein duel.")
                .define("allowFlee", true);
        MAX_ROUNDS = b.comment("Garde-fou : au dela de ce nombre de rounds, match nul.")
                .defineInRange("maxRounds", 60, 5, 500);
        LEASH_RADIUS = b.comment("Distance maximum (en blocs) par rapport au cercle avant de fuir le duel.")
                .defineInRange("leashRadius", 32, 4, 256);
        b.pop();

        b.comment("Progression des sorciers").push("wizard");
        BASE_POWER_PIP_CHANCE = b.comment("Chance de pip de pouvoir de base (%) - dans Wizard101 elle vient de l'equipement.")
                .defineInRange("basePowerPipChance", 25, 0, 100);
        CRIT_RATING_PER_LEVEL = b.comment("Score de critique gagne par niveau (0 = uniquement via equipement).")
                .defineInRange("critRatingPerLevel", 2, 0, 50);
        ACCURACY_BONUS = b.comment("Bonus de precision (%) donne aux sorciers.")
                .defineInRange("accuracyBonus", 0, -100, 100);
        BASE_RESIST = b.comment("Resistance universelle de base (%) des sorciers.")
                .defineInRange("baseResist", 0, 0, 90);
        BASE_DAMAGE_BONUS = b.comment("Bonus de degats universel de base (%) des sorciers.")
                .defineInRange("baseDamageBonus", 0, 0, 500);
        DECK_SIZE_LIMIT = b.comment("Nombre maximum de cartes dans un deck.")
                .defineInRange("deckSizeLimit", 40, 8, 200);
        XP_PER_CREATURE_LEVEL = b.comment("Points d'experience gagnes par niveau de creature vaincue.")
                .defineInRange("xpPerCreatureLevel", 10, 1, 1000);
        XP_TO_LEVEL_UP = b.comment("Experience necessaire pour passer un niveau (multipliee par le niveau).")
                .defineInRange("xpToLevelUp", 100, 10, 100000);
        AUTO_LEARN_SPELLS = b.comment("Apprend automatiquement les sorts debloques par le niveau.")
                .define("autoLearnSpells", true);
        INVULNERABLE_DURING_DUEL = b.comment("Les participants sont proteges des degats du monde pendant un duel.")
                .define("invulnerableDuringDuel", true);
        RESTORE_HEALTH_AFTER_DUEL = b.comment("Restaure la vie des sorciers a la fin du duel selon leur etat dans le duel.")
                .define("restoreHealthAfterDuel", true);
        DEFEAT_DAMAGE = b.comment("Degats infliges (en demi-coeurs) a un sorcier vaincu.")
                .defineInRange("defeatDamage", 6.0, 0.0, 20.0);
        b.pop();

        b.comment("Arenes de duel").push("arena");
        ARENA_ENCOUNTERS = b.comment("Rencontres declenchees par un cercle arcanique.",
                        "Chaque entree est une liste d'identifiants de creatures separees par des virgules,",
                        "par exemple : \"ember_imp,static_wisp\" ou \"cinder_tyrant\".")
                .defineListAllowEmpty(List.of("encounters"), () -> List.of(
                        "ember_imp,ember_imp",
                        "static_wisp,bramble_thrall",
                        "glacier_hound,rune_gnome",
                        "grave_shade,balance_scribe",
                        "cinder_tyrant"), o -> o instanceof String);
        ARENA_RADIUS = b.comment("Rayon du cercle de duel, en blocs.")
                .defineInRange("arenaRadius", 6, 2, 32);
        b.pop();

        b.comment("Comportement des personnages (PNJ)").push("npc");
        NPC_WANDER = b.comment("Les PNJ font quelques pas autour de leur poste (comme des villageois).",
                        "Desactivez pour qu'ils restent strictement immobiles, comme dans Wizard101.")
                .define("wander", true);
        NPC_WANDER_RADIUS = b.comment("Distance maximale, en blocs, a laquelle un PNJ s'eloigne de son poste.")
                .defineInRange("wanderRadius", 6, 1, 32);
        b.pop();

        SPEC = b.build();
    }

    private ArcanumConfig() {}

    /** Lecture sure de la promenade des PNJ (valeur par defaut si la config n'est pas chargee). */
    public static boolean wanderEnabled() {
        try {
            return NPC_WANDER.get();
        } catch (Throwable t) {
            return true;
        }
    }

    /** Rayon de promenade des PNJ, en blocs. */
    public static int wanderRadius() {
        try {
            return NPC_WANDER_RADIUS.get();
        } catch (Throwable t) {
            return 6;
        }
    }

    /** Lecture sure de la limite de deck cote client (valeur par defaut si la config n'est pas encore chargee). */
    public static int deckSizeLimitClient() {
        try {
            return DECK_SIZE_LIMIT.get();
        } catch (Throwable t) {
            return 40;
        }
    }
}
