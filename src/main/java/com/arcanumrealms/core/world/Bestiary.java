package com.arcanumrealms.core.world;

import com.arcanumrealms.core.minion.CreatureDef;
import com.arcanumrealms.core.school.School;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.arcanumrealms.core.school.School.*;

/**
 * Bestiaire du monde, releve sur le wiki de Wizard101 Central.
 *
 * <p>Chaque creature reprend la fiche du wiki : rang, points de vie, ecole,
 * pips de depart, faiblesse ("Incoming Boost"), resistance ("Incoming Resist"),
 * classification, sorts utilises (son deck), zone d'apparition et butin.
 *
 * <p>Les valeurs de reference verifiees sur le wiki :
 * <ul>
 *   <li>Chasseur elfe de braise : rang 2, 150 PV, Feu, 1 pip, +10 % de degats
 *       subis de Givre, 10 % de resistance au Feu, sensible aux etourdissements ;</li>
 *   <li>l'or lache par une creature de rang 2 va de 2 a 5 pieces ;</li>
 *   <li>les creatures de l'Allee des braises : chasseur, eclaireur, homme de magma,
 *       serviteur hante, guerrier squelettique, plus les boss Sepulcra Ombrevent,
 *       Alcane Vive-Fleche, Vermigut et Melweena Chatiment ;</li>
 *   <li>celles de l'Avenue des marees : marcheur des cryptes, chair pourrissante,
 *       rampant infect, hurleurs ecarlates, anguille electrique, Kraken,
 *       sergent Fende-Crane et le Seigneur des Moissons en boss final.</li>
 * </ul>
 */
public final class Bestiary {

    private static final Map<String, WorldCreature> DEFS = new LinkedHashMap<>();

    private Bestiary() {}

    public static WorldCreature get(String id) {
        WorldCreature def = DEFS.get(id);
        if (def == null) throw new IllegalArgumentException("Creature inconnue: " + id);
        return def;
    }

    public static Optional<WorldCreature> find(String id) {
        return Optional.ofNullable(DEFS.get(id));
    }

    public static Collection<WorldCreature> all() {
        return Collections.unmodifiableCollection(DEFS.values());
    }

    public static List<WorldCreature> ofZone(String zoneId) {
        return DEFS.values().stream().filter(c -> c.zoneIds().contains(zoneId)).toList();
    }

    /** Creature associee a une entite Minecraft (utilise par le gestionnaire de duels). */
    public static Optional<WorldCreature> forEntity(String entityId) {
        return DEFS.values().stream().filter(c -> c.entity().equals(entityId)).findFirst();
    }

    /** Creature correspondant a une entite, en respectant la zone si possible. */
    public static Optional<WorldCreature> forEntity(String entityId, String zoneId) {
        return DEFS.values().stream()
                .filter(c -> c.entity().equals(entityId))
                .filter(c -> zoneId == null || c.zoneIds().contains(zoneId))
                .findFirst()
                .or(() -> forEntity(entityId));
    }

    public static WorldCreature def(String id) {
        return get(id);
    }

    /** Toutes les creatures sous forme de fiches de combat. */
    public static List<CreatureDef> combatDefs() {
        return DEFS.values().stream().map(WorldCreature::toCreatureDef).toList();
    }

    private static void add(WorldCreature def) {
        if (DEFS.put(def.id(), def) != null) throw new IllegalStateException("Creature en double: " + def.id());
    }

    // ------------------------------------------------------------------ donnees

    static {
        starterZone();
        emberAlley();
        tideAvenue();
        colossusRoad();
        hollowHollow();
    }

