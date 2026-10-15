package com.arcanumrealms.core.world;

import com.arcanumrealms.core.school.School;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un personnage non joueur, calque sur la fiche du wiki : nom, role, lieu,
 * description, quetes donnees et quetes rendues, plus ses repliques.
 *
 * @param id          identifiant
 * @param zoneId      zone ou il se trouve
 * @param role        ce qu'il propose au joueur
 * @param school      ecole enseignee (pour les professeurs), sinon null
 * @param level       niveau du PNJ (les professeurs sont plus hauts)
 * @param entity      entite Minecraft utilisee pour le representer (ex. minecraft:villager)
 * @param outfit      couleur de sa tenue (0xRRGGBB)
 */
public record NpcDef(String id,
                     String nameEn,
                     String nameFr,
                     String titleEn,
                     String titleFr,
                     String zoneId,
                     NpcRole role,
                     School school,
                     int level,
                     String entity,
                     int outfit,
                     String descriptionEn,
                     String descriptionFr,
                     Dialogue greeting,
                     Dialogue inProgress,
                     Dialogue completion,
                     Dialogue farewell,
                     List<String> givesQuests,
                     List<String> endsQuests) {

    public NpcDef {
        givesQuests = Collections.unmodifiableList(new ArrayList<>(givesQuests));
        endsQuests = Collections.unmodifiableList(new ArrayList<>(endsQuests));
    }

    public String name(boolean french) { return french ? nameFr : nameEn; }

    public String title(boolean french) { return french ? titleFr : titleEn; }

    public String description(boolean french) { return french ? descriptionFr : descriptionEn; }

    /** "Professeure Cendrelune, maitresse de la Pyromancie" */
    public String fullName(boolean french) {
        String title = title(french);
        return title.isEmpty() ? name(french) : name(french) + ", " + title;
    }

    public boolean isTrainer() { return role == NpcRole.TRAINER; }

    public static Builder builder(String id) { return new Builder(id); }

    /** Constructeur pas a pas pour garder la liste des PNJ lisible. */
    public static final class Builder {
        private final String id;
        private String nameEn, nameFr, titleEn = "", titleFr = "";
        private String zoneId;
        private NpcRole role = NpcRole.VILLAGER;
        private School school;
        private int level = 5;
        private String entity = "minecraft:villager";
        private int outfit = 0x9B59B6;
        private String descriptionEn = "", descriptionFr;
        private Dialogue greeting = Dialogue.EMPTY;
        private Dialogue inProgress = Dialogue.EMPTY;
        private Dialogue completion = Dialogue.EMPTY;
        private Dialogue farewell = Dialogue.EMPTY;
        private final List<String> givesQuests = new ArrayList<>();
        private final List<String> endsQuests = new ArrayList<>();

        Builder(String id) { this.id = id; this.nameEn = id; }

        public Builder name(String en, String fr) { this.nameEn = en; this.nameFr = fr; return this; }
        public Builder title(String en, String fr) { this.titleEn = en; this.titleFr = fr; return this; }
        public Builder zone(String zoneId) { this.zoneId = zoneId; return this; }
        public Builder role(NpcRole role) { this.role = role; return this; }
        public Builder school(School school) { this.school = school; return this; }
        public Builder level(int level) { this.level = level; return this; }
        public Builder entity(String entity) { this.entity = entity; return this; }
        public Builder outfit(int rgb) { this.outfit = rgb; return this; }
        public Builder description(String en, String fr) { this.descriptionEn = en; this.descriptionFr = fr; return this; }
        public Builder greeting(Dialogue dialogue) { this.greeting = dialogue; return this; }
        public Builder inProgress(Dialogue dialogue) { this.inProgress = dialogue; return this; }
        public Builder completion(Dialogue dialogue) { this.completion = dialogue; return this; }
        public Builder farewell(Dialogue dialogue) { this.farewell = dialogue; return this; }
        public Builder gives(String... questIds) { Collections.addAll(givesQuests, questIds); return this; }
        public Builder ends(String... questIds) { Collections.addAll(endsQuests, questIds); return this; }

        public NpcDef build() {
            if (nameFr == null) nameFr = nameEn;
            if (descriptionFr == null) descriptionFr = descriptionEn;
            return new NpcDef(id, nameEn, nameFr, titleEn, titleFr, zoneId, role, school, level, entity,
                    outfit, descriptionEn, descriptionFr, greeting, inProgress, completion, farewell,
                    givesQuests, endsQuests);
        }
    }
}
