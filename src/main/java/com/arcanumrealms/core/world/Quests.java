package com.arcanumrealms.core.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Les quetes de la Cite des Arcanes.
 *
 * <p>Chaque quete suit la fiche du wiki : un donneur, un lieu, une quete
 * precedente, un niveau minimum, une liste d'objectifs (vaincre, recolter,
 * parler, se rendre quelque part), un remettant et des recompenses (XP, or,
 * sort, equipement).
 *
 * <p>La chaine principale traverse les cinq zones dans l'ordre ; les quetes
 * annexes et les sept epreuves d'ecole peuvent etre prises en parallele.
 */
public final class Quests {

    private static final Map<String, QuestDef> DEFS = new LinkedHashMap<>();

    private Quests() {}

    public static QuestDef get(String id) {
        QuestDef def = DEFS.get(id);
        if (def == null) throw new IllegalArgumentException("Quete inconnue: " + id);
        return def;
    }

    public static Optional<QuestDef> find(String id) {
        return Optional.ofNullable(DEFS.get(id));
    }

    public static Collection<QuestDef> all() {
        return Collections.unmodifiableCollection(DEFS.values());
    }

    public static List<QuestDef> ofZone(String zoneId) {
        return DEFS.values().stream().filter(q -> zoneId.equals(q.giverZone())).toList();
    }

    public static List<QuestDef> byGiver(String npcId) {
        return DEFS.values().stream().filter(q -> npcId.equals(q.giverNpc())).toList();
    }

    public static List<QuestDef> byHandIn(String npcId) {
        return DEFS.values().stream().filter(q -> npcId.equals(q.handInNpc())).toList();
    }

    /** Premier maillon de la chaine principale. */
    public static String firstMainQuest() {
        return "welcome_to_the_city";
    }

    private static void add(QuestDef def) {
        if (DEFS.put(def.id(), def) != null) throw new IllegalStateException("Quete en double: " + def.id());
    }

