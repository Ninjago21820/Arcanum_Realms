package com.arcanumrealms.core.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Une zone du monde : une rue ou un quartier de la cite.
 *
 * <p>Comme sur le wiki, une zone rassemble ses creatures, ses PNJ et ses quetes,
 * et possede une plage de niveaux conseilles.
 *
 * @param id        identifiant
 * @param minLevel  niveau conseille minimum
 * @param maxLevel  niveau conseille maximum
 * @param dungeon   vrai pour un donjon (rencontre finale)
 * @param unlockQuest quete a terminer pour debloquer la zone (null si ouverte)
 * @param unlockLevel niveau minimum pour entrer
 */
public record ZoneDef(String id,
                      String nameFr,
                      String nameEn,
                      String descriptionFr,
                      String descriptionEn,
                      int minLevel,
                      int maxLevel,
                      List<String> creatures,
                      List<String> npcs,
                      List<String> quests,
                      boolean dungeon,
                      String unlockQuest,
                      int unlockLevel) {

    public ZoneDef {
        creatures = Collections.unmodifiableList(new ArrayList<>(creatures));
        npcs = Collections.unmodifiableList(new ArrayList<>(npcs));
        quests = Collections.unmodifiableList(new ArrayList<>(quests));
    }

    public String name(boolean french) { return french ? nameFr : nameEn; }

    public String description(boolean french) { return french ? descriptionFr : descriptionEn; }

    public String levelRange() { return minLevel + "-" + maxLevel; }
}