    /** Le Sentier des Lucioles : les premieres rencontres, rangs 1 et 2. */
    private static void starterZone() {
        add(WorldCreature.builder("lost_soul")
                .name("Lost Soul", "Ame Perdue").school(NECROMANCY).rank(1).level(2).health(55).pips(0)
                .resist(NECROMANCY, 10).boost(SYLVAN, 15)
                .classification("Spirit").zones("sentier_lucioles")
                .entity("minecraft:skeleton")
                .deck("wisp_wraith", "wisp_wraith", "wisp_wraith", "ghoul", "wand_necromancy")
                .loot("rank1").build());

        add(WorldCreature.builder("haunted_minion")
                .name("Haunted Minion", "Serviteur Hante").school(NECROMANCY).rank(1).level(3).health(90).pips(1)
                .resist(NECROMANCY, 10).boost(SYLVAN, 15)
                .classification("Spirit").zones("sentier_lucioles", "allee_braises", "avenue_marees")
                .entity("minecraft:zombie")
                .deck("wisp_wraith", "wisp_wraith", "wisp_wraith", "grave_edge", "wand_necromancy")
                .loot("rank1").build());

        add(WorldCreature.builder("skeletal_warrior")
                .name("Skeletal Warrior", "Guerrier Squelettique").school(NECROMANCY).rank(2).level(4).health(115).pips(1)
                .resist(NECROMANCY, 15).boost(SYLVAN, 20)
                .classification("Undead").zones("sentier_lucioles", "allee_braises")
                .entity("minecraft:skeleton").crit(5)
                .deck("wisp_wraith", "wisp_wraith", "ghoul", "ghoul", "grave_mark", "wand_necromancy")
                .loot("rank2").build());

        add(WorldCreature.builder("pesky_pixie")
                .name("Pesky Pixie", "Lutin Ennuyeux").school(TEMPEST).rank(1).level(2).health(65).pips(0)
                .resist(TEMPEST, 10).boost(RUNOMANCY, 15)
                .classification("Fairy").zones("sentier_lucioles")
                .entity("minecraft:silverfish")
                .deck("spark_serpent", "spark_serpent", "spark_serpent", "gale_bats", "wand_tempest")
                .loot("rank1").build());

        add(WorldCreature.builder("thorn_sprite")
                .name("Thorn Sprite", "Lutin des Ronces").school(SYLVAN).rank(1).level(3).health(60).pips(0)
                .resist(SYLVAN, 10).boost(NECROMANCY, 15)
                .classification("Fairy").zones("sentier_lucioles")
                .entity("minecraft:cave_spider")
                .deck("sprout_imp", "sprout_imp", "sprout_imp", "verdant_edge", "wand_sylvan")
                .loot("rank1").build());

        add(WorldCreature.builder("scarab_swarm")
                .name("Scarab Swarm", "Essaim de Scarabees").school(EQUILIBRIUM).rank(1).level(3).health(70).pips(0)
                .resist(EQUILIBRIUM, 10).boost(TEMPEST, 15)
                .classification("Insect").zones("sentier_lucioles")
                .entity("minecraft:endermite")
                .deck("scarab", "scarab", "scarab", "weakness", "wand_equilibrium")
                .loot("rank1").build());
    }

