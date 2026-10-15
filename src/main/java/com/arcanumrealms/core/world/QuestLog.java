package com.arcanumrealms.core.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Journal de quetes d'un joueur : quetes acceptees, progression des objectifs,
 * quetes terminees.
 *
 * <p>La classe est volontairement independante de Minecraft : le serveur la
 * sauvegarde dans les donnees du joueur et la nourrit avec les evenements du
 * jeu (creature vaincue, objet ramasse, PNJ rencontre, zone visitee).
 */
public final class QuestLog {

    private final List<String> active = new ArrayList<>();
    private final Set<String> completed = new LinkedHashSet<>();
    private final Map<String, Map<String, Integer>> progress = new LinkedHashMap<>();

    public List<String> active() { return Collections.unmodifiableList(active); }

    public Set<String> completed() { return Collections.unmodifiableSet(completed); }

    public Map<String, Map<String, Integer>> rawProgress() {
        Map<String, Map<String, Integer>> copy = new LinkedHashMap<>();
        progress.forEach((quest, goals) -> copy.put(quest, new LinkedHashMap<>(goals)));
        return copy;
    }

    public boolean isActive(String questId) { return active.contains(questId); }

    public boolean isCompleted(String questId) { return completed.contains(questId); }

    public int progressOf(String questId, String goalKey) {
        return progress.getOrDefault(questId, Map.of()).getOrDefault(goalKey, 0);
    }

    /** Un objectif est termine quand son compteur atteint le seuil demande. */
    public boolean goalDone(String questId, QuestGoal goal) {
        return progressOf(questId, goal.key()) >= goalCount(goal);
    }

    private static int goalCount(QuestGoal goal) {
        if (goal instanceof QuestGoal.Defeat d) return d.count();
        if (goal instanceof QuestGoal.Collect c) return c.count();
        return 1;
    }

    /** Vrai si tous les objectifs de la quete sont remplis. */
    public boolean readyToHandIn(String questId) {
        QuestDef quest = Quests.find(questId).orElse(null);
        if (quest == null || !isActive(questId)) return false;
        for (QuestGoal goal : quest.goals()) {
            if (!goalDone(questId, goal)) return false;
        }
        return true;
    }

    /** Verifie les prerequis : quete precedente terminee et niveau suffisant. */
    public boolean canStart(String questId, int playerLevel) {
        QuestDef quest = Quests.find(questId).orElse(null);
        if (quest == null) return false;
        if (isActive(questId) || isCompleted(questId)) return false;
        if (playerLevel < quest.levelRequired()) return false;
        return quest.prequest() == null || isCompleted(quest.prequest());
    }

    public boolean start(String questId) {
        if (Quests.find(questId).isEmpty() || isActive(questId) || isCompleted(questId)) return false;
        active.add(questId);
        progress.put(questId, new LinkedHashMap<>());
        return true;
    }

    /** Termine la quete : la progression est oubliee, la quete reste au palmares. */
    public boolean complete(String questId) {
        if (!active.remove(questId)) return false;
        progress.remove(questId);
        completed.add(questId);
        return true;
    }

    /** Abandonne une quete : sa progression est perdue, elle peut etre reprise. */
    public boolean abandon(String questId) {
        if (!active.remove(questId)) return false;
        progress.remove(questId);
        return true;
    }

    /** Quetes que le joueur peut rendre maintenant (tous objectifs remplis). */
    public List<String> handInReady() {
        List<String> out = new ArrayList<>();
        for (String questId : active) {
            if (readyToHandIn(questId)) out.add(questId);
        }
        return out;
    }

    // ------------------------------------------------------------ evenements

    private boolean advance(String questId, String goalKey, int amount) {
        QuestDef quest = Quests.find(questId).orElse(null);
        if (quest == null) return false;
        for (QuestGoal goal : quest.goals()) {
            if (!goal.key().equals(goalKey)) continue;
            int max = goalCount(goal);
            int current = progressOf(questId, goalKey);
            int next = Math.min(max, current + Math.max(0, amount));
            if (next == current) return false;
            progress.computeIfAbsent(questId, id -> new LinkedHashMap<>()).put(goalKey, next);
            return true;
        }
        return false;
    }

    /** Enregistre des creatures vaincues : renvoie les quetes dont l'objectif a avance. */
    public List<String> reportDefeat(String creatureId, int count) {
        return reportMatching("defeat:" + creatureId, count, creatureId);
    }

    /**
     * Un objectif "vaincre une creature" peut viser une creature precise ou toute
     * une classification ("Elf" sur le wiki regroupe Fire Elf Hunter et Pathfinder).
     * La classification est portee par la creature elle-meme.
     */
    public List<String> reportDefeat(String creatureId, String classification, int count) {
        List<String> touched = new ArrayList<>();
        for (String questId : new ArrayList<>(active)) {
            if (advance(questId, "defeat:" + creatureId, count)) touched.add(questId);
            else if (classification != null && advance(questId, "defeat:" + classification, count)) touched.add(questId);
        }
        return touched;
    }

    public List<String> reportCollect(String itemId, int count) {
        return reportMatching("collect:" + itemId, count, itemId);
    }

    public List<String> reportTalk(String npcId) {
        return reportMatching("talk:" + npcId, 1, npcId);
    }

    public List<String> reportVisit(String zoneId) {
        return reportMatching("visit:" + zoneId, 1, zoneId);
    }

    private List<String> reportMatching(String goalKey, int count, String ignored) {
        List<String> touched = new ArrayList<>();
        for (String questId : new ArrayList<>(active)) {
            if (advance(questId, goalKey, count)) touched.add(questId);
        }
        return touched;
    }

    // --------------------------------------------------------- persistance

    public void restore(List<String> activeQuests, Set<String> completedQuests,
                        Map<String, Map<String, Integer>> storedProgress) {
        active.clear();
        completed.clear();
        progress.clear();
        for (String id : activeQuests) {
            if (Quests.find(id).isPresent() && !active.contains(id)) active.add(id);
        }
        for (String id : completedQuests) {
            if (Quests.find(id).isPresent()) completed.add(id);
        }
        storedProgress.forEach((questId, goals) -> {
            if (!active.contains(questId)) return;
            Map<String, Integer> copy = new LinkedHashMap<>(goals);
            progress.put(questId, copy);
        });
    }
}
