package com.arcanumrealms.core.world;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.arcanumrealms.core.school.School.*;

/**
 * Les habitants de la Cite des Arcanes.
 *
 * <p>Comme sur le wiki, chaque PNJ indique sa zone, sa description, les quetes
 * qu'il donne et celles qu'il rend. Ses repliques sont originales : le wiki sert
 * de reference pour qui parle, ou et pour quelle quete.
 */
public final class Npcs {

    private static final Map<String, NpcDef> DEFS = new LinkedHashMap<>();

    private Npcs() {}

    public static NpcDef get(String id) {
        NpcDef def = DEFS.get(id);
        if (def == null) throw new IllegalArgumentException("PNJ inconnu: " + id);
        return def;
    }

    public static Optional<NpcDef> find(String id) {
        return Optional.ofNullable(DEFS.get(id));
    }

    public static Collection<NpcDef> all() {
        return Collections.unmodifiableCollection(DEFS.values());
    }

    public static List<NpcDef> ofZone(String zoneId) {
        return DEFS.values().stream().filter(n -> zoneId.equals(n.zoneId())).toList();
    }

    public static List<NpcDef> trainers() {
        return DEFS.values().stream().filter(NpcDef::isTrainer).toList();
    }

    private static void add(NpcDef def) {
        if (DEFS.put(def.id(), def) != null) throw new IllegalStateException("PNJ en double: " + def.id());
    }

    private static final String NPC_ENTITY = "arcanum_realms:wizard_npc";

    /** Aide a ecrire un professeur sans se repeter. */
    private static void teacher(String id, String en, String fr, String titleEn, String titleFr,
                                com.arcanumrealms.core.school.School school, int outfit, int level,
                                Dialogue greeting, Dialogue quest, Dialogue done) {
        add(NpcDef.builder(id)
                .name(en, fr).title(titleEn, titleFr)
                .zone("bosquet").role(NpcRole.TRAINER).school(school).level(level)
                .entity(NPC_ENTITY).outfit(outfit)
                .description("Teaches " + school.displayEn() + " magic in the grove.",
                        "Enseigne la magie " + school.displayFr() + " dans le bosquet.")
                .greeting(greeting).inProgress(quest).completion(done)
                .farewell(Dialogue.of(
                        "Practise your spells, young wizard.",
                        "Entrainez vos sorts, jeune sorcier."))
                .gives("school_trial_" + school.id())
                .ends("school_trial_" + school.id())
                .build());
    }

