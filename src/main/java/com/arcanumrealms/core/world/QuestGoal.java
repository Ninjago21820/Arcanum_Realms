package com.arcanumrealms.core.world;

/**
 * Objectif d'une quete. Le wiki decrit les quetes exactement ainsi :
 * "Defeat Fire Elves and Collect a Fire Shard in Firecat Alley", "Talk to Bernie",
 * "Visit Festival Park".
 */
public sealed interface QuestGoal {

    /** Cle unique de l'objectif dans sa quete (sert a stocker la progression). */
    String key();

    /** Texte affiche au joueur. */
    String describe(boolean french);

    /** Vaincre N creatures (par identifiant de creature) ou par classification ("Elf"). */
    record Defeat(String creatureId, int count) implements QuestGoal {
        @Override public String key() { return "defeat:" + creatureId; }
        @Override public String describe(boolean french) {
            return (french ? "Vaincre " : "Defeat ") + count + " " + creatureId;
        }
    }

    /** Ramasser N objets. */
    record Collect(String itemId, int count) implements QuestGoal {
        @Override public String key() { return "collect:" + itemId; }
        @Override public String describe(boolean french) {
            return (french ? "Recuperer " : "Collect ") + count + " " + itemId;
        }
    }

    /** Parler a un PNJ. */
    record Talk(String npcId) implements QuestGoal {
        @Override public String key() { return "talk:" + npcId; }
        @Override public String describe(boolean french) {
            return (french ? "Parler a " : "Talk to ") + npcId;
        }
    }

    /** Se rendre dans une zone. */
    record Visit(String zoneId) implements QuestGoal {
        @Override public String key() { return "visit:" + zoneId; }
        @Override public String describe(boolean french) {
            return (french ? "Se rendre a " : "Visit ") + zoneId;
        }
    }
}