    /** L'Allee des Braises : le quartier en feu, rangs 2 a 4. */
    private static void emberAlley() {
        add(WorldCreature.builder("ember_elf_hunter")
                .name("Ember Elf Hunter", "Chasseur Elfe de Braise").school(PYROMANCY).rank(2).level(9)
                .health(150).pips(1)
                .resist(PYROMANCY, 10).boost(FROST, 10)
                .classification("Elf").zones("allee_braises")
                .entity("minecraft:pillager").crit(5)
                .deck("ember_kitten", "ember_kitten", "ember_kitten", "cinder_wisp", "sunhawk",
                        "frost_ward", "wand_pyromancy")
                .loot("rank2").build());

        add(WorldCreature.builder("ember_elf_pathfinder")
                .name("Ember Elf Pathfinder", "Eclaireur Elfe de Braise").school(PYROMANCY).rank(2).level(8)
                .health(135).pips(1)
                .resist(PYROMANCY, 10).boost(FROST, 10)
                .classification("Elf").zones("allee_braises")
                .entity("minecraft:vindicator")
                .deck("ember_kitten", "ember_kitten", "cinder_wisp", "frost_ward", "wand_pyromancy")
                .loot("rank2").build());

        add(WorldCreature.builder("magma_man")
                .name("Magma Man", "Homme de Magma").school(PYROMANCY).rank(3).level(11)
                .health(210).pips(2)
                .resist(PYROMANCY, 15).boost(FROST, 15)
                .classification("Elemental").zones("allee_braises")
                .entity("minecraft:magma_cube").crit(5)
                .deck("ember_kitten", "cinder_wisp", "cinder_wisp", "sunhawk", "pyre_mark", "wand_pyromancy")
                .loot("rank3").build());

        add(WorldCreature.builder("gretta_darkkettle")
                .name("Gretta Darkkettle", "Gretta Chaudron-Noir").school(PYROMANCY).rank(3).level(10)
                .health(300).pips(2)
                .resist(PYROMANCY, 15).boost(FROST, 15)
                .classification("Witch").zones("allee_braises")
                .entity("minecraft:witch").crit(8).powerPip(35)
                .deck("ember_kitten", "cinder_wisp", "pyre_mark", "fan_the_flames", "sunhawk", "wand_pyromancy")
                .loot("rank3").build());

        add(WorldCreature.builder("wormguts")
                .name("Wormguts", "Vermigut").school(RUNOMANCY).rank(3).level(10)
                .health(340).pips(2).boss(true)
                .resist(RUNOMANCY, 15).boost(TEMPEST, 15)
                .classification("Gobbler").zones("allee_braises")
                .entity("minecraft:hoglin").crit(10).powerPip(40)
                .deck("rune_bat", "rune_bat", "stone_troll", "rune_mark", "runic_fury", "wand_runomancy")
                .loot("boss_alley").build());

        add(WorldCreature.builder("melweena_smite")
                .name("Melweena Smite", "Melweena Chatiment").school(NECROMANCY).rank(3).level(10)
                .health(320).pips(2).boss(true)
                .resist(NECROMANCY, 15).boost(SYLVAN, 20)
                .classification("Undead").zones("allee_braises")
                .entity("minecraft:witch").crit(10).powerPip(40)
                .deck("ghoul", "wraith", "wisp_wraith", "wisp_wraith", "plague", "wand_necromancy")
                .loot("boss_alley").build());

        add(WorldCreature.builder("bastilla_gravewynd")
                .name("Sepulcra Gravewynd", "Sepulcra Ombrevent").school(NECROMANCY).rank(4).level(12)
                .health(415).pips(3).boss(true)
                .resist(NECROMANCY, 20).boost(SYLVAN, 25)
                .classification("Undead").zones("allee_braises")
                .entity("minecraft:wither_skeleton").crit(12).block(10).powerPip(45)
                .deck("ghoul", "ghoul", "wraith", "wraith", "grave_edge", "plague", "grave_mark",
                        "wand_necromancy")
                .loot("boss_alley").build());

        add(WorldCreature.builder("alicane_swiftarrow")
                .name("Alcane Swiftarrow", "Alcane Vive-Fleche").school(PYROMANCY).rank(4).level(12)
                .health(400).pips(3).boss(true)
                .resist(PYROMANCY, 20).boost(FROST, 25)
                .classification("Elf").zones("allee_braises")
                .entity("minecraft:pillager").crit(12).block(10).powerPip(45)
                .deck("sunhawk", "sunhawk", "cinder_wisp", "emberedge", "pyre_mark", "fan_the_flames",
                        "ashfall", "wand_pyromancy")
                .loot("boss_alley").build());
    }

