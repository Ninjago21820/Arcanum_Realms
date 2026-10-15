package com.arcanumrealms.core.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Une quete, calquee sur la fiche du wiki : prerequis, donneur, lieu du donneur,
 * objectifs, remettant, recompenses (or, XP, sort, objet) et enchainement.
 *
 * @param id           identifiant
 * @param giverNpc     PNJ qui donne la quete
 * @param giverZone    zone du donneur (comme "Giver Location" sur le wiki)
 * @param prequest     quete a terminer avant (null si aucune)
 * @param levelRequired niveau minimum pour la prendre
 * @param side         vrai pour une quete annexe (le wiki distingue "side")
 * @param handInNpc    PNJ a qui rendre la quete
 * @param rewardXp     XP gagnee
 * @param rewardGold   or gagne
 * @param rewardSpell  sort appris en recompense (null si aucun)
 * @param rewardItem   objet d'equipement donne (null si aucun)
 * @param leadsTo      quete suivante de la chaine
 * @param leadsToLevel niveau requis pour la suivante
 * @param rewardChoices objets parmi lesquels choisir (comme les "reward choices" du jeu)
 */
public record QuestDef(String id,
                       String nameFr,
                       String nameEn,
                       String summaryFr,
                       String summaryEn,
                       String giverNpc,
                       String giverZone,
                       String prequest,
                       int levelRequired,
                       boolean side,
                       List<QuestGoal> goals,
                       String handInNpc,
                       int rewardXp,
                       int rewardGold,
                       String rewardSpell,
                       String rewardItem,
                       List<String> rewardChoices,
                       String leadsTo,
                       int leadsToLevel) {

    public QuestDef {
        goals = Collections.unmodifiableList(new ArrayList<>(goals));
        rewardChoices = Collections.unmodifiableList(new ArrayList<>(rewardChoices));
    }

    public String name(boolean french) { return french ? nameFr : nameEn; }

    public String summary(boolean french) { return french ? summaryFr : summaryEn; }

    public boolean main() { return !side; }

    public static Builder builder(String id) { return new Builder(id); }

    /** Constructeur pas a pas, pour garder la liste des quetes lisible. */
    public static final class Builder {
        private final String id;
        private String nameFr, nameEn, summaryFr, summaryEn, giverNpc, giverZone, prequest;
        private int levelRequired = 1;
        private boolean side;
        private final List<QuestGoal> goals = new ArrayList<>();
        private String handInNpc;
        private int rewardXp, rewardGold;
        private String rewardSpell, rewardItem, leadsTo;
        private int leadsToLevel;
        private final List<String> rewardChoices = new ArrayList<>();

        Builder(String id) { this.id = id; }

        public Builder fr(String name) { this.nameFr = name; return this; }
        public Builder en(String name) { this.nameEn = name; return this; }
        public Builder summaryFr(String text) { this.summaryFr = text; return this; }
        public Builder summaryEn(String text) { this.summaryEn = text; return this; }
        public Builder giver(String npcId) { this.giverNpc = npcId; return this; }
        public Builder giverZone(String zoneId) { this.giverZone = zoneId; return this; }
        public Builder prequest(String questId) { this.prequest = questId; return this; }
        public Builder level(int level) { this.levelRequired = level; return this; }
        public Builder side(boolean value) { this.side = value; return this; }
        public Builder handIn(String npcId) { this.handInNpc = npcId; return this; }
        public Builder xp(int value) { this.rewardXp = value; return this; }
        public Builder gold(int value) { this.rewardGold = value; return this; }
        public Builder spell(String spellId) { this.rewardSpell = spellId; return this; }
        public Builder item(String itemId) { this.rewardItem = itemId; return this; }
        public Builder choice(String itemId) { this.rewardChoices.add(itemId); return this; }
        public Builder leadsTo(String questId, int level) { this.leadsTo = questId; this.leadsToLevel = level; return this; }

        public Builder defeat(String creatureId, int count) { goals.add(new QuestGoal.Defeat(creatureId, count)); return this; }
        public Builder collect(String itemId, int count) { goals.add(new QuestGoal.Collect(itemId, count)); return this; }
        public Builder talk(String npcId) { goals.add(new QuestGoal.Talk(npcId)); return this; }
        public Builder visit(String zoneId) { goals.add(new QuestGoal.Visit(zoneId)); return this; }

        public QuestDef build() {
            if (handInNpc == null) handInNpc = giverNpc;
            if (nameEn == null) nameEn = id;
            if (nameFr == null) nameFr = nameEn;
            if (summaryEn == null) summaryEn = "";
            if (summaryFr == null) summaryFr = summaryEn;
            return new QuestDef(id, nameFr, nameEn, summaryFr, summaryEn, giverNpc, giverZone,
                    prequest, levelRequired, side, goals, handInNpc, rewardXp, rewardGold,
                    rewardSpell, rewardItem, rewardChoices, leadsTo, leadsToLevel);
        }
    }
}