    static {
        // ------------------------------------------------------------ la grand-place
        add(QuestDef.builder("welcome_to_the_city")
                .fr("Bienvenue dans la Cite").en("Welcome to the City")
                .summaryFr("Le directeur vous envoie sur le Sentier des Lucioles.")
                .summaryEn("The Headmaster sends you to the Firefly Path.")
                .giver("headmaster_alderwise").giverZone("grand_place")
                .visit("sentier_lucioles").talk("lea_nightchant")
                .handIn("headmaster_alderwise")
                .xp(20).gold(5)
                .leadsTo("first_steps", 1)
                .build());

        // ------------------------------------------------------- le sentier des lucioles
        add(QuestDef.builder("first_steps")
                .fr("Premiers Pas").en("First Steps")
                .summaryFr("Lea Chantenuit veut voir comment vous vous battez.")
                .summaryEn("Lea Nightchant wants to see how you fight.")
                .giver("lea_nightchant").giverZone("sentier_lucioles")
                .prequest("welcome_to_the_city").level(1)
                .defeat("lost_soul", 2)
                .xp(50).gold(6).item("novice_hat")
                .leadsTo("unwelcome_spirits", 1)
                .build());

        add(QuestDef.builder("unwelcome_spirits")
                .fr("Esprits Indesirables").en("Unwelcome Spirits")
                .summaryFr("Les ames perdues reviennent sur le sentier.")
                .summaryEn("The lost souls keep coming back to the path.")
                .giver("lea_nightchant").giverZone("sentier_lucioles")
                .prequest("first_steps").level(1)
                .defeat("lost_soul", 3).defeat("haunted_minion", 1)
                .xp(60).gold(8).item("novice_ring")
                .leadsTo("bones_and_badges", 3)
                .build());

        add(QuestDef.builder("bones_and_badges")
                .fr("Os et Insignes").en("Bones and Badges")
                .summaryFr("Un guerrier squelettique porte l'insigne de la garde disparue.")
                .summaryEn("A skeletal warrior still wears a missing guard's badge.")
                .giver("lea_nightchant").giverZone("sentier_lucioles")
                .prequest("unwelcome_spirits").level(3)
                .defeat("skeletal_warrior", 2).collect("minecraft:bone", 3)
                .xp(90).gold(12).item("novice_robe")
                .leadsTo("report_to_hanna", 5)
                .build());

        add(QuestDef.builder("pixie_problem")
                .fr("Probleme de Lutins").en("Pixie Problem")
                .summaryFr("Les lutins s'attaquent aux voyageurs.")
                .summaryEn("The pixies are ambushing travellers.")
                .giver("lea_nightchant").giverZone("sentier_lucioles").level(1).side(true)
                .defeat("pesky_pixie", 4)
                .xp(35).gold(5).item("novice_boots")
                .build());

        add(QuestDef.builder("sprite_trouble")
                .fr("Ronces Vivantes").en("Living Thorns")
                .summaryFr("Des lutins des ronces ont envahi les haies.")
                .summaryEn("Thorn sprites have overrun the hedges.")
                .giver("lea_nightchant").giverZone("sentier_lucioles").level(2).side(true)
                .defeat("thorn_sprite", 3)
                .xp(40).gold(6).item("novice_amulet")
                .build());

        add(QuestDef.builder("scarabs_in_the_grass")
                .fr("Scarabees dans l'Herbe").en("Scarabs in the Grass")
                .summaryFr("Un essaim de scarabees grignote les recoltes.")
                .summaryEn("A scarab swarm is chewing through the crops.")
                .giver("lea_nightchant").giverZone("sentier_lucioles").level(2).side(true)
                .defeat("scarab_swarm", 3).collect("minecraft:amethyst_shard", 1)
                .xp(45).gold(7).item("novice_deck")
                .build());

        // ---------------------------------------------------------- l'allee des braises
        add(QuestDef.builder("report_to_hanna")
                .fr("Rapport a Hanna").en("Report to Hanna")
                .summaryFr("Lea vous envoie preter main forte a Hanna Brassefonte.")
                .summaryEn("Lea sends you to help Hanna Emberwright.")
                .giver("lea_nightchant").giverZone("allee_braises")
                .prequest("bones_and_badges").level(5)
                .visit("allee_braises").talk("hanna_emberwright")
                .handIn("hanna_emberwright")
                .xp(80).gold(10)
                .leadsTo("burning_books", 8)
                .build());

        add(QuestDef.builder("burning_books")
                .fr("Livres Brulants").en("Burning Books")
                .summaryFr("Les elfes de feu brulent la bibliotheque du quartier.")
                .summaryEn("The fire elves are burning the district library.")
                .giver("hanna_emberwright").giverZone("allee_braises")
                .prequest("report_to_hanna").level(8)
                .defeat("ember_elf_hunter", 4).collect("minecraft:blaze_powder", 2)
                .xp(150).gold(18).item("grove_wand")
                .leadsTo("the_pathfinders", 9)
                .build());

        add(QuestDef.builder("the_pathfinders")
                .fr("Les Eclaireurs").en("The Pathfinders")
                .summaryFr("Les eclaireurs elfes guident les autres vers les maisons.")
                .summaryEn("Elf pathfinders are guiding the others to the houses.")
                .giver("hanna_emberwright").giverZone("allee_braises")
                .prequest("burning_books").level(9)
                .defeat("ember_elf_pathfinder", 3)
                .xp(170).gold(20).item("grove_hood")
                .leadsTo("magma_rising", 10)
                .build());

        add(QuestDef.builder("magma_rising")
                .fr("Le Magma Monte").en("Magma Rising")
                .summaryFr("Quelque chose de plus gros marche dans les ruelles.")
                .summaryEn("Something bigger is walking in the alleys.")
                .giver("hanna_emberwright").giverZone("allee_braises")
                .prequest("the_pathfinders").level(10)
                .defeat("magma_man", 2)
                .xp(200).gold(22).item("grove_robe")
                .leadsTo("quinns_orders", 11)
                .build());

        add(QuestDef.builder("quinns_orders")
                .fr("Les Ordres de Quinn").en("Quinn's Orders")
                .summaryFr("Le soldat Quinn veut les tetes des lieutenants des elfes.")
                .summaryEn("Private Quinn wants the elf lieutenants taken down.")
                .giver("private_quinn").giverZone("allee_braises")
                .prequest("magma_rising").level(11)
                .defeat("gretta_darkkettle", 1).defeat("wormguts", 1)
                .xp(240).gold(26).item("grove_ring")
                .leadsTo("the_spirit_speaks", 11)
                .build());

        add(QuestDef.builder("the_spirit_speaks")
                .fr("L'Esprit Parle").en("The Spirit Speaks")
                .summaryFr("Un esprit de feu sait ou se cache le capitaine elfe.")
                .summaryEn("A fire spirit knows where the elf captain hides.")
                .giver("private_quinn").giverZone("allee_braises")
                .prequest("quinns_orders").level(11)
                .talk("volcano_spirit")
                .handIn("volcano_spirit")
                .xp(60).gold(8)
                .leadsTo("gravewynds_bastion", 11)
                .build());

        add(QuestDef.builder("gravewynds_bastion")
                .fr("Le Bastion de Sepulcra").en("Sepulcra's Bastion")
                .summaryFr("Sepulcra Ombrevent leve des morts derriere le bastion.")
                .summaryEn("Sepulcra Gravewynd is raising the dead behind the bastion.")
                .giver("volcano_spirit").giverZone("allee_braises")
                .prequest("the_spirit_speaks").level(11)
                .defeat("bastilla_gravewynd", 1)
                .handIn("volcano_spirit")
                .xp(350).gold(45).item("ember_cowl")
                .leadsTo("swiftarrow_ambush", 12)
                .build());

        add(QuestDef.builder("swiftarrow_ambush")
                .fr("L'Embuscade d'Alcane").en("Swiftarrow's Ambush")
                .summaryFr("Alcane Vive-Fleche vous attend avec ses meilleurs archers.")
                .summaryEn("Alcane Swiftarrow waits with her best archers.")
                .giver("volcano_spirit").giverZone("allee_braises")
                .prequest("gravewynds_bastion").level(12)
                .defeat("alicane_swiftarrow", 1)
                .handIn("volcano_spirit")
                .xp(400).gold(50).item("ember_wand")
                .leadsTo("tide_trouble", 12)
                .build());

        // ---------------------------------------------------------- l'avenue des marees
        add(QuestDef.builder("tide_trouble")
                .fr("Problemes de Maree").en("Tide Trouble")
                .summaryFr("Le port est sous l'eau et les noyes en sortent la nuit.")
                .summaryEn("The harbour is flooded and the drowned walk at night.")
                .giver("captain_marrow").giverZone("avenue_marees")
                .prequest("swiftarrow_ambush").level(12)
                .defeat("rotting_fodder", 3)
                .xp(180).gold(22).item("grove_boots")
                .leadsTo("screaming_shores", 12)
                .build());

        add(QuestDef.builder("screaming_shores")
                .fr("Les Rives Hurlantes").en("Screaming Shores")
                .summaryFr("Les hurleurs ecarlates font rater les sorts des pecheurs.")
                .summaryEn("The scarlet screamers make the fishers fizzle.")
                .giver("captain_marrow").giverZone("avenue_marees")
                .prequest("tide_trouble").level(12)
                .defeat("scarlet_screamer", 4).defeat("spellwrit_screamer", 2)
                .xp(260).gold(30).item("grove_amulet")
                .leadsTo("drowning_fears", 13)
                .build());

        add(QuestDef.builder("drowning_fears")
                .fr("Peur de Se Noyer").en("Drowning Fears")
                .summaryFr("Les vieilles peurs remontent avec la maree.")
                .summaryEn("Old fears come back up with the tide.")
                .giver("captain_marrow").giverZone("avenue_marees")
                .prequest("screaming_shores").level(13)
                .defeat("fear_of_drowning", 3).defeat("crypt_walker", 2)
                .xp(280).gold(32).item("grove_deck")
                .leadsTo("the_eel_deal", 13)
                .build());

        add(QuestDef.builder("the_eel_deal")
                .fr("Le Marche des Anguilles").en("The Eel Deal")
                .summaryFr("Les anguilles electriques gardent les docks.")
                .summaryEn("Electric eels are guarding the docks.")
                .giver("captain_marrow").giverZone("avenue_marees")
                .prequest("drowning_fears").level(13)
                .defeat("electric_eel", 3).collect("minecraft:prismarine_shard", 3)
                .xp(300).gold(35).item("tide_crown")
                .leadsTo("kraken_wakes", 14)
                .build());

        add(QuestDef.builder("pikes_net")
                .fr("Les Filets de Pike").en("Pike's Net")
                .summaryFr("Le pecheur a perdu ses filets dans l'inondation.")
                .summaryEn("The fisherman lost his nets in the flood.")
                .giver("fisherman_pike").giverZone("avenue_marees").level(13).side(true)
                .prequest("the_eel_deal")
                .collect("minecraft:string", 4)
                .xp(120).gold(15).item("tide_ring")
                .build());

        add(QuestDef.builder("kraken_wakes")
                .fr("Le Kraken s'Eveille").en("The Kraken Wakes")
                .summaryFr("Quelque chose remue sous le ponton.")
                .summaryEn("Something is moving under the dock.")
                .giver("captain_marrow").giverZone("avenue_marees")
                .prequest("the_eel_deal").level(14)
                .defeat("tide_kraken", 1)
                .xp(450).gold(60).item("tide_robe")
                .leadsTo("the_harvest_lord_quest", 15)
                .build());

        add(QuestDef.builder("the_harvest_lord_quest")
                .fr("Le Seigneur des Moissons").en("The Harvest Lord")
                .summaryFr("Le maitre des noyes attend au bout de l'avenue.")
                .summaryEn("The master of the drowned waits at the end of the avenue.")
                .giver("captain_marrow").giverZone("avenue_marees")
                .prequest("kraken_wakes").level(15)
                .defeat("the_harvest_lord", 1)
                .xp(550).gold(70).item("tide_wand")
                .leadsTo("road_of_colossi", 15)
                .build());

        // --------------------------------------------------------- le chemin des colosses
        add(QuestDef.builder("road_of_colossi")
                .fr("Le Chemin des Colosses").en("The Colossus Road")
                .summaryFr("La gardienne du chemin a besoin d'aide.")
                .summaryEn("The warden of the road needs help.")
                .giver("warden_ashwind").giverZone("chemin_colosses")
                .prequest("the_harvest_lord_quest").level(15)
                .talk("warden_ashwind")
                .xp(100).gold(12)
                .leadsTo("cold_shoulders", 15)
                .build());

        add(QuestDef.builder("cold_shoulders")
                .fr("Epaules Froides").en("Cold Shoulders")
                .summaryFr("Les trolls des glaces bloquent la route des statues.")
                .summaryEn("Ice trolls are blocking the statue road.")
                .giver("warden_ashwind").giverZone("chemin_colosses")
                .prequest("road_of_colossi").level(15)
                .defeat("ice_troll", 4)
                .xp(320).gold(38).item("tide_boots")
                .leadsTo("sentinels_stone", 16)
                .build());

        add(QuestDef.builder("sentinels_stone")
                .fr("La Pierre des Sentinelles").en("Sentinels of Stone")
                .summaryFr("Les gardiens de pierre ne dorment plus.")
                .summaryEn("The stone guardians no longer sleep.")
                .giver("warden_ashwind").giverZone("chemin_colosses")
                .prequest("cold_shoulders").level(16)
                .defeat("colossus_sentinel", 3).collect("minecraft:blue_ice", 4)
                .xp(380).gold(45).item("tide_amulet")
                .leadsTo("shades_of_night", 16)
                .build());

        add(QuestDef.builder("shades_of_night")
                .fr("Ombres Nocturnes").en("Shades of Night")
                .summaryFr("Les ombres du creux montent jusqu'a la route.")
                .summaryEn("The hollow's shades are climbing up to the road.")
                .giver("warden_ashwind").giverZone("chemin_colosses")
                .prequest("sentinels_stone").level(16)
                .defeat("night_shade", 3).defeat("rune_marauder", 2)
                .xp(420).gold(50).item("tide_deck")
                .leadsTo("ashes_of_the_road", 17)
                .build());

        add(QuestDef.builder("ashes_of_the_road")
                .fr("Cendres sur la Route").en("Ashes on the Road")
                .summaryFr("Les diablotins maraudeurs volent les offrandes des statues.")
                .summaryEn("Marauder imps are stealing the statues' offerings.")
                .giver("warden_ashwind").giverZone("chemin_colosses")
                .prequest("shades_of_night").level(17)
                .defeat("marauder_imp", 4)
                .xp(360).gold(42).item("tide_athame")
                .leadsTo("hollow_beckons", 17)
                .build());

        // ------------------------------------------------------------ la grotte hantee
        add(QuestDef.builder("hollow_beckons")
                .fr("Le Creux Appelle").en("The Hollow Beckons")
                .summaryFr("Un apprenti est prisonnier dans la grotte.")
                .summaryEn("An apprentice is trapped inside the hollow.")
                .giver("lost_apprentice").giverZone("grotte_hantee")
                .prequest("ashes_of_the_road").level(17)
                .visit("grotte_hantee").talk("lost_apprentice")
                .xp(150).gold(20)
                .leadsTo("lost_apprentice_quest", 17)
                .build());

        add(QuestDef.builder("lost_apprentice_quest")
                .fr("L'Apprenti Perdu").en("The Lost Apprentice")
                .summaryFr("Brisez la legion d'os qui garde l'apprenti.")
                .summaryEn("Break the bone legion guarding the apprentice.")
                .giver("lost_apprentice").giverZone("grotte_hantee")
                .prequest("hollow_beckons").level(17)
                .defeat("bone_legion", 3).defeat("corsair_bones", 1)
                .xp(400).gold(45).item("hollow_crown")
                .leadsTo("the_hollow_lord", 18)
                .build());

        add(QuestDef.builder("the_hollow_lord")
                .fr("Le Seigneur du Creux").en("The Hollow Lord")
                .summaryFr("Le premier des lieutenants du necromancien garde le passage.")
                .summaryEn("The necromancer's first lieutenant guards the way down.")
                .giver("lost_apprentice").giverZone("grotte_hantee")
                .prequest("lost_apprentice_quest").level(18)
                .defeat("hollow_lord", 1)
                .xp(800).gold(90).item("hollow_robe")
                .leadsTo("end_of_the_hollow", 19)
                .build());

        add(QuestDef.builder("end_of_the_hollow")
                .fr("La Fin du Creux").en("The End of the Hollow")
                .summaryFr("Le Grand Necromancien vous attend au fond de la grotte.")
                .summaryEn("The Grand Necromancer waits at the bottom of the hollow.")
                .giver("lost_apprentice").giverZone("grotte_hantee")
                .prequest("the_hollow_lord").level(19)
                .defeat("grand_necromancer", 1)
                .xp(1200).gold(150).item("hollow_wand").choice("hollow_athame").choice("hollow_amulet")
                .build());

        // ------------------------------------------------------- les epreuves d'ecole
        schoolTrial("school_trial_pyromancy", "Pyromancie", "Pyromancy", "ember_elf_hunter", 3,
                "minecraft:blaze_powder", 1, "sunhawk", "pyromancy_athame");
        schoolTrial("school_trial_tempest", "Tempete", "Tempest", "scarlet_screamer", 3,
                "minecraft:prismarine_shard", 1, "riptide_shark", "tempest_athame");
        schoolTrial("school_trial_frost", "Givre", "Frost", "colossus_sentinel", 2,
                "minecraft:blue_ice", 2, "glacial_aegis", "frost_athame");
        schoolTrial("school_trial_runomancy", "Runomancie", "Runomancy", "ice_troll", 3,
                "minecraft:amethyst_shard", 2, "rune_cyclops", "runomancy_athame");
        schoolTrial("school_trial_sylvan", "Sylve", "Sylvan", "rotting_fodder", 3,
                "minecraft:wheat", 3, "unicorn_blessing", "sylvan_athame");
        schoolTrial("school_trial_necromancy", "Necromancie", "Necromancy", "corsair_bones", 2,
                "minecraft:bone", 6, "wraith", "necromancy_athame");
        schoolTrial("school_trial_equilibrium", "Equilibre", "Equilibrium", "night_shade", 2,
                "minecraft:gold_nugget", 3, "judgement", "equilibrium_athame");
    }

