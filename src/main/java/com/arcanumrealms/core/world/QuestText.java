package com.arcanumrealms.core.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Textes lisibles des quetes : objectifs et recompenses, dans les deux langues.
 *
 * <p>Ces chaines servent a l'interface (journal de quetes, dialogue) et a la
 * documentation generee.
 */
public final class QuestText {

    private QuestText() {}

    /** "Vaincre 3 Chasseurs Elfe de Braise" */
    public static String goal(QuestGoal goal, boolean french) {
        if (goal instanceof QuestGoal.Defeat d) {
            String name = Bestiary.find(d.creatureId()).map(c -> c.name(french)).orElse(d.creatureId());
            if (d.count() > 1) name = pluralize(name, french);
            return (french ? "Vaincre " : "Defeat ") + d.count() + " " + name;
        }
        if (goal instanceof QuestGoal.Collect c) {
            String name = Reagents.name(c.itemId(), french);
            String word = c.count() > 1 ? pluralize(name, french) : name;
            return (french ? "Recuperer " : "Collect ") + c.count() + " " + word;
        }
        if (goal instanceof QuestGoal.Talk t) {
            String name = Npcs.find(t.npcId()).map(n -> n.name(french)).orElse(t.npcId());
            return (french ? "Parler a " : "Talk to ") + name;
        }
        if (goal instanceof QuestGoal.Visit v) {
            String name = Zones.find(v.zoneId()).map(z -> z.name(french)).orElse(v.zoneId());
            return french ? "Se rendre " + aZone(name) : "Visit " + name;
        }
        return goal.describe(french);
    }

    /**
     * Pluriel d'un groupe nominal ("Chasseur Elfe de Braise" -&gt; "Chasseurs Elfes de Braise",
     * "poudre de blaze" -&gt; "poudres de blaze").
     *
     * <p>En francais le premier mot passe au pluriel, ainsi que le suivant
     * lorsqu'il forme un groupe avec lui ; les complements introduits par "de"
     * ou "d'" restent au singulier. En anglais, on pluralise le dernier mot.
     */
    static String pluralize(String name, boolean french) {
        String[] words = name.split(" ");
        if (words.length == 0) return name;
        if (french) {
            words[0] = pluralWord(words[0], true);
            if (words.length > 1 && !isPreposition(words[1]) && words[1].endsWith("e")) {
                words[1] = pluralWord(words[1], true);
            }
        } else {
            words[words.length - 1] = pluralWord(words[words.length - 1], false);
        }
        return String.join(" ", words);
    }

    /**
     * Contraction de "a" avec l'article du nom de zone : "a Le Sentier" devient
     * "au Sentier", "a Les Marees" devient "aux Marees", etc.
     */
    static String aZone(String name) {
        if (name.startsWith("Le ")) return "au " + name.substring(3);
        if (name.startsWith("Les ")) return "aux " + name.substring(4);
        if (name.startsWith("La ")) return "a la " + name.substring(3);
        if (name.startsWith("L'")) return "a l'" + name.substring(2);
        return "a " + name;
    }

    private static boolean isPreposition(String word) {
        String lower = word.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("de") || lower.startsWith("d'") || lower.startsWith("du") || lower.startsWith("des");
    }

    private static final java.util.Map<String, String> IRREGULAR = java.util.Map.ofEntries(
            java.util.Map.entry("man", "men"), java.util.Map.entry("woman", "women"),
            java.util.Map.entry("child", "children"), java.util.Map.entry("mouse", "mice"),
            java.util.Map.entry("wolf", "wolves"), java.util.Map.entry("elf", "elves"),
            java.util.Map.entry("dwarf", "dwarves"), java.util.Map.entry("foot", "feet"),
            java.util.Map.entry("tooth", "teeth"), java.util.Map.entry("undead", "undead"));

    /** Reprend la majuscule du mot d'origine ("Man" -&gt; "Men"). */
    private static String matchCase(String source, String result) {
        if (!source.isEmpty() && Character.isUpperCase(source.charAt(0))) {
            return Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        return result;
    }

    private static String pluralWord(String word, boolean french) {
        if (word.isEmpty()) return word;
        String lower = word.toLowerCase(java.util.Locale.ROOT);
        if (!french) {
            String irregular = IRREGULAR.get(lower);
            if (irregular != null) return matchCase(word, irregular);
        }
        char last = lower.charAt(lower.length() - 1);
        if (last == 's' || last == 'x' || last == 'z') return word;
        if (lower.endsWith("y")) return word.substring(0, word.length() - 1) + "ies";
        if (lower.endsWith("sh") || lower.endsWith("ch")) return word + "es";
        return word + "s";
    }

    public static List<String> goals(QuestDef quest, boolean french) {
        List<String> out = new ArrayList<>();
        for (QuestGoal goal : quest.goals()) out.add(goal(goal, french));
        return out;
    }

    /** "550 XP, 70 or, Baguette des Marees" */
    public static String rewards(QuestDef quest, boolean french) {
        List<String> parts = new ArrayList<>();
        if (quest.rewardXp() > 0) parts.add(quest.rewardXp() + " XP");
        if (quest.rewardGold() > 0) parts.add(quest.rewardGold() + (french ? " or" : " gold"));
        if (quest.rewardSpell() != null) {
            parts.add(com.arcanumrealms.core.spell.SpellBook.find(quest.rewardSpell())
                    .map(s -> s.name(french)).orElse(quest.rewardSpell()));
        }
        if (quest.rewardItem() != null) {
            parts.add(gearName(quest.rewardItem(), french));
        }
        for (String choice : quest.rewardChoices()) {
            parts.add(gearName(choice, french));
        }
        return String.join(", ", parts);
    }

    public static String gearName(String gearId, boolean french) {
        return Gears.find(gearId).map(g -> g.name(french)).orElse(gearId);
    }

    /** Resume d'une quete sur une ligne, pour le journal. */
    public static String line(QuestDef quest, boolean french) {
        return (quest.side() ? (french ? "[Annexe] " : "[Side] ") : "") + quest.name(french)
                + " - " + quest.summary(french);
    }
}
