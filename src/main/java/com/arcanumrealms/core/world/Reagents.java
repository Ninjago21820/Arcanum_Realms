package com.arcanumrealms.core.world;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Nom des reagents ramasses sur les creatures.
 *
 * <p>Le moteur est independant de Minecraft : il ne peut pas demander le nom
 * traduit d'un objet du jeu. Cette petite table donne donc un nom lisible aux
 * seuls reagents utilises par les quetes de recolte.
 */
public final class Reagents {

    private record Names(String fr, String en, boolean plural) {}

    private static final Map<String, Names> DEFS = new LinkedHashMap<>();

    private Reagents() {}

    static {
        add("minecraft:bone", "os", "bones", true);
        add("minecraft:rotten_flesh", "chair pourrissante", "rotten flesh", true);
        add("minecraft:amethyst_shard", "eclat d'amethyste", "amethyst shard", false);
        add("minecraft:blaze_powder", "poudre de blaze", "blaze powder", true);
        add("minecraft:blaze_rod", "barre de blaze", "blaze rod", false);
        add("minecraft:prismarine_shard", "eclat de prismarine", "prismarine shard", false);
        add("minecraft:heart_of_the_sea", "coeur de la mer", "heart of the sea", true);
        add("minecraft:blue_ice", "glace bleue", "blue ice", true);
        add("minecraft:diamond", "diamant", "diamond", false);
        add("minecraft:netherite_scrap", "debris de netherite", "netherite scrap", false);
        add("minecraft:echo_shard", "eclat d'echo", "echo shard", false);
        add("minecraft:string", "fil", "string", true);
        add("minecraft:wheat", "ble", "wheat", true);
        add("minecraft:gold_nugget", "pepite d'or", "gold nugget", false);
        add("minecraft:gold_ingot", "lingot d'or", "gold ingot", false);
    }

    private static void add(String id, String fr, String en, boolean plural) {
        DEFS.put(id, new Names(fr, en, plural));
    }

    /** Nom lisible d'un reagent, ou l'identifiant si on ne le connait pas. */
    public static String name(String itemId, boolean french) {
        Names names = DEFS.get(itemId);
        if (names == null) return itemId;
        return french ? names.fr() : names.en();
    }

    public static boolean isPlural(String itemId) {
        Names names = DEFS.get(itemId);
        return names != null && names.plural();
    }
}