    /** Une epreuve d'ecole : vaincre des creatures de sa zone et rapporter un reagent. */
    private static void schoolTrial(String id, String frSchool, String enSchool, String creatureId,
                                    int creatureCount, String reagent, int reagentCount,
                                    String rewardSpell, String rewardGear) {
        String schoolId = switch (enSchool) {
            case "Pyromancy" -> "pyromancy";
            case "Tempest" -> "tempest";
            case "Frost" -> "frost";
            case "Runomancy" -> "runomancy";
            case "Sylvan" -> "sylvan";
            case "Necromancy" -> "necromancy";
            default -> "equilibrium";
        };
        add(QuestDef.builder(id)
                .fr("Epreuve de " + frSchool).en(enSchool + " Trial")
                .summaryFr("Le professeur de " + frSchool + " veut voir vos progres.")
                .summaryEn("The " + enSchool + " professor wants to see your progress.")
                .giver("prof_" + professorId(schoolId)).giverZone("bosquet")
                .level(10).side(true)
                .defeat(creatureId, creatureCount).collect(reagent, reagentCount)
                .xp(200).gold(20).spell(rewardSpell).item(rewardGear)
                .build());
    }

    private static String professorId(String schoolId) {
        return switch (schoolId) {
            case "pyromancy" -> "cinderlune";
            case "tempest" -> "stormvale";
            case "frost" -> "frostbloom";
            case "runomancy" -> "cyren";
            case "sylvan" -> "greenearth";
            case "necromancy" -> "ossuary";
            default -> "whitequill";
        };
    }

