package com.arcanumrealms.core.world;

/**
 * Un objet lache par une creature.
 *
 * @param kind    equipement du mod ou objet Minecraft (reagent)
 * @param id      identifiant de l'equipement (Gears) ou de l'objet Minecraft ("minecraft:bone")
 * @param count   quantite
 */
public record LootDrop(LootKind kind, String id, int count) {

    public static LootDrop gear(String gearId, int count) {
        return new LootDrop(LootKind.GEAR, gearId, count);
    }

    public static LootDrop item(String itemId, int count) {
        return new LootDrop(LootKind.ITEM, itemId, count);
    }

    public enum LootKind { GEAR, ITEM }
}