    static {
        // ------------------------------------------------------------ la grand-place
        add(NpcDef.builder("headmaster_alderwise")
                .name("Headmaster Alderwise", "Directeur Sage-Aulne")
                .title("Headmaster of the Arcane City", "Directeur de la Cite des Arcanes")
                .zone("grand_place").role(NpcRole.QUEST_GIVER).level(50)
                .entity(NPC_ENTITY).outfit(0x8E6BC8)
                .description("Runs the city and hands out the first errands.",
                        "Dirige la cite et confie les premieres commissions.")
                .greeting(Dialogue.of(
                        "Welcome to the Arcane City, young wizard. Fresh from the grove, I see.",
                        "Bienvenue dans la Cite des Arcanes, jeune sorcier. Tout droit sorti du bosquet, je vois.",
                        "The streets are restless. Spirits walk the Firefly Path and the harbour is drowned.",
                        "Les rues sont agitees. Les esprits rôdent sur le Sentier des Lucioles et le port est noye.",
                        "Find Lea Nightchant on the path. She will show you how a duel is won.",
                        "Trouvez Lea Chantenuit sur le sentier. Elle vous montrera comment on gagne un duel."))
                .inProgress(Dialogue.of(
                        "Lea is waiting for you on the Firefly Path.",
                        "Lea vous attend sur le Sentier des Lucioles."))
                .completion(Dialogue.of(
                        "You have a wizard's hands already. Good - the city will need them.",
                        "Vous avez deja des mains de sorcier. Tant mieux : la cite aura besoin de vous.",
                        "Come back when the Firefly Path is quiet. There is more work in Ember Alley.",
                        "Revenez quand le Sentier sera calme. Il y a du travail a l'Allee des Braises."))
                .farewell(Dialogue.of(
                        "May your pips turn golden.",
                        "Que vos pips deviennent dores."))
                .gives("welcome_to_the_city").ends("welcome_to_the_city")
                .build());

        add(NpcDef.builder("shopkeeper_beatrice")
                .name("Beatrice", "Beatrice")
                .title("Keeper of the gear shop", "Tenanciere de la boutique")
                .zone("grand_place").role(NpcRole.SHOPKEEPER).level(20)
                .entity(NPC_ENTITY).outfit(0xD9A93C)
                .description("Buys nothing, sells everything, and knows every rumour.",
                        "N'achete rien, vend tout, et connait toutes les rumeurs.")
                .greeting(Dialogue.of(
                        "Hats, robes, boots, wands! Everything a wizard needs to stop fizzling.",
                        "Chapeaux, robes, bottes, baguettes ! Tout ce qu'il faut pour arreter de rater ses sorts.",
                        "Creatures drop gear when you defeat them, you know. Wear the best you find.",
                        "Les creatures lachent de l'equipement quand vous les battez. Portez le meilleur.",
                        "Use /arcanum gear to see what you are wearing.",
                        "Utilisez /arcanum gear pour voir ce que vous portez."))
                .farewell(Dialogue.of("Come back with a full purse!", "Revenez avec une bourse pleine !"))
                .build());

        add(NpcDef.builder("guard_quentin")
                .name("Sentinel Quentin", "Sentinelle Quentin")
                .title("Gate watch", "Garde de la porte")
                .zone("grand_place").role(NpcRole.GUARD).level(15)
                .entity(NPC_ENTITY).outfit(0x607D8B)
                .description("Old soldier. Points travellers towards the right street.",
                        "Vieux soldat. Indique aux voyageurs la bonne rue.")
                .greeting(Dialogue.of(
                        "Keep your wand up and your deck full. That is my only advice.",
                        "Gardez votre baguette haute et votre deck plein. C'est mon seul conseil.",
                        "Ember Alley is east, the tide harbour south. Both are dangerous.",
                        "L'Allee des Braises est a l'est, le port au sud. Les deux sont dangereux."))
                .farewell(Dialogue.of("Stay alive out there.", "Restez en vie, la-bas."))
                .build());

        // ------------------------------------------------------------ les professeurs
        teacher("prof_cinderlune", "Professor Cinderlune", "Professeure Cendrelune",
                "Professor of Pyromancy", "Professeure de Pyromancie", PYROMANCY, 0xE4592B, 40,
                Dialogue.of("Fire is not anger, child. Fire is patience that finally moves.",
                        "Le feu n'est pas de la colere, petit. Le feu est de la patience qui se decide enfin."),
                Dialogue.of("Bring me proof from the Ember Alley elves.",
                        "Rapportez-moi une preuve prise aux elfes de l'Allee des Braises."),
                Dialogue.of("A shard of their flame. Good. Now watch how a real Pyromancer uses it.",
                        "Un eclat de leur flamme. Bien. Regardez maintenant comment un vrai pyromancien l'utilise."));

        teacher("prof_stormvale", "Professor Stormvale", "Professeur Orageval",
                "Professor of Tempest", "Professeur de Tempete", TEMPEST, 0x7A55D6, 40,
                Dialogue.of("Storm magic is fast, loud and often inaccurate. Like me.",
                        "La magie de Tempete est rapide, bruyante et souvent imprecise. Comme moi."),
                Dialogue.of("The tide harbour is full of drowned things. Zap a few for me.",
                        "Le port est plein de noyes. Foudroyez-en quelques-uns pour moi."),
                Dialogue.of("Ha! Sloppy, but it worked. You will do.",
                        "Ha ! Maladroit, mais efficace. Vous ferez l'affaire."));

        teacher("prof_frostbloom", "Professor Frostbloom", "Professeure Givrefleur",
                "Professor of Frost", "Professeure de Givre", FROST, 0x74C4E8, 40,
                Dialogue.of("Frost wizards are the last ones standing. Slow, and last.",
                        "Les sorciers de Givre sont ceux qui restent debout le plus longtemps. Lents, et tenaces."),
                Dialogue.of("Raise a shield against the road guardians and come back.",
                        "Elevez un bouclier contre les gardiens du chemin, puis revenez."),
                Dialogue.of("You kept your guard up. That is the whole secret of Frost.",
                        "Vous avez garde votre garde. Tout le secret du Givre est la."));

        teacher("prof_cyren", "Professor Cyren", "Professeur Cyren",
                "Professor of Runomancy", "Professeur de Runomancie", RUNOMANCY, 0xD9A93C, 40,
                Dialogue.of("Myth magic is older than the city. Respect the runes and they obey.",
                        "La magie Runique est plus vieille que la cite. Respectez les runes et elles obeissent."),
                Dialogue.of("Break a few trolls on the Colossus Road and bring me their runes.",
                        "Brisez quelques trolls sur le Chemin des Colosses et rapportez-moi leurs runes."),
                Dialogue.of("Strong runes, strong wizard. You are learning.",
                        "Runes solides, sorcier solide. Vous apprenez."));

        teacher("prof_greenearth", "Professor Greenearth", "Professeure Verdeterre",
                "Professor of Sylvan", "Professeure de Sylve", SYLVAN, 0x5FBF63, 40,
                Dialogue.of("Life magic mends. It also takes the longest to learn.",
                        "La magie Sylvestre repare. C'est aussi la plus longue a apprendre."),
                Dialogue.of("Heal your friends in a duel, then tell me how it went.",
                        "Soignez vos amis en duel, puis racontez-moi."),
                Dialogue.of("You healed instead of hitting. That is what makes a Life wizard.",
                        "Vous avez soigne au lieu de frapper. C'est ce qui fait un sorcier de Sylve."));

        teacher("prof_ossuary", "Professor Ossuary", "Professeur Osseaume",
                "Professor of Necromancy", "Professeur de Necromancie", NECROMANCY, 0x6B7280, 40,
                Dialogue.of("Death is not evil, child. It is only very patient.",
                        "La mort n'est pas mauvaise, petit. Elle est seulement tres patiente."),
                Dialogue.of("The hollow is full of bones. Bring some back.",
                        "Le creux est plein d'ossements. Rapportez-en."),
                Dialogue.of("Bones, dust, and a new spell. Do not waste it.",
                        "Des os, de la poussiere, et un nouveau sort. Ne le gaspillez pas."));

        teacher("prof_whitequill", "Professor Whitequill", "Professeur Plumesage",
                "Professor of Equilibrium", "Professeur d'Equilibre", EQUILIBRIUM, 0xD7C08D, 40,
                Dialogue.of("Balance is not neutrality. Balance is knowing which side to push.",
                        "L'equilibre n'est pas la neutralite. C'est savoir quel cote pousser."),
                Dialogue.of("Your blade must work for every school at once. Show me.",
                        "Votre lame doit servir toutes les ecoles a la fois. Montrez-moi."),
                Dialogue.of("Now every card in your hand is stronger. Use it well.",
                        "Desormais chaque carte de votre main est plus forte. Usez-en bien."));

        // ------------------------------------------------------- le sentier des lucioles
        add(NpcDef.builder("lea_nightchant")
                .name("Lea Nightchant", "Lea Chantenuit")
                .title("Guardian of the Firefly Path", "Gardienne du Sentier des Lucioles")
                .zone("sentier_lucioles").role(NpcRole.QUEST_GIVER).level(12)
                .entity(NPC_ENTITY).outfit(0x5FBF63)
                .description("Keeps the path clear of restless spirits.",
                        "Debarrasse le sentier des esprits agites.")
                .greeting(Dialogue.of(
                        "Oh, a new face! The spirits have been wandering again.",
                        "Oh, un nouveau visage ! Les esprits se remettent a rôder.",
                        "They will duel you whether you want it or not. Better to be ready.",
                        "Ils vous defieront que vous le vouliez ou non. Mieux vaut etre pret.",
                        "Try them: cast a card, then pick your target. Watch your pips.",
                        "Essayez : lancez une carte, puis choisissez votre cible. Surveillez vos pips."))
                .inProgress(Dialogue.of(
                        "The restless ones are still out there. Keep your deck full.",
                        "Les agites sont toujours la. Gardez votre deck plein."))
                .completion(Dialogue.of(
                        "You handled yourself well! Here, take this.",
                        "Vous vous en etes bien sorti ! Tenez, prenez ceci.",
                        "The path is quieter now. The city will send you up to Ember Alley soon.",
                        "Le sentier est plus calme. La cite vous enverra bientot a l'Allee des Braises."))
                .farewell(Dialogue.of(
                        "Rest, then come back for more work.",
                        "Reposez-vous, puis revenez pour la suite."))
                .gives("first_steps", "unwelcome_spirits", "bones_and_badges", "report_to_hanna", "pixie_problem", "sprite_trouble", "scarabs_in_the_grass", "ticklin_da_bones", "nightmare_time")
                .ends("first_steps", "unwelcome_spirits", "bones_and_badges", "pixie_problem", "sprite_trouble", "scarabs_in_the_grass", "ticklin_da_bones", "nightmare_time")
                .build());

        add(NpcDef.builder("melisande_whitehorn")
                .name("Melisande Whitehorn", "Melisande Corneblanche")
                .title("Herb gatherer", "Cueilleuse d'herbes")
                .zone("sentier_lucioles").role(NpcRole.VILLAGER).level(8)
                .entity(NPC_ENTITY).outfit(0x9CCC65)
                .description("Collects reagents along the path and trades stories.",
                        "Ramasse des reactifs le long du sentier et echange des histoires.")
                .greeting(Dialogue.of(
                        "Mind the pixies. They bite, and they fizzle you first.",
                        "Attention aux lutins. Ils mordent, et ils vous font rater vos sorts.",
                        "If you find amethyst shards, keep them. They are worth good gold.",
                        "Si vous trouvez des eclats d'amethyste, gardez-les. Ils valent de l'or."))
                .farewell(Dialogue.of("Good luck, wizard.", "Bonne chance, sorcier."))
                .build());

        // ---------------------------------------------------------- l'allee des braises
        add(NpcDef.builder("hanna_emberwright")
                .name("Hanna Emberwright", "Hanna Brassefonte")
                .title("House-sitting for the Fire professor", "Garde la maison de la professeure de feu")
                .zone("allee_braises").role(NpcRole.QUEST_GIVER).level(14)
                .entity(NPC_ENTITY).outfit(0xE4592B)
                .description("Watches the professor's house while the elves burn the street.",
                        "Surveille la maison de la professeure pendant que les elfes brulent la rue.")
                .greeting(Dialogue.of(
                        "Careful! The elves set the library on fire last week.",
                        "Attention ! Les elfes ont mis le feu a la bibliotheque la semaine derniere.",
                        "I need someone to push them back and save what books are left.",
                        "J'ai besoin de quelqu'un pour les repousser et sauver les livres restants."))
                .inProgress(Dialogue.of(
                        "The elves are still out there, and the books are still burning.",
                        "Les elfes sont toujours la, et les livres brulent encore."))
                .completion(Dialogue.of(
                        "The professor will be pleased. Take a wand from the cupboard.",
                        "La professeure sera contente. Prenez une baguette dans le placard.",
                        "Watch for their pathfinders - they strike before you can shield.",
                        "Mefiez-vous de leurs eclaireurs : ils frappent avant qu'on puisse se proteger."))
                .farewell(Dialogue.of("Come back if they regroup.", "Revenez s'ils se rallient."))
                .gives("she_ran_crying_wildfire", "investigate_alley", "point_taken", "the_cure", "administer_cure", "oil_rush")
                .ends("report_to_hanna", "she_ran_crying_wildfire", "investigate_alley", "point_taken", "the_cure", "administer_cure", "oil_rush")
                .build());

        add(NpcDef.builder("private_quinn")
                .name("Private Quinn", "Soldat Quinn")
                .title("City watch, Ember Alley post", "Garde de la cite, poste de l'Allee des Braises")
                .zone("allee_braises").role(NpcRole.GUARD).level(16)
                .entity(NPC_ENTITY).outfit(0x8D6E63)
                .description("Holds the barricade at the end of the alley.",
                        "Tient la barricade au bout de l'alley.")
                .greeting(Dialogue.of(
                        "Three of my patrol are still missing. I hold this line alone.",
                        "Trois de ma patrouille manquent encore. Je tiens cette ligne seul.",
                        "If you clear the alley, I can go look for them.",
                        "Si vous nettoyez l'alley, je pourrai partir les chercher."))
                .inProgress(Dialogue.of("The line holds... for now.", "La ligne tient... pour l'instant."))
                .completion(Dialogue.of(
                        "Thank you. I am going to find my people.",
                        "Merci. Je vais chercher les miens."))
                .farewell(Dialogue.of("Hold the line, wizard.", "Tenez la ligne, sorcier."))
                .gives("danger_danger", "dire_news", "cold_hard_steel", "magma_mania", "gotta_see_gretta").ends("danger_danger", "dire_news", "cold_hard_steel", "magma_mania", "gotta_see_gretta")
                .build());

        add(NpcDef.builder("volcano_spirit")
                .name("Volcano Spirit", "Esprit du Volcan")
                .title("Old fire, older anger", "Vieux feu, vieille colere")
                .zone("allee_braises").role(NpcRole.VILLAGER).level(20)
                .entity(NPC_ENTITY).outfit(0xFF7043)
                .description("Speaks in short, hot sentences.",
                        "Parle par phrases courtes et brulantes.")
                .greeting(Dialogue.of(
                        "You smell of parchment. The elves smell of ash. Both burn.",
                        "Vous sentez le parchemin. Les elfes sentent la cendre. Les deux brulent.",
                        "Their captain, Alcane, hides in the bastion. Bring ice.",
                        "Leur capitaine, Alcane, se cache dans le bastion. Prenez de la glace."))
                .inProgress(Dialogue.of(
                        "The bastion is hot. You are not ready.",
                        "Le bastion est brulant. Vous n'etes pas pret."))
                .completion(Dialogue.of(
                        "Cooled at last. The street can breathe.",
                        "Enfin refroidi. La rue peut respirer."))
                .farewell(Dialogue.of("Burn bright, not out.", "Brulez fort, pas jusqu'au bout."))
                .gives("which_craft", "which_hunt", "not_that_witch", "the_embercat", "swiftarrow_ambush")
                .ends("which_craft", "which_hunt", "not_that_witch", "the_embercat", "swiftarrow_ambush")
                .build());

        // ---------------------------------------------------------- l'avenue des marees
        add(NpcDef.builder("captain_marrow")
                .name("Captain Marrow", "Capitaine Moelle")
                .title("Harbour watch", "Garde du port")
                .zone("avenue_marees").role(NpcRole.QUEST_GIVER).level(18)
                .entity(NPC_ENTITY).outfit(0x4A6FA5)
                .description("Lost his ship and most of his crew to the flood.",
                        "A perdu son navire et presque tout son equipage dans l'inondation.")
                .greeting(Dialogue.of(
                        "The tide came in and never went out. My crew went with it.",
                        "La maree est montee et n'est jamais redescendue. Mon equipage est parti avec.",
                        "They walk the avenue now, rotting and screaming. Put them to rest.",
                        "Ils arpentent l'avenue maintenant, pourrissants et hurlants. Faites-les reposer.",
                        "Careful with the Scarlets: they scream and your spells fizzle.",
                        "Attention aux Ecarlates : ils hurlent et vos sorts ratent."))
                .inProgress(Dialogue.of(
                        "The avenue is still loud. Do what you can.",
                        "L'avenue est toujours bruyante. Faites ce que vous pouvez."))
                .completion(Dialogue.of(
                        "Then it is done. You have my thanks, and the harbour's.",
                        "Alors c'est fait. Vous avez mes remerciements, et ceux du port."))
                .farewell(Dialogue.of("The sea keeps what it takes.", "La mer garde ce qu'elle prend."))
                .gives("tide_trouble", "screaming_shores", "drowning_fears", "the_eel_deal",
                        "kraken_wakes", "the_harvest_lord_quest")
                .ends("tide_trouble", "screaming_shores", "drowning_fears", "the_eel_deal",
                        "kraken_wakes", "the_harvest_lord_quest")
                .build());

        add(NpcDef.builder("fisherman_pike")
                .name("Fisherman Pike", "Pecheur Pike")
                .title("Still fishing", "Peche toujours")
                .zone("avenue_marees").role(NpcRole.VILLAGER).level(10)
                .entity(NPC_ENTITY).outfit(0x66BB6A)
                .description("Refuses to leave the harbour, despite everything.",
                        "Refuse de quitter le port, malgre tout.")
                .greeting(Dialogue.of(
                        "Lost my nets in the flood. Found three eels instead.",
                        "Perdu mes filets dans l'inondation. Trouve trois anguilles a la place.",
                        "Bring me back my nets and I will show you where the Kraken sleeps.",
                        "Rapportez-moi mes filets et je vous montrerai ou dort le Kraken."))
                .inProgress(Dialogue.of("No nets yet? The fish are laughing at me.", "Pas encore de filets ? Les poissons se moquent de moi."))
                .completion(Dialogue.of(
                        "My nets! And look - something big is moving under the dock.",
                        "Mes filets ! Et regardez : quelque chose de gros bouge sous le ponton."))
                .farewell(Dialogue.of("Tight lines, wizard.", "Bonnes prises, sorcier."))
                .gives("pikes_net").ends("pikes_net")
                .build());

        // --------------------------------------------------------- le chemin des colosses
        add(NpcDef.builder("warden_ashwind")
                .name("Warden Ashwind", "Gardienne Cendrevent")
                .title("Warden of the Colossus Road", "Gardienne du Chemin des Colosses")
                .zone("chemin_colosses").role(NpcRole.QUEST_GIVER).level(25)
                .entity(NPC_ENTITY).outfit(0x74C4E8)
                .description("Keeps the statues asleep. It is not going well.",
                        "Garde les statues endormies. Cela se passe mal.")
                .greeting(Dialogue.of(
                        "The statues on this road are not statues. They are guards.",
                        "Les statues de cette route ne sont pas des statues. Ce sont des gardes.",
                        "Something woke them. Find out what, and put them back to sleep.",
                        "Quelque chose les a reveillees. Decouvrez quoi, et rendormez-les.",
                        "Bring frost wards. Their ice hits very hard.",
                        "Prenez des protections de givre. Leur glace frappe tres fort."))
                .inProgress(Dialogue.of(
                        "Still awake, still walking. Hurry.",
                        "Toujours eveillees, toujours en marche. Hatez-vous."))
                .completion(Dialogue.of(
                        "Quiet again. Whatever woke them came from the hollow - be ready.",
                        "Calme a nouveau. Ce qui les a reveillees vient du creux : soyez pret."))
                .farewell(Dialogue.of("The road is yours, wizard.", "La route est a vous, sorcier."))
                .gives("road_of_colossi", "cold_shoulders", "sentinels_stone", "shades_of_night", "ashes_of_the_road", "frost_knight_fight")
                .ends("road_of_colossi", "cold_shoulders", "sentinels_stone", "shades_of_night", "ashes_of_the_road", "frost_knight_fight")
                .build());

        // ------------------------------------------------------------ la grotte hantee
        add(NpcDef.builder("lost_apprentice")
                .name("Lost Apprentice", "Apprenti Perdu")
                .title("Trapped in the hollow", "Prisonnier du creux")
                .zone("grotte_hantee").role(NpcRole.QUEST_GIVER).level(16)
                .entity(NPC_ENTITY).outfit(0xB0BEC5)
                .description("Followed the Grand Necromancer into the dark. Regrets it.",
                        "A suivi le Grand Necromancien dans le noir. Le regrette.")
                .greeting(Dialogue.of(
                        "You came down here? Then you are braver or more foolish than me.",
                        "Vous etes descendu ici ? Alors vous etes plus brave ou plus fou que moi.",
                        "He is building something out of the bones. Break his captains first.",
                        "Il construit quelque chose avec les os. Brisez d'abord ses capitaines.",
                        "Then we end it together. I can still cast, barely.",
                        "Ensuite nous en finirons ensemble. Je peux encore lancer des sorts, a peine."))
                .inProgress(Dialogue.of(
                        "His captains are still standing. And so are we. Barely.",
                        "Ses capitaines tiennent encore. Et nous aussi. A peine."))
                .completion(Dialogue.of(
                        "It is over. I am going home, and I am never lighting a candle again.",
                        "C'est fini. Je rentre chez moi, et je ne rallumerai plus jamais de bougie."))
                .farewell(Dialogue.of("Do not stay down here too long.", "Ne restez pas trop longtemps ici."))
                .gives("hollow_beckons", "lost_apprentice_quest", "the_hollow_lord", "end_of_the_hollow")
                .ends("hollow_beckons", "lost_apprentice_quest", "the_hollow_lord", "end_of_the_hollow")
                .build());
    }
}
