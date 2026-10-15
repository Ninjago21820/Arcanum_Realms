package com.arcanumrealms.core.world;

import com.arcanumrealms.core.school.School;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.arcanumrealms.core.school.School.*;

/**
 * Catalogue d'equipement.
 *
 * <p>Les creatures du wiki lachent des chapeaux, robes, bottes, baguettes,
 * athames, anneaux, amulettes et decks : chaque emplacement apporte les
 * statistiques qui lui correspondent dans Wizard101 (les robes donnent des PV
 * et de la resistance, les baguettes des degats et de la percee, les anneaux de
 * la chance de pip de puissance, etc.).
 *
 * <p>Cinq paliers suivent la progression des zones de la cite :
 * novice (1-4), apprenti (5-9), adepte (10-14), expert (15-19), maitre (20+).
 */
public final class Gears {

    private static final Map<String, GearDef> DEFS = new LinkedHashMap<>();

    private Gears() {}

    public static GearDef get(String id) {
        GearDef def = DEFS.get(id);
        if (def == null) throw new IllegalArgumentException("Equipement inconnu: " + id);
        return def;
    }

    public static Optional<GearDef> find(String id) {
        return Optional.ofNullable(DEFS.get(id));
    }

    public static Collection<GearDef> all() {
        return Collections.unmodifiableCollection(DEFS.values());
    }

    public static List<GearDef> ofSlot(GearSlot slot) {
        return DEFS.values().stream().filter(g -> g.slot() == slot).toList();
    }

    public static List<GearDef> ofTier(int tier) {
        return DEFS.values().stream().filter(g -> g.tier() == tier).toList();
    }

    /** Identifiant de l'objet a donner au joueur a la fin d'une quete de palier. */
    public static String reward(int tier, GearSlot slot) {
        for (GearDef def : DEFS.values()) {
            if (def.tier() == tier && def.slot() == slot && def.school() == null) return def.id();
        }
        throw new IllegalStateException("Aucun equipement de palier " + tier + " pour " + slot);
    }

    private static void add(GearDef def) {
        if (DEFS.put(def.id(), def) != null) throw new IllegalStateException("Equipement en double: " + def.id());
    }