    /** L'Avenue des Marees : le port noye et ses morts-vivants, rangs 2 a 4. */
    private static void tideAvenue() {
        add(WorldCreature.builder("rotting_fodder")
                .name("Rotting Fodder", "Chair Pourrissante").school(NECROMANCY).rank(2).level(6)
                .health(155).pips(1)
                .resist(NECROMANCY, 10).boost(SYLVAN, 15)
                .classification("Undead").zones("avenue_marees")
                .entity("minecraft:zombie")
                .deck("wisp_wraith", "wisp_wraith", "wisp_wraith", "ghoul", "wand_necromancy")
                .loot("rank2").build());

        add(WorldCreature.builder("crypt_walker")
                .name("Crypt Walker", "Marcheur des Cryptes").school(NECROMANCY).rank(2).level(7)
                .health(175).pips(1)
                .resist(NECROMANCY, 10).boost(SYLVAN, 15)
                .classification("Undead").zones("avenue_marees")
                .entity("minecraft:husk")
                .deck("wisp_wraith", "wisp_wraith", "ghoul", "grave_mark", "wand_necromancy")
                .loot("rank2").build());

        add(WorldCreature.builder("loathsome_creeper")
                .name("Loathsome Creeper", "Rampant Infect").school(NECROMANCY).rank(2).level(7)
                .health(165).pips(1)
                .resist(NECROMANCY, 10).boost(SYLVAN, 15)
                .classification("Undead").zones("avenue_marees")
                .entity("minecraft:cave_spider")
                .deck("wisp_wraith", "wisp_wraith", "ghoul", "grave_edge", "wand_necromancy")
                .loot("rank2").build());

        add(WorldCreature.builder("rotten_rob")
                .name("Rotten Rob", "Rob le Pourri").school(NECROMANCY).rank(3).level(8)
                .health(240).pips(2)
                .resist(NECROMANCY, 15).boost(SYLVAN, 20)
                .classification("Undead").zones("avenue_marees")
                .entity("minecraft:zombie").crit(5)
                .deck("ghoul", "ghoul", "wraith", "wisp_wraith", "plague", "wand_necromancy")
                .loot("rank3").build());

        add(WorldCreature.builder("electric_eel")
                .name("Electric Eel", "Anguille Electrique").school(TEMPEST).rank(2).level(7)
                .health(145).pips(1)
                .resist(TEMPEST, 10).boost(RUNOMANCY, 15)
                .classification("Sea Creature").zones("avenue_marees")
                .entity("minecraft:drowned")
                .deck("spark_serpent", "spark_serpent", "gale_bats", "galeedge", "wand_tempest")
                .loot("rank2").build());

        add(WorldCreature.builder("scarlet_screamer")
                .name("Scarlet Screamer", "Hurleur Ecarlate").school(TEMPEST).rank(3).level(8)
                .health(205).pips(2)
                .resist(TEMPEST, 15).boost(RUNOMANCY, 20)
                .classification("Sea Creature").zones("avenue_marees")
                .entity("minecraft:phantom").crit(5)
                .deck("spark_serpent", "gale_bats", "gale_bats", "galeedge", "wand_tempest")
                .loot("rank3").build());

        add(WorldCreature.builder("spellwrit_screamer")
                .name("Spellwrit Screamer", "Hurleur Griffe-Sort").school(TEMPEST).rank(3).level(9)
                .health(215).pips(2)
                .resist(TEMPEST, 15).boost(RUNOMANCY, 20)
                .classification("Sea Creature").zones("avenue_marees")
                .entity("minecraft:phantom").crit(5)
                .deck("spark_serpent", "spark_serpent", "gale_bats", "tempest_mark", "wand_tempest")
                .loot("rank3").build());

        add(WorldCreature.builder("fear_of_drowning")
                .name("Fear of Drowning", "Peur de Se Noyer").school(TEMPEST).rank(3).level(9)
                .health(260).pips(2)
                .resist(TEMPEST, 15).boost(RUNOMANCY, 20)
                .classification("Spirit").zones("avenue_marees")
                .entity("minecraft:drowned").crit(8).powerPip(35)
                .deck("spark_serpent", "gale_bats", "riptide_shark", "galeedge", "wand_tempest")
                .loot("rank3").build());

        add(WorldCreature.builder("kestrel_blackstorm")
                .name("Kestrel Blackstorm", "Kestrel Orage-Noir").school(TEMPEST).rank(3).level(9)
                .health(250).pips(2)
                .resist(TEMPEST, 15).boost(RUNOMANCY, 20)
                .classification("Human").zones("avenue_marees")
                .entity("minecraft:witch").crit(8).powerPip(35)
                .deck("spark_serpent", "gale_bats", "tempest_mark", "galeedge", "tempest_surge", "wand_tempest")
                .loot("rank3").build());

        add(WorldCreature.builder("sergeant_skullsplitter")
                .name("Sergeant Skullsplitter", "Sergent Fende-Crane").school(RUNOMANCY).rank(3).level(9)
                .health(275).pips(2)
                .resist(RUNOMANCY, 15).boost(TEMPEST, 15)
                .classification("Gobbler").zones("avenue_marees")
                .entity("minecraft:vindicator").crit(8).powerPip(35)
                .deck("rune_bat", "rune_bat", "stone_troll", "rune_mark", "runic_fury", "wand_runomancy")
                .loot("rank3").build());

        add(WorldCreature.builder("tide_kraken")
                .name("Tide Kraken", "Kraken des Marees").school(TEMPEST).rank(4).level(10)
                .health(520).pips(3).boss(true)
                .resist(TEMPEST, 20).boost(RUNOMANCY, 25)
                .classification("Sea Creature").zones("avenue_marees")
                .entity("minecraft:ravager").crit(12).block(10).powerPip(45)
                .deck("riptide_shark", "riptide_shark", "spark_serpent", "gale_bats", "tempest_mark",
                        "tempest_surge", "wand_tempest")
                .loot("boss_avenue").build());

        add(WorldCreature.builder("the_harvest_lord")
                .name("The Harvest Lord", "Le Seigneur des Moissons").school(NECROMANCY).rank(4).level(10)
                .health(500).pips(3).boss(true)
                .resist(NECROMANCY, 20).boost(SYLVAN, 25)
                .classification("Undead").zones("avenue_marees")
                .entity("minecraft:wither_skeleton").crit(12).block(10).powerPip(45)
                .deck("ghoul", "ghoul", "wraith", "plague", "harvest_of_bones", "grave_edge",
                        "doom_and_gloom", "wand_necromancy")
                .loot("boss_avenue").build());
    }

