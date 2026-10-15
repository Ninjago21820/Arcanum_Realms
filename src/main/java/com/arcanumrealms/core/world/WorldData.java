package com.arcanumrealms.core.world;

import com.arcanumrealms.core.spell.SpellBook;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verification du contenu du monde.
 *
 * <p>Le contenu est ecrit a la main d'apres le wiki : cette classe s'assure
 * qu'aucune reference ne pointe dans le vide (une creature qui invoque un sort
 * inexistant, une quete qui parle a un PNJ qui n'existe pas, un butin qui donne
 * un equipement inconnu...). Elle est appelee au demarrage du serveur et par les
 * tests, ce qui evite les mauvaises surprises en partie.
 */
public final class WorldData {

    private WorldData() {}

    public static List<String> validate() {
        List<String> problems = new ArrayList<>();
        checkZones(problems);
        checkNpcs(problems);
        checkBestiary(problems);
        checkLoot(problems);
        checkGears(problems);
        problems.addAll(Quests.validate());
        checkQuestGraph(problems);
        return problems;
    }

    private static void checkZones(List<String> problems) {
        for (ZoneDef zone : Zones.all()) {
            if (zone.minLevel() > zone.maxLevel()) {
                problems.add("Zone " + zone.id() + " : plage de niveaux inversee");
            }
            for (String creature : zone.creatures()) {
                if (Bestiary.find(creature).isEmpty()) {
                    problems.add("Zone " + zone.id() + " : creature inconnue " + creature);
                }
            }
            for (String npc : zone.npcs()) {
                if (Npcs.find(npc).isEmpty()) {
                    problems.add("Zone " + zone.id() + " : PNJ inconnu " + npc);
                }
            }
            for (String quest : zone.quests()) {
                if (Quests.find(quest).isEmpty()) {
                    problems.add("Zone " + zone.id() + " : quete inconnue " + quest);
                }
            }
            if (zone.unlockQuest() != null && Quests.find(zone.unlockQuest()).isEmpty()) {
                problems.add("Zone " + zone.id() + " : quete de deblocage inconnue " + zone.unlockQuest());
            }
        }
    }

    private static void checkNpcs(List<String> problems) {
        for (NpcDef npc : Npcs.all()) {
            if (npc.zoneId() == null || Zones.find(npc.zoneId()).isEmpty()) {
                problems.add("PNJ " + npc.id() + " : zone inconnue " + npc.zoneId());
            }
            if (npc.entity() == null || npc.entity().isEmpty()) {
                problems.add("PNJ " + npc.id() + " : aucune entite");
            }
            if (npc.isTrainer() && npc.school() == null) {
                problems.add("PNJ " + npc.id() + " : professeur sans ecole");
            }
            for (String quest : npc.givesQuests()) {
                if (Quests.find(quest).isEmpty()) {
                    problems.add("PNJ " + npc.id() + " : quete donnee inconnue " + quest);
                } else if (!npc.id().equals(Quests.get(quest).giverNpc())) {
                    problems.add("PNJ " + npc.id() + " : annonce donner " + quest
                            + " mais le donneur est " + Quests.get(quest).giverNpc());
                }
            }
            for (String quest : npc.endsQuests()) {
                if (Quests.find(quest).isEmpty()) {
                    problems.add("PNJ " + npc.id() + " : quete rendue inconnue " + quest);
                } else if (!npc.id().equals(Quests.get(quest).handInNpc())) {
                    problems.add("PNJ " + npc.id() + " : annonce rendre " + quest
                            + " mais le remettant est " + Quests.get(quest).handInNpc());
                }
            }
            if (npc.greeting().isEmpty()) {
                problems.add("PNJ " + npc.id() + " : aucun dialogue d'accueil");
            }
        }
    }