    static {
        // ------------------------------------------------------- palier 0 : novice
        int t = 0;
        add(GearDef.builder("novice_hat").name("Novice Hat", "Chapeau de Novice")
                .slot(GearSlot.HAT).tier(t).level(1).health(20).powerPip(2).accuracy(2).build());
        add(GearDef.builder("novice_robe").name("Novice Robe", "Robe de Novice")
                .slot(GearSlot.ROBE).tier(t).level(1).health(40).powerPip(1).resist(EQUILIBRIUM, 1).build());
        add(GearDef.builder("novice_boots").name("Novice Shoes", "Bottes de Novice")
                .slot(GearSlot.BOOTS).tier(t).level(1).health(15).accuracy(3).block(1).build());
        add(GearDef.builder("novice_wand").name("Novice Wand", "Baguette de Novice")
                .slot(GearSlot.WAND).tier(t).level(1).health(10).powerPip(1).damage(3).crit(2).build());
        add(GearDef.builder("novice_athame").name("Novice Athame", "Athame de Novice")
                .slot(GearSlot.ATHAME).tier(t).level(1).powerPip(1).accuracy(2).damage(2).build());
        add(GearDef.builder("novice_ring").name("Novice Ring", "Anneau de Novice")
                .slot(GearSlot.RING).tier(t).level(1).health(10).powerPip(2).crit(1).build());
        add(GearDef.builder("novice_amulet").name("Novice Amulet", "Amulette de Novice")
                .slot(GearSlot.AMULET).tier(t).level(1).health(25).crit(2).block(2).build());
        add(GearDef.builder("novice_deck").name("Novice Deck", "Deck de Novice")
                .slot(GearSlot.DECK).tier(t).level(1).health(10).powerPip(1).accuracy(2).build());

        // ---------------------------------------------------- palier 1 : apprenti
        t = 1;
        add(GearDef.builder("grove_hood").name("Grove Hood", "Coiffe du Bosquet")
                .slot(GearSlot.HAT).tier(t).level(5).health(32).powerPip(3).accuracy(3).build());
        add(GearDef.builder("grove_robe").name("Grove Robe", "Robe du Bosquet")
                .slot(GearSlot.ROBE).tier(t).level(5).health(60).powerPip(2).resist(EQUILIBRIUM, 2).build());
        add(GearDef.builder("grove_boots").name("Grove Boots", "Bottes du Bosquet")
                .slot(GearSlot.BOOTS).tier(t).level(5).health(25).accuracy(4).block(2).damage(1).build());
        add(GearDef.builder("grove_wand").name("Grove Wand", "Baguette du Bosquet")
                .slot(GearSlot.WAND).tier(t).level(5).health(15).powerPip(2).damage(5).crit(4).pierce(1).build());
        add(GearDef.builder("grove_athame").name("Grove Athame", "Athame du Bosquet")
                .slot(GearSlot.ATHAME).tier(t).level(5).powerPip(2).accuracy(3).damage(4).build());
        add(GearDef.builder("grove_ring").name("Grove Ring", "Anneau du Bosquet")
                .slot(GearSlot.RING).tier(t).level(5).health(18).powerPip(3).crit(2).block(2).build());
        add(GearDef.builder("grove_amulet").name("Grove Amulet", "Amulette du Bosquet")
                .slot(GearSlot.AMULET).tier(t).level(5).health(40).crit(4).block(4).accuracy(2).build());
        add(GearDef.builder("grove_deck").name("Grove Deck", "Deck du Bosquet")
                .slot(GearSlot.DECK).tier(t).level(5).health(15).powerPip(2).crit(2).accuracy(4).build());

        // ------------------------------------------------------- palier 2 : adepte
        t = 2;
        add(GearDef.builder("ember_cowl").name("Ember Cowl", "Coiffe de Braise")
                .slot(GearSlot.HAT).tier(t).level(10).health(45).powerPip(4).accuracy(4).crit(2).build());
        add(GearDef.builder("ember_robe").name("Ember Robe", "Robe de Braise")
                .slot(GearSlot.ROBE).tier(t).level(10).health(85).powerPip(3).resist(EQUILIBRIUM, 3).build());
        add(GearDef.builder("ember_boots").name("Ember Boots", "Bottes de Braise")
                .slot(GearSlot.BOOTS).tier(t).level(10).health(36).accuracy(5).block(3).damage(2).build());
        add(GearDef.builder("ember_wand").name("Ember Wand", "Baguette de Braise")
                .slot(GearSlot.WAND).tier(t).level(10).health(20).powerPip(3).damage(7).crit(6).pierce(2).build());
        add(GearDef.builder("ember_athame").name("Ember Athame", "Athame de Braise")
                .slot(GearSlot.ATHAME).tier(t).level(10).powerPip(3).accuracy(4).damage(5).pierce(1).build());
        add(GearDef.builder("ember_ring").name("Ember Ring", "Anneau de Braise")
                .slot(GearSlot.RING).tier(t).level(10).health(26).powerPip(4).crit(3).block(3).build());
        add(GearDef.builder("ember_amulet").name("Ember Amulet", "Amulette de Braise")
                .slot(GearSlot.AMULET).tier(t).level(10).health(60).crit(6).block(6).accuracy(3).build());
        add(GearDef.builder("ember_deck").name("Ember Deck", "Deck de Braise")
                .slot(GearSlot.DECK).tier(t).level(10).health(20).powerPip(3).crit(3).accuracy(6).build());

        // ------------------------------------------------------- palier 3 : expert
        t = 3;
        add(GearDef.builder("tide_crown").name("Tide Crown", "Couronne des Marees")
                .slot(GearSlot.HAT).tier(t).level(15).health(60).powerPip(5).accuracy(5).crit(3).build());
        add(GearDef.builder("tide_robe").name("Tide Robe", "Robe des Marees")
                .slot(GearSlot.ROBE).tier(t).level(15).health(115).powerPip(4).resist(EQUILIBRIUM, 4).build());
        add(GearDef.builder("tide_boots").name("Tide Boots", "Bottes des Marees")
                .slot(GearSlot.BOOTS).tier(t).level(15).health(48).accuracy(6).block(4).damage(3).build());
        add(GearDef.builder("tide_wand").name("Tide Wand", "Baguette des Marees")
                .slot(GearSlot.WAND).tier(t).level(15).health(25).powerPip(4).damage(9).crit(8).pierce(3).build());
        add(GearDef.builder("tide_athame").name("Tide Athame", "Athame des Marees")
                .slot(GearSlot.ATHAME).tier(t).level(15).powerPip(4).accuracy(5).damage(7).pierce(2).build());
        add(GearDef.builder("tide_ring").name("Tide Ring", "Anneau des Marees")
                .slot(GearSlot.RING).tier(t).level(15).health(34).powerPip(5).crit(4).block(4).build());
        add(GearDef.builder("tide_amulet").name("Tide Amulet", "Amulette des Marees")
                .slot(GearSlot.AMULET).tier(t).level(15).health(80).crit(8).block(8).accuracy(4).build());
        add(GearDef.builder("tide_deck").name("Tide Deck", "Deck des Marees")
                .slot(GearSlot.DECK).tier(t).level(15).health(25).powerPip(4).crit(4).accuracy(8).build());

        // -------------------------------------------------------- palier 4 : maitre
        t = 4;
        add(GearDef.builder("hollow_crown").name("Hollow Crown", "Couronne du Creux")
                .slot(GearSlot.HAT).tier(t).level(20).health(78).powerPip(6).accuracy(6).crit(4).build());
        add(GearDef.builder("hollow_robe").name("Hollow Robe", "Robe du Creux")
                .slot(GearSlot.ROBE).tier(t).level(20).health(150).powerPip(5).resist(EQUILIBRIUM, 5).build());
        add(GearDef.builder("hollow_boots").name("Hollow Boots", "Bottes du Creux")
                .slot(GearSlot.BOOTS).tier(t).level(20).health(62).accuracy(7).block(5).damage(4).build());
        add(GearDef.builder("hollow_wand").name("Hollow Wand", "Baguette du Creux")
                .slot(GearSlot.WAND).tier(t).level(20).health(30).powerPip(5).damage(12).crit(10).pierce(4).build());
        add(GearDef.builder("hollow_athame").name("Hollow Athame", "Athame du Creux")
                .slot(GearSlot.ATHAME).tier(t).level(20).powerPip(5).accuracy(6).damage(9).pierce(3).build());
        add(GearDef.builder("hollow_ring").name("Hollow Ring", "Anneau du Creux")
                .slot(GearSlot.RING).tier(t).level(20).health(44).powerPip(6).crit(5).block(5).build());
        add(GearDef.builder("hollow_amulet").name("Hollow Amulet", "Amulette du Creux")
                .slot(GearSlot.AMULET).tier(t).level(20).health(105).crit(10).block(10).accuracy(5).build());
        add(GearDef.builder("hollow_deck").name("Hollow Deck", "Deck du Creux")
                .slot(GearSlot.DECK).tier(t).level(20).health(32).powerPip(5).crit(5).accuracy(10).build());

        // ------------------------------- athames d'ecole (butin des professeurs)
        add(schoolAthame("pyromancy_athame", "Pyromancy Athame", "Athame Pyromantique", PYROMANCY));
        add(schoolAthame("tempest_athame", "Tempest Athame", "Athame de Tempete", TEMPEST));
        add(schoolAthame("frost_athame", "Frost Athame", "Athame de Givre", FROST));
        add(schoolAthame("runomancy_athame", "Runomancy Athame", "Athame Runique", RUNOMANCY));
        add(schoolAthame("sylvan_athame", "Sylvan Athame", "Athame Sylvestre", SYLVAN));
        add(schoolAthame("necromancy_athame", "Necromancy Athame", "Athame Necromantique", NECROMANCY));
        add(schoolAthame("equilibrium_athame", "Equilibrium Athame", "Athame d'Equilibre", EQUILIBRIUM));
    }

    /** Athame de palier 2 qui ne booste que les degats de son ecole. */
    private static GearDef schoolAthame(String id, String en, String fr, School school) {
        return GearDef.builder(id).name(en, fr).slot(GearSlot.ATHAME).tier(2).level(10)
                .powerPip(2).accuracy(3).damage(school, 9).crit(2).school(school).build();
    }
}
