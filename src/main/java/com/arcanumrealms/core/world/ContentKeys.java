package com.arcanumrealms.core.world;

import com.arcanumrealms.core.spell.Spell;

import java.util.ArrayList;
import java.util.List;

/**
 * Toutes les cles de traduction du contenu.
 *
 * <p>C'est le seul endroit ou le nom des cles est ecrit : le moteur, le
 * generateur de langue et le paquet de textes l'utilisent tous, donc ils ne
 * peuvent pas diverger.
 *
 * <p>Convention : {@code <type>.arcanum_realms.<id>[.<champ>][.<index>]}. Un
 * joueur (ou un pack de ressources) peut redefinir n'importe laquelle de ces
 * cles dans {@code assets/arcanum_realms/lang/fr_fr.json} pour remplacer un
 * dialogue, un nom de PNJ, un nom de sort, etc.
 */
public final class ContentKeys {

    /** Noms des quatre moments d'un dialogue. */
    public static final String GREETING = "greeting";
    public static final String IN_PROGRESS = "inProgress";
    public static final String COMPLETION = "completion";
    public static final String FAREWELL = "farewell";
    public static final List<String> SLOTS = List.of(GREETING, IN_PROGRESS, COMPLETION, FAREWELL);

    private ContentKeys() {}

    // ------------------------------------------------------------- PNJ

    public static String npc(String npcId) {
        return "npc.arcanum_realms." + npcId;
    }

    public static String npcTitle(String npcId) {
        return npc(npcId) + ".title";
    }

    public static String npcDescription(String npcId) {
        return npc(npcId) + ".description";
    }

    /**
     * Une replique de dialogue : {@code dialogue.arcanum_realms.<pnj>.<moment>.<numero>}.
     *
     * @param npcId  identifiant du PNJ
     * @param slot   l'un des quatre moments ({@link #SLOTS})
     * @param index  numero de la replique dans ce moment, a partir de 0
     */
    public static String dialogue(String npcId, String slot, int index) {
        return "dialogue.arcanum_realms." + npcId + "." + slot + "." + index;
    }

    // ----------------------------------------------------------- sorts

    public static String spell(String spellId) {
        return "spell.arcanum_realms." + spellId + ".name";
    }

    public static String spellDescription(String spellId) {
        return "spell.arcanum_realms." + spellId + ".desc";
    }

    // ------------------------------------------------------- creatures

    public static String creature(String creatureId) {
        return "creature.arcanum_realms." + creatureId;
    }

    // -------------------------------------------------- monde et objets

    public static String zone(String zoneId) {
        return "zone.arcanum_realms." + zoneId;
    }

    public static String gear(String gearId) {
        return "gear.arcanum_realms." + gearId;
    }

    public static String gearSlot(String slotName) {
        return "gear.arcanum_realms.slot." + slotName.toLowerCase(java.util.Locale.ROOT);
    }

    // ----------------------------------------------------------- quetes

    public static String quest(String questId) {
        return "quest.arcanum_realms." + questId;
    }

    public static String questSummary(String questId) {
        return quest(questId) + ".summary";
    }

    /** Objectif numero {@code index} d'une quete. */
    public static String questGoal(String questId, int index) {
        return quest(questId) + ".goal." + index;
    }

    /** Ligne des recompenses d'une quete. */
    public static String questRewards(String questId) {
        return quest(questId) + ".rewards";
    }

    // ------------------------------------------------------ fabriques

    /** Nom d'un PNJ, dans la langue demandee. */
    public static ContentLine npcName(NpcDef npc, boolean french) {
        return ContentLine.of(npc(npc.id()), npc.name(french));
    }

    public static ContentLine npcTitle(NpcDef npc, boolean french) {
        return ContentLine.of(npcTitle(npc.id()), npc.title(french));
    }

    /** Nom complet "Prenom Nom, titre", pret a afficher en tete de dialogue. */
    public static ContentLine npcHeadline(NpcDef npc, boolean french) {
        String name = npc.name(french);
        String title = npc.title(french);
        String text = title == null || title.isBlank() ? name : name + ", " + title;
        return ContentLine.of(npc(npc.id()), text);
    }

    /** Les repliques d'un moment de dialogue, avec leurs cles. */
    public static List<ContentLine> dialogue(NpcDef npc, String slot, boolean french) {
        Dialogue dialogue = switch (slot) {
            case IN_PROGRESS -> npc.inProgress();
            case COMPLETION -> npc.completion();
            case FAREWELL -> npc.farewell();
            default -> npc.greeting();
        };
        List<ContentLine> out = new ArrayList<>(dialogue.size());
        List<String> texts = dialogue.texts(french);
        for (int i = 0; i < texts.size(); i++) {
            out.add(ContentLine.of(dialogue(npc.id(), slot, i), texts.get(i)));
        }
        return out;
    }

    public static ContentLine spellName(Spell spell, boolean french) {
        return ContentLine.of(spell(spell.id()), spell.name(french));
    }

    public static ContentLine creatureName(WorldCreature creature, boolean french) {
        return ContentLine.of(creature(creature.id()), creature.name(french));
    }

    public static ContentLine zoneName(ZoneDef zone, boolean french) {
        return ContentLine.of(zone(zone.id()), zone.name(french));
    }

    public static ContentLine gearName(GearDef gear, boolean french) {
        return ContentLine.of(gear(gear.id()), gear.name(french));
    }

    public static ContentLine questName(QuestDef quest, boolean french) {
        return ContentLine.of(quest(quest.id()), quest.name(french));
    }

    public static ContentLine questSummaryLine(QuestDef quest, boolean french) {
        return ContentLine.of(questSummary(quest.id()), quest.summary(french));
    }

    /** Les objectifs d'une quete, avec leurs cles. */
    public static List<ContentLine> questGoals(QuestDef quest, boolean french) {
        List<ContentLine> out = new ArrayList<>(quest.goals().size());
        for (int i = 0; i < quest.goals().size(); i++) {
            out.add(ContentLine.of(questGoal(quest.id(), i), QuestText.goal(quest.goals().get(i), french)));
        }
        return out;
    }

    public static ContentLine questRewardLine(QuestDef quest, boolean french) {
        return ContentLine.of(questRewards(quest.id()), QuestText.rewards(quest, french));
    }
}
