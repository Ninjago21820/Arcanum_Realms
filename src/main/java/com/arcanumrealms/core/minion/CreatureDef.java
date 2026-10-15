package com.arcanumrealms.core.minion;

import com.arcanumrealms.core.school.School;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Modele d'une creature : sert aussi bien aux ennemis du monde qu'aux serviteurs invoques.
 *
 * @param id         identifiant du modele
 * @param entityId   identifiant de l'entite Minecraft a faire apparaitre (ex. {@code arcanum_realms:ember_imp})
 * @param deck       liste des cartes du deck de la creature (repetitions incluses)
 * @param resist     resistance par ecole (%)
 * @param boss       vrai pour un boss (interface et recompenses specifiques)
 */
public record CreatureDef(String id,
                          String nameEn,
                          String nameFr,
                          String entityId,
                          School school,
                          int level,
                          int health,
                          Map<School, Integer> resist,
                          int critRating,
                          int blockRating,
                          int powerPipChance,
                          int accuracyBonus,
                          int damageBonus,
                          List<String> deck,
                          boolean boss) {

    public CreatureDef {
        resist = Collections.unmodifiableMap(resist);
        deck = Collections.unmodifiableList(deck);
    }

    public String name(boolean french) { return french ? nameFr : nameEn; }

    public int resist(School school) { return resist.getOrDefault(school, 0); }
}