    /** Le Chemin des Colosses : les gardiens de pierre et de glace, rangs 3 et 4. */
    private static void colossusRoad() {
        add(WorldCreature.builder("ice_troll")
                .name("Ice Troll", "Troll des Glaces").school(FROST).rank(3).level(13)
                .health(300).pips(2)
                .resist(FROST, 15).boost(PYROMANCY, 15)
                .classification("Troll").zones("chemin_colosses")
                .entity("minecraft:stray").crit(5)
                .deck("frost_beetle", "frost_beetle", "snow_serpent", "frost_mark", "warding_shield", "wand_frost")
                .loot("rank3").build());

        add(WorldCreature.builder("marauder_imp")
                .name("Marauder Imp", "Diablotin Maraudeur").school(PYROMANCY).rank(3).level(14)
                .health(280).pips(2)
                .resist(PYROMANCY, 15).boost(FROST, 15)
                .classification("Imp").zones("chemin_colosses")
                .entity("minecraft:zombified_piglin").crit(8)
                .deck("ember_kitten", "ember_kitten", "sunhawk", "pyre_mark", "emberedge", "wand_pyromancy")
                .loot("rank3").build());

        add(WorldCreature.builder("colossus_sentinel")
                .name("Colossus Sentinel", "Sentinelle Colossale").school(FROST).rank(4).level(15)
                .health(400).pips(3)
                .resist(FROST, 20).boost(PYROMANCY, 20)
                .classification("Golem").zones("chemin_colosses")
                .entity("minecraft:stray").crit(10).block(10).powerPip(35)
                .stunnable(false)
                .deck("snow_serpent", "snow_serpent", "ice_wyvern", "glacial_aegis", "rimeedge", "wand_frost")
                .loot("rank4").build());

        add(WorldCreature.builder("night_shade")
                .name("Night Shade", "Ombre Nocturne").school(NECROMANCY).rank(4).level(16)
                .health(340).pips(3)
                .resist(NECROMANCY, 20).boost(SYLVAN, 25)
                .classification("Spirit").zones("chemin_colosses")
                .entity("minecraft:wither_skeleton").crit(10).powerPip(35)
                .deck("wraith", "wraith", "ghoul", "grave_edge", "plague", "wand_necromancy")
                .loot("rank4").build());

        add(WorldCreature.builder("rune_marauder")
                .name("Rune Marauder", "Maraudeur Runique").school(RUNOMANCY).rank(4).level(16)
                .health(360).pips(3)
                .resist(RUNOMANCY, 20).boost(TEMPEST, 20)
                .classification("Gobbler").zones("chemin_colosses")
                .entity("minecraft:pillager").crit(10).powerPip(35)
                .deck("rune_bat", "stone_troll", "rune_cyclops", "rune_mark", "runic_fury", "wand_runomancy")
                .loot("rank4").build());
    }