    private static void checkBestiary(List<String> problems) {
        for (WorldCreature creature : Bestiary.all()) {
            if (creature.health() <= 0) problems.add("Creature " + creature.id() + " : PV invalides");
            if (creature.rank() < 1) problems.add("Creature " + creature.id() + " : rang invalide");
            if (creature.deck().isEmpty()) problems.add("Creature " + creature.id() + " : deck vide");
            for (String spell : creature.deck()) {
                if (SpellBook.find(spell).isEmpty()) {
                    problems.add("Creature " + creature.id() + " : sort inconnu " + spell);
                }
            }
            for (String zone : creature.zoneIds()) {
                if (Zones.find(zone).isEmpty()) {
                    problems.add("Creature " + creature.id() + " : zone inconnue " + zone);
                }
            }
            if (LootTables.get(creature.lootTable()) == null) {
                problems.add("Creature " + creature.id() + " : table de butin inconnue");
            }
            for (var entry : creature.resist().entrySet()) {
                if (entry.getValue() < 0 || entry.getValue() > 90) {
                    problems.add("Creature " + creature.id() + " : resistance invalide " + entry.getValue());
                }
            }
            for (var entry : creature.boost().entrySet()) {
                if (entry.getValue() < 0 || entry.getValue() > 90) {
                    problems.add("Creature " + creature.id() + " : faiblesse invalide " + entry.getValue());
                }
            }
        }
    }

    private static void checkLoot(List<String> problems) {
        Set<String> lootIds = new HashSet<>();
        for (LootTable table : LootTables.all()) {
            if (!lootIds.add(table.id())) problems.add("Table de butin en double : " + table.id());
            if (table.goldMax() < table.goldMin()) {
                problems.add("Table " + table.id() + " : fourchette d'or inversee");
            }
            for (LootTable.Entry entry : table.entries()) {
                if (entry.min() <= 0 || entry.max() < entry.min()) {
                    problems.add("Table " + table.id() + " : quantite invalide pour " + entry.id());
                }
                if (entry.chance() < 0 || entry.chance() > 100) {
                    problems.add("Table " + table.id() + " : chance invalide pour " + entry.id());
                }
                if (entry.kind() == LootDrop.LootKind.GEAR && Gears.find(entry.id()).isEmpty()) {
                    problems.add("Table " + table.id() + " : equipement inconnu " + entry.id());
                }
                if (entry.kind() == LootDrop.LootKind.ITEM
                        && !entry.id().contains(":") && !entry.id().startsWith("minecraft:")) {
                    problems.add("Table " + table.id() + " : identifiant d'objet douteux " + entry.id());
                }
            }
        }
        for (WorldCreature creature : Bestiary.all()) {
            if (LootTables.all().stream().noneMatch(t -> t.id().equals(creature.lootTable()))) {
                problems.add("Creature " + creature.id() + " : butin introuvable " + creature.lootTable());
            }
        }
    }

    private static void checkGears(List<String> problems) {
        for (GearDef gear : Gears.all()) {
            if (gear.level() < 1) problems.add("Equipement " + gear.id() + " : niveau invalide");
            if (gear.powerPipChance() < 0) problems.add("Equipement " + gear.id() + " : pip negatif");
            if (gear.damageBonus().values().stream().anyMatch(v -> v <= 0)) {
                problems.add("Equipement " + gear.id() + " : bonus de degats nul");
            }
        }
    }

    /** Verifie que la chaine principale est continue et que les niveaux montent. */
    private static void checkQuestGraph(List<String> problems) {
        Set<String> reachable = new HashSet<>();
        String current = Quests.firstMainQuest();
        int previousLevel = 0;
        int guard = 0;
        while (current != null && guard++ < 200) {
            QuestDef quest = Quests.find(current).orElse(null);
            if (quest == null) {
                problems.add("Chaine principale : quete introuvable " + current);
                return;
            }
            if (!reachable.add(current)) {
                problems.add("Chaine principale : boucle detectee sur " + current);
                return;
            }
            if (quest.levelRequired() < previousLevel) {
                problems.add("Chaine principale : " + current + " demande un niveau plus bas que la precedente");
            }
            previousLevel = quest.levelRequired();
            if (quest.side()) problems.add("Chaine principale : " + current + " est marquee annexe");
            current = quest.leadsTo();
        }
        // Les quetes annexes doivent tout de meme etre rattachees a une zone ou a une epreuve d'ecole.
        for (QuestDef quest : Quests.all()) {
            if (!reachable.contains(quest.id()) && !quest.side()) {
                problems.add("Quete " + quest.id() + " : hors de la chaine principale et non annexe");
            }
        }
    }
}