    /**
     * Verifie la coherence du contenu : chaque reference (creature, PNJ, zone,
     * sort, equipement) doit exister. Appele par {@code ArcanumCore.init()}.
     */
    public static List<String> validate() {
        List<String> problems = new ArrayList<>();
        for (QuestDef quest : DEFS.values()) {
            if (quest.giverNpc() == null || Npcs.find(quest.giverNpc()).isEmpty()) {
                problems.add("Quete " + quest.id() + " : donneur inconnu " + quest.giverNpc());
            }
            if (quest.handInNpc() == null || Npcs.find(quest.handInNpc()).isEmpty()) {
                problems.add("Quete " + quest.id() + " : remettant inconnu " + quest.handInNpc());
            }
            if (quest.giverZone() != null && Zones.find(quest.giverZone()).isEmpty()) {
                problems.add("Quete " + quest.id() + " : zone inconnue " + quest.giverZone());
            }
            if (quest.prequest() != null && !DEFS.containsKey(quest.prequest())) {
                problems.add("Quete " + quest.id() + " : prequelle inconnue " + quest.prequest());
            }
            if (quest.leadsTo() != null && !DEFS.containsKey(quest.leadsTo())) {
                problems.add("Quete " + quest.id() + " : suite inconnue " + quest.leadsTo());
            }
            for (QuestGoal goal : quest.goals()) {
                if (goal instanceof QuestGoal.Defeat d && Bestiary.find(d.creatureId()).isEmpty()) {
                    problems.add("Quete " + quest.id() + " : creature inconnue " + d.creatureId());
                }
                if (goal instanceof QuestGoal.Talk t && Npcs.find(t.npcId()).isEmpty()) {
                    problems.add("Quete " + quest.id() + " : PNJ inconnu " + t.npcId());
                }
                if (goal instanceof QuestGoal.Visit v && Zones.find(v.zoneId()).isEmpty()) {
                    problems.add("Quete " + quest.id() + " : zone inconnue " + v.zoneId());
                }
            }
            if (quest.rewardSpell() != null
                    && com.arcanumrealms.core.spell.SpellBook.find(quest.rewardSpell()).isEmpty()) {
                problems.add("Quete " + quest.id() + " : sort recompense inconnu " + quest.rewardSpell());
            }
            for (String gear : quest.rewardChoices()) {
                if (Gears.find(gear).isEmpty()) {
                    problems.add("Quete " + quest.id() + " : equipement inconnu " + gear);
                }
            }
            if (quest.rewardItem() != null && Gears.find(quest.rewardItem()).isEmpty()) {
                problems.add("Quete " + quest.id() + " : equipement inconnu " + quest.rewardItem());
            }
        }
        return problems;
    }
}