    /** La Grotte Hantee : le donjon final, rangs 5 et plus. */
    private static void hollowHollow() {
        add(WorldCreature.builder("corsair_bones")
                .name("Corsair Bones", "Os de Corsaire").school(NECROMANCY).rank(5).level(16)
                .health(700).pips(3)
                .resist(NECROMANCY, 25).boost(SYLVAN, 25)
                .classification("Undead").zones("grotte_hantee")
                .entity("minecraft:skeleton").crit(12).block(10).powerPip(40)
                .deck("wraith", "wraith", "ghoul", "harvest_of_bones", "grave_edge", "wand_necromancy")
                .loot("rank5").build());

        add(WorldCreature.builder("bone_legion")
                .name("Bone Legion", "Legion d'Os").school(NECROMANCY).rank(4).level(15)
                .health(420).pips(2)
                .resist(NECROMANCY, 20).boost(SYLVAN, 20)
                .classification("Undead").zones("grotte_hantee")
                .entity("minecraft:husk").crit(8)
                .deck("wisp_wraith", "ghoul", "ghoul", "grave_mark", "wand_necromancy")
                .loot("rank4").build());

        add(WorldCreature.builder("hollow_lord")
                .name("The Hollow Lord", "Le Seigneur du Creux").school(NECROMANCY).rank(6).level(18)
                .health(1200).pips(4).boss(true)
                .resist(NECROMANCY, 30).boost(SYLVAN, 30)
                .classification("Undead").zones("grotte_hantee")
                .entity("minecraft:wither_skeleton").crit(18).block(15).powerPip(50)
                .deck("wraith", "wraith", "plague", "harvest_of_bones", "doom_and_gloom",
                        "grave_mark", "bone_corsair", "wand_necromancy")
                .loot("boss_hollow").build());

        add(WorldCreature.builder("grand_necromancer")
                .name("The Grand Necromancer", "Le Grand Necromancien").school(NECROMANCY).rank(7).level(20)
                .health(1800).pips(5).boss(true)
                .resist(NECROMANCY, 35).boost(SYLVAN, 35)
                .classification("Undead").zones("grotte_hantee")
                .entity("minecraft:evoker").crit(22).block(20).powerPip(60)
                .stunnable(false)
                .deck("wraith", "wraith", "wraith", "plague", "harvest_of_bones",
                        "doom_and_gloom", "bone_corsair", "wand_necromancy")
                .loot("boss_hollow").build());
    }

    /** Nombre de creatures par zone, pour l'affichage et les tests. */
    public static int countIn(School school) {
        return (int) DEFS.values().stream().filter(c -> c.school() == school).count();
    }
}
