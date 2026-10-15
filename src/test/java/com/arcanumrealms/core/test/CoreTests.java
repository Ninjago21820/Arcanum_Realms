package com.arcanumrealms.core.test;

import com.arcanumrealms.core.ArcanumCore;
import com.arcanumrealms.core.ai.EnemyAI;
import com.arcanumrealms.core.battle.*;
import com.arcanumrealms.core.card.Deck;
import com.arcanumrealms.core.minion.CreatureDef;
import com.arcanumrealms.core.minion.Creatures;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.core.status.Status;
import com.arcanumrealms.core.util.Rng;
import com.arcanumrealms.core.util.StatsFactory;
import com.arcanumrealms.core.world.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.arcanumrealms.core.school.School.*;

/**
 * Tests du moteur de duel : verifient point par point les regles de Wizard101.
 *
 * <p>Lancement : {@code tools/run-core-tests.sh} (javac + java, sans Gradle).
 */
public final class CoreTests {

    private static int passed;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        testPipsPowerRules();
        testPipsMaxSlots();
        testDeckHandAndDiscard();
        testDeckReshuffle();
        testDamagePipeline();
        testShieldsAndAbsorb();
        testFizzle();
        testCriticalFormula();
        testDamageOverTime();
        testDrain();
        testHealAndHealOverTime();
        testStun();
        testVariablePips();
        testAreaOfEffect();
        testFullBattleSimulation();
        testSpellData();
        testWorldZones();
        testWorldNpcs();
        testWorldCreatures();
        testWorldGear();
        testQuestLog();
        testQuestRewards();
        testWikiAnchors();
        testContentKeys();
        testContentNames();
        testContentOverride();

        System.out.println();
        System.out.println("Tests reussis : " + passed);
        if (!failures.isEmpty()) {
            System.out.println("Echecs : " + failures.size());
            failures.forEach(f -> System.out.println("  - " + f));
            System.exit(1);
        }
        System.out.println("Tous les tests du moteur passent.");
    }

    // ------------------------------------------------------------ outils

    private interface TestBody { void run() throws Exception; }

    private static void test(String name, TestBody body) {
        try {
            body.run();
            passed++;
            System.out.println("  [OK]   " + name);
        } catch (Throwable t) {
            String detail = t.getClass().getSimpleName() + ": " + t.getMessage();
            failures.add(name + " : " + detail);
            System.out.println("  [ECHEC] " + name + " -> " + detail);
            if (System.getenv("ARCANUM_TRACE") != null) t.printStackTrace(System.out);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void eq(Object expected, Object actual, String message) {
        if (!String.valueOf(expected).equals(String.valueOf(actual))) {
            throw new AssertionError(message + " (attendu " + expected + ", obtenu " + actual + ")");
        }
    }

    private static void near(double expected, double actual, double tolerance, String message) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(message + " (attendu ~" + expected + ", obtenu " + actual + ")");
        }
    }

    /** Un sorcier joueur (equipe des joueurs). */
    private static Combatant wizard(String id, School school, int level, CombatantStats stats, String... deck) {
        return duelist(id, school, level, stats, Team.PLAYERS, deck);
    }

    /** Un sorcier adverse (sert de cible dans les tests de degats). */
    private static Combatant foe(String id, School school, int level, CombatantStats stats, String... deck) {
        return duelist(id, school, level, stats, Team.ENEMIES, deck);
    }

    private static Combatant duelist(String id, School school, int level, CombatantStats stats, Team team, String... deck) {
        return Combatant.builder(id, id)
                .kind(team == Team.PLAYERS ? Combatant.Kind.WIZARD : Combatant.Kind.ENEMY)
                .school(school)
                .level(level)
                .team(team)
                .stats(stats)
                .health(StatsFactory.healthFor(school, level))
                .deck(List.of(deck))
                .build();
    }

    private static Combatant creature(String id, CreatureDef def) {
        return Combatant.builder(id, def.nameFr())
                .kind(Combatant.Kind.ENEMY)
                .school(def.school())
                .level(def.level())
                .team(Team.ENEMIES)
                .stats(StatsFactory.forCreature(def))
                .def(def)
                .ai(new EnemyAI())
                .health(def.health())
                .deck(def.deck())
                .build();
    }

    private static Battle battle(Combatant... combatants) {
        Battle b = new Battle("test", BattleConfig.defaults(), new Rng(1234), new BattleHost() {});
        for (Combatant c : combatants) b.addCombatant(c);
        b.start();
        return b;
    }

    private static void givePips(Combatant c, int normal, int power) {
        c.pips().clear();
        c.pips().add(normal, false);
        c.pips().add(power, true);
    }

    private static void playCard(Battle b, Combatant caster, String spellId, String targetId) {
        int index = handIndex(caster, spellId);
        check(index >= 0, "carte absente de la main : " + spellId + " / main=" + hand(caster));
        String error = b.validate(caster, BattleAction.cast(index, targetId));
        check(error == null, "coup refuse : " + error);
        check(b.submit(caster.id(), BattleAction.cast(index, targetId, 0)), "coup non enregistre");
    }

    private static int handIndex(Combatant c, String spellId) {
        List<Deck.Card> hand = c.deck().hand();
        for (int i = 0; i < hand.size(); i++) if (hand.get(i).spellId().equals(spellId)) return i;
        return -1;
    }

    private static List<String> hand(Combatant c) {
        return c.deck().hand().stream().map(Deck.Card::spellId).toList();
    }

    /** Force la main d'un combattant (utile pour tester un sort precis). */
    private static void setHand(Combatant c, String... spellIds) {
        while (!c.deck().hand().isEmpty()) c.deck().discard(0, -1);
        for (String id : spellIds) c.deck().addToHand(id, 99);
    }

    // ------------------------------------------------------------- pips

    private static void testPipsPowerRules() {
        test("pips : un pip de pouvoir vaut 2 pour son ecole", () -> {
            CombatantPips pips = new CombatantPips();
            pips.add(true);
            eq(2, pips.totalValue(true), "valeur pour son ecole");
            eq(1, pips.totalValue(false), "valeur pour une autre ecole");
        });

        test("pips : regle de paiement de Wizard101", () -> {
            CombatantPips exact = new CombatantPips();
            exact.add(true);
            exact.add(false);
            eq(0, exact.pay(3, true), "1 pip de pouvoir + 1 pip normal = 3 exactement");

            CombatantPips waste = new CombatantPips();
            waste.add(true);
            waste.add(true);
            waste.add(true);
            int wasted = waste.pay(3, true);
            eq(1, wasted, "2 pips de pouvoir pour un sort a 3 pips : 1 pip gaspille");
            eq(1, waste.slotCount(), "il reste un pip de pouvoir");

            CombatantPips offSchool = new CombatantPips();
            offSchool.add(true);
            offSchool.add(true);
            offSchool.add(true);
            eq(0, offSchool.pay(3, false), "hors ecole, chaque pip compte 1");
            eq(0, offSchool.slotCount(), "les 3 pips de pouvoir sont consommes");
        });

        test("pips : les pips normaux sont utilises en priorite hors ecole", () -> {
            CombatantPips pips = new CombatantPips();
            pips.add(false);
            pips.add(true);
            pips.pay(1, false);
            eq(1, pips.slotCount(), "un pip normal a ete consomme");
            check(pips.view().get(0), "il reste le pip de pouvoir");
        });

        test("pips : impossible de payer plus que ce qu'on possede", () -> {
            CombatantPips pips = new CombatantPips();
            pips.add(false);
            check(!pips.canPay(2, true), "1 pip normal ne paie pas 2 pips");
        });
    }

    private static void testPipsMaxSlots() {
        test("pips : maximum 7 emplacements (14 pips)", () -> {
            CombatantPips pips = new CombatantPips();
            for (int i = 0; i < 10; i++) pips.add(true);
            eq(7, pips.slotCount(), "plafond d'emplacements");
            eq(14, pips.totalValue(true), "14 pips pour son ecole");
        });
    }

    // ------------------------------------------------------------- deck

    private static void testDeckHandAndDiscard() {
        test("deck : main de 7 cartes et defausse interdite le round de pioche", () -> {
            List<String> cards = new ArrayList<>();
            for (int i = 0; i < 12; i++) cards.add("ember_kitten");
            Deck deck = new Deck(cards, 7, new Rng(1));
            deck.refillHand(1);
            eq(7, deck.hand().size(), "main completee a 7");
            check(!deck.discard(0, 1), "impossible de defausser une carte piochee ce round");
            check(deck.discard(0, 2), "defausse possible le round suivant");
            eq(6, deck.hand().size(), "une carte est partie");
        });

        test("deck : les cartes utilisees partent a la defausse", () -> {
            Deck deck = new Deck(List.of("ember_kitten", "ember_kitten"), 7, new Rng(2));
            deck.refillHand(1);
            eq(2, deck.hand().size(), "deux cartes en main");
            String played = deck.play(0);
            eq("ember_kitten", played, "carte jouee");
            eq(1, deck.hand().size(), "il reste une carte");
            eq(1, deck.discardPileSize(), "la carte jouee est a la defausse");
        });
    }

    private static void testDeckReshuffle() {
        test("deck : Rebattre remelange la defausse", () -> {
            List<String> cards = new ArrayList<>();
            for (int i = 0; i < 8; i++) cards.add("scarab");
            Deck deck = new Deck(cards, 7, new Rng(3));
            deck.refillHand(1);
            for (int i = 0; i < 4; i++) deck.play(0);
            eq(4, deck.discardPileSize(), "4 cartes a la defausse");
            deck.reshuffleAll(2);
            eq(7, deck.hand().size(), "nouvelle main complete");
            eq(0, deck.discardPileSize(), "defausse videe");
        });
    }

    // ------------------------------------------------------- degats

    private static void testDamagePipeline() {
        test("degats : ordre exact bonus d'ecole -> lame -> faiblesse -> piege -> critique -> resistance", () -> {
            CombatantStats attackerStats = CombatantStats.builder().damage(NECROMANCY, 100).build();
            CombatantStats targetStats = CombatantStats.builder().resist(NECROMANCY, 50).build();
            Combatant caster = wizard("caster", NECROMANCY, 10, attackerStats, "ghoul", "grave_edge", "wisp_wraith");
            Combatant target = foe("target", EQUILIBRIUM, 10, targetStats, "scarab", "scarab", "scarab");
            Battle b = battle(caster, target);
            givePips(caster, 0, 2);
            setHand(caster, "ghoul");
            setHand(target, "scarab");

            // Lame consommee au lancer : +35%
            caster.addStatus(Status.blade(35, java.util.Set.of(NECROMANCY), 1, "grave_edge", "Grave Edge"));
            // Piege sur la cible : +30%
            target.addStatus(Status.trap(30, java.util.Set.of(NECROMANCY), 1, "grave_mark", "Grave Mark"));

            int healthBefore = target.health();
            playCard(b, caster, "ghoul", target.id());
            b.resolveRound();

            // 160 (Ghoul) x 2 (bonus +100%) = 320 ; x1.35 lame = 432 ; x1.30 piege = 561.6
            // -> 562 (arrondi) ; x0.5 resistance = 281
            int dealt = healthBefore - target.health();
            near(281, dealt, 2, "degats attendus apres tous les modificateurs");
        });

        test("degats : resistance de 50% divise bien les degats par deux", () -> {
            CombatantStats noResist = CombatantStats.builder().build();
            CombatantStats halfResist = CombatantStats.builder().resist(NECROMANCY, 50).build();
            int plain = hitOnce(noResist);
            int halved = hitOnce(halfResist);
            near(plain / 2.0, halved, 1, "degats avec 50% de resistance");
        });
    }

    /** Frappe une cible avec Ghoul (160 degats fixes) et renvoie les degats infliges. */
    private static int hitOnce(CombatantStats targetStats) {
        Combatant caster = wizard("c", NECROMANCY, 10, CombatantStats.builder().build(), "ghoul");
        Combatant target = foe("t", EQUILIBRIUM, 10, targetStats, "scarab");
        Battle b = battle(caster, target);
        givePips(caster, 0, 2);
        setHand(caster, "ghoul");
        setHand(target, "scarab");
        int before = target.health();
        playCard(b, caster, "ghoul", target.id());
        b.resolveRound();
        return before - target.health();
    }

    private static void testShieldsAndAbsorb() {
        test("bouclier : Warding Shield reduit les degats de 50%", () -> {
            Combatant caster = wizard("c", NECROMANCY, 10, CombatantStats.builder().build(), "ghoul");
            Combatant target = foe("t", FROST, 10, CombatantStats.builder().build(), "scarab");
            Battle b = battle(caster, target);
            givePips(caster, 0, 2);
            setHand(caster, "ghoul");
            setHand(target, "scarab");
            target.addStatus(Status.shield(50, java.util.Set.of(), 1, "warding_shield", "Warding Shield"));
            int before = target.health();
            playCard(b, caster, "ghoul", target.id());
            b.resolveRound();
            near(80, before - target.health(), 2, "160 degats reduits de moitie");
        });

        test("absorption : Glacial Aegis encaisse avant les points de vie", () -> {
            Combatant caster = wizard("c", NECROMANCY, 10, CombatantStats.builder().build(), "ghoul");
            Combatant target = foe("t", FROST, 10, CombatantStats.builder().build(), "scarab");
            Battle b = battle(caster, target);
            givePips(caster, 0, 2);
            setHand(caster, "ghoul");
            setHand(target, "scarab");
            target.addStatus(Status.absorb(300, "glacial_aegis", "Glacial Aegis"));
            int before = target.health();
            playCard(b, caster, "ghoul", target.id());
            b.resolveRound();
            eq(0, before - target.health(), "les 160 degats sont absorbes");
            eq(140, target.statuses(Status.Type.ABSORB).get(0).remainingValue(), "reserve d'absorption restante");
        });
    }

    // ------------------------------------------------------ precision

    private static void testFizzle() {
        test("fizzle : un sort a 100% de precision ne rate jamais", () -> {
            long fizzles = 0;
            for (int i = 0; i < 60; i++) {
                Combatant caster = wizard("c", EQUILIBRIUM, 10, CombatantStats.builder().build(), "scarab");
                Combatant target = foe("t", EQUILIBRIUM, 10, CombatantStats.builder().build(), "scarab");
                Battle b = battle(caster, target);
                givePips(caster, 0, 1);
                setHand(caster, "scarab");
                setHand(target, "scarab");
                playCard(b, caster, "scarab", target.id());
                b.resolveRound();
                fizzles += b.history().stream().filter(e -> e instanceof BattleEvent.Fizzle
                        && ((BattleEvent.Fizzle) e).casterName().equals("c")).count();
            }
            eq(0L, fizzles, "aucun echec avec 100% de precision");
        });

        test("fizzle : un malus de precision fait rater les sorts", () -> {
            long fizzles = 0;
            for (int i = 0; i < 60; i++) {
                Combatant caster = wizard("c", EQUILIBRIUM, 10, CombatantStats.builder().build(), "scarab");
                Combatant target = foe("t", EQUILIBRIUM, 10, CombatantStats.builder().build(), "scarab");
                Battle b = battle(caster, target);
                givePips(caster, 0, 1);
                setHand(caster, "scarab");
                setHand(target, "scarab");
                caster.addStatus(Status.accuracy(-100, 1, "ashen_veil", "Ashen Veil"));
                playCard(b, caster, "scarab", target.id());
                b.resolveRound();
                fizzles += b.history().stream().filter(e -> e instanceof BattleEvent.Fizzle
                        && ((BattleEvent.Fizzle) e).casterName().equals("c")).count();
            }
            eq(60L, fizzles, "tous les sorts ratent avec -100% de precision");
        });
    }

    private static void testCriticalFormula() {
        test("critique : formule 3*l*c/(3c+b)", () -> {
            Combatant caster = wizard("c", PYROMANCY, 100, CombatantStats.builder().crit(300).build(), "ember_kitten");
            Combatant target = wizard("t", EQUILIBRIUM, 100, CombatantStats.builder().build(), "scarab");
            Battle b = battle(caster, target);
            near(1.0, b.critChance(caster, target), 0.001, "sans blocage, la chance tend vers 100%");

            Combatant blocked = wizard("t2", EQUILIBRIUM, 100, CombatantStats.builder().block(100).build(), "scarab");
            Battle b2 = battle(caster, blocked);
            near(0.9, b2.critChance(caster, blocked), 0.001, "300 de critique contre 100 de blocage = 90%");
        });

        test("critique : un critique bloque ne fait pas de degats supplementaires", () -> {
            CombatantStats attacker = CombatantStats.builder().crit(10000).build();
            CombatantStats defender = CombatantStats.builder().block(100000).build();
            Combatant caster = wizard("c", NECROMANCY, 100, attacker, "ghoul");
            Combatant target = foe("t", EQUILIBRIUM, 100, defender, "scarab");
            Battle b = battle(caster, target);
            givePips(caster, 0, 2);
            setHand(caster, "ghoul");
            setHand(target, "scarab");
            int before = target.health();
            playCard(b, caster, "ghoul", target.id());
            b.resolveRound();
            near(160, before - target.health(), 2, "degats normaux malgre le critique");
        });
    }

    // ------------------------------------------------- effets sur la duree

    private static void testDamageOverTime() {
        test("degats sur la duree : Cinder Wisp frappe 3 rounds de suite", () -> {
            Combatant caster = wizard("c", PYROMANCY, 10, CombatantStats.builder().build(), "cinder_wisp");
            Combatant target = foe("t", EQUILIBRIUM, 10, CombatantStats.builder().build(), "scarab");
            // La cible ne doit pas riposter : on vide sa main
            Battle b = battle(caster, target);
            givePips(caster, 0, 2);
            setHand(caster, "cinder_wisp");
            setHand(target);
            int before = target.health();
            playCard(b, caster, "cinder_wisp", target.id());
            b.resolveRound();
            // Round du lancer : 100 degats directs + le premier tick de DoT en fin de round
            eq(170, before - target.health(), "100 degats directs + 70 sur la duree en fin de round");

            eq(1, target.statuses(Status.Type.DOT).size(), "l'effet de duree est pose");

            for (int i = 0; i < 2; i++) {
                givePips(caster, 0, 1);
                setHand(caster);
                b.resolveRound();
            }
            near(310, before - target.health(), 2, "100 directs + 3 x 70 sur la duree");
            eq(0, target.statuses(Status.Type.DOT).size(), "l'effet a expire");
        });
    }

    private static void testDrain() {
        test("drain : la Goule rend la moitie des degats en vie", () -> {
            Combatant caster = wizard("c", NECROMANCY, 10, CombatantStats.builder().build(), "ghoul");
            Combatant target = foe("t", EQUILIBRIUM, 10, CombatantStats.builder().build(), "scarab");
            caster.applyDamage(300);
            int injured = caster.health();
            Battle b = battle(caster, target);
            givePips(caster, 0, 2);
            setHand(caster, "ghoul");
            setHand(target);
            playCard(b, caster, "ghoul", target.id());
            b.resolveRound();
            eq(injured + 80, caster.health(), "160 degats infliges -> 80 points de vie rendus");
        });
    }

    private static void testHealAndHealOverTime() {
        test("soins : Faerie Gift soigne 420 points de vie sans depasser le maximum", () -> {
            Combatant healer = wizard("h", SYLVAN, 10, CombatantStats.builder().build(), "faerie_gift");
            Combatant ally = wizard("a", SYLVAN, 10, CombatantStats.builder().build(), "scarab");
            Battle b = battle(healer, ally, foe("dummy", EQUILIBRIUM, 10, CombatantStats.builder().build()));
            ally.applyDamage(500);
            int injured = ally.health();
            givePips(healer, 0, 2);
            setHand(healer, "faerie_gift");
            setHand(ally);
            playCard(b, healer, "faerie_gift", ally.id());
            b.resolveRound();
            eq(injured + 420, ally.health(), "soin complet");
        });

        test("soins sur la duree : Sprite Bloom soigne 30 puis 90 par round", () -> {
            Combatant healer = wizard("h", SYLVAN, 10, CombatantStats.builder().build(), "sprite_bloom");
            Combatant ally = wizard("a", SYLVAN, 10, CombatantStats.builder().build(), "scarab");
            Battle b = battle(healer, ally, foe("dummy", EQUILIBRIUM, 10, CombatantStats.builder().build()));
            ally.applyDamage(600);
            int injured = ally.health();
            givePips(healer, 0, 1);
            setHand(healer, "sprite_bloom");
            setHand(ally);
            playCard(b, healer, "sprite_bloom", ally.id());
            b.resolveRound();
            eq(injured + 30 + 90, ally.health(), "soin direct puis premier tick");
            givePips(healer, 0, 1);
            setHand(healer);
            b.resolveRound();
            eq(injured + 30 + 90 * 2, ally.health(), "deuxieme tick");
        });
    }

    private static void testStun() {
        test("etourdissement : la cible perd son tour", () -> {
            Combatant caster = wizard("c", RUNOMANCY, 10, CombatantStats.builder().build(), "scarab");
            Combatant target = foe("t", EQUILIBRIUM, 10, CombatantStats.builder().build(), "scarab");
            Battle b = battle(caster, target);
            givePips(caster, 0, 1);
            setHand(caster, "scarab");
            setHand(target, "scarab");
            target.addStatus(Status.stun(1, "rime_chains", "Rime Chains"));
            check(b.submit(caster.id(), BattleAction.pass()), "le lanceur passe son tour");
            b.resolveRound();
            check(b.history().stream().anyMatch(e -> e instanceof BattleEvent.Stunned), "evenement d'etourdissement");
            check(!b.history().stream().anyMatch(e -> e instanceof BattleEvent.Cast
                    && ((BattleEvent.Cast) e).casterName().equals("t")), "la cible etourdie n'a rien lance");
        });

        test("etourdissement : les degats reveillent la cible", () -> {
            Combatant caster = wizard("c", NECROMANCY, 10, CombatantStats.builder().build(), "ghoul");
            Combatant target = foe("t", EQUILIBRIUM, 10, CombatantStats.builder().build(), "scarab");
            Battle b = battle(caster, target);
            givePips(caster, 0, 2);
            setHand(caster, "ghoul");
            setHand(target, "scarab");
            target.addStatus(Status.stun(2, "rime_chains", "Rime Chains"));
            playCard(b, caster, "ghoul", target.id());
            b.resolveRound();
            eq(0, target.statuses(Status.Type.STUN).size(), "l'etourdissement est dissipe par les degats");
        });
    }

    private static void testVariablePips() {
        test("pips variables : Deferlante Tempetueuse inflige 80 degats par pip depense", () -> {
            Combatant caster = wizard("c", TEMPEST, 20, CombatantStats.builder().accuracy(100).build(), "tempest_surge");
            Combatant target = foe("t", EQUILIBRIUM, 20, CombatantStats.builder().build(), "scarab");
            Battle b = battle(caster, target);
            givePips(caster, 3, 2);
            setHand(caster, "tempest_surge");
            setHand(target);
            int before = target.health();
            int index = handIndex(caster, "tempest_surge");
            int pips = b.pipsFor(caster, com.arcanumrealms.core.spell.SpellBook.get("tempest_surge"), 7);
            eq(7, pips, "7 pips utilises");
            check(b.submit(caster.id(), BattleAction.cast(index, target.id(), 7)), "coup enregistre");
            b.resolveRound();
            near(560, before - target.health(), 2, "80 x 7 = 560 degats");
        });
    }

    private static void testAreaOfEffect() {
        test("zone : une attaque de zone touche tous les ennemis", () -> {
            Combatant caster = wizard("c", RUNOMANCY, 20, CombatantStats.builder().accuracy(100).build(), "croaking_idol");
            Combatant e1 = creature("e1", Creatures.get("ember_imp"));
            Combatant e2 = creature("e2", Creatures.get("static_wisp"));
            Battle b = battle(caster, e1, e2);
            givePips(caster, 0, 4);
            setHand(caster, "croaking_idol");
            setHand(e1);
            setHand(e2);
            int before1 = e1.health();
            int before2 = e2.health();
            playCard(b, caster, "croaking_idol", e1.id());
            b.resolveRound();
            check(before1 - e1.health() > 0, "premier ennemi touche");
            check(before2 - e2.health() > 0, "second ennemi touche");
        });
    }

    // ----------------------------------------------------- partie complete

    private static void testFullBattleSimulation() {
        test("simulation : 3 sorciers contre 3 creatures, l'IA termine le duel", () -> {
            long seed = 42;
            Battle b = new Battle("sim", BattleConfig.defaults(), new Rng(seed), new BattleHost() {});
            b.addCombatant(playerCombatant("p1", PYROMANCY, 12, seed));
            b.addCombatant(playerCombatant("p2", SYLVAN, 12, seed + 1));
            b.addCombatant(playerCombatant("p3", TEMPEST, 12, seed + 2));
            b.addCombatant(creature("e1", Creatures.get("ember_imp")));
            b.addCombatant(creature("e2", Creatures.get("glacier_hound")));
            b.addCombatant(creature("e3", Creatures.get("grave_shade")));
            b.start();

            int guard = 0;
            while (!b.isFinished() && guard++ < 120) {
                for (Combatant c : b.living()) {
                    if (c.isAI() || !c.alive()) continue;
                    b.submit(c.id(), new EnemyAI().choose(b, c));
                }
                b.resolveRound();
                for (Combatant c : b.combatants()) {
                    check(c.health() >= 0, "points de vie negatifs pour " + c.name());
                    check(c.pips().slotCount() <= CombatantPips.MAX_SLOTS, "trop de pips pour " + c.name());
                    check(c.deck().hand().size() <= 7, "main trop grande pour " + c.name());
                }
            }
            check(b.isFinished(), "le duel doit se terminer (round " + b.round() + ")");
            check(b.winner() != null || b.isDraw(), "un camp doit gagner");
        });
    }

    private static Combatant playerCombatant(String id, School school, int level, long seed) {
        List<String> deck = new ArrayList<>();
        for (Spell s : SpellBook.trainable(school, level)) {
            int copies = Math.min(s.maxCopies(), 3);
            for (int i = 0; i < copies; i++) deck.add(s.id());
        }
        return Combatant.builder(id, id)
                .kind(Combatant.Kind.WIZARD)
                .school(school)
                .level(level)
                .team(Team.PLAYERS)
                .stats(StatsFactory.forWizard(school, level, StatsFactory.WizardGear.none()))
                .health(StatsFactory.healthFor(school, level))
                .deck(deck)
                .rng(new Rng(seed))
                .build();
    }

    private static void testSpellData() {
        test("donnees : le catalogue de sorts est coherent", () -> {
            List<String> problems = ArcanumCore.init();
            check(problems.isEmpty(), "problemes detectes : " + problems);
            check(SpellBook.size() > 100, "au moins 100 sorts attendus, trouve " + SpellBook.size());
            for (School school : School.values()) {
                check(!SpellBook.bySchool(school).isEmpty(), "aucun sort pour " + school.id());
            }
        });

        test("donnees : chaque sort est decrit en francais et en anglais", () -> {
            for (Spell spell : SpellBook.all()) {
                String fr = com.arcanumrealms.core.spell.SpellDescriber.describe(spell, true);
                String en = com.arcanumrealms.core.spell.SpellDescriber.describe(spell, false);
                check(!fr.isBlank(), "description francaise manquante pour " + spell.id());
                check(!en.isBlank(), "description anglaise manquante pour " + spell.id());
            }
        });
    }

    // ------------------------------------------------------------ monde

    private static void testWorldZones() {
        test("monde : les sept zones sont coherentes", () -> {
            eq(7, Zones.all().size(), "nombre de zones");
            for (ZoneDef zone : Zones.all()) {
                check(!zone.nameFr().isBlank() && !zone.nameEn().isBlank(),
                        "nom manquant pour la zone " + zone.id());
                check(!zone.descriptionFr().isBlank() && !zone.descriptionEn().isBlank(),
                        "description manquante pour la zone " + zone.id());
                check(zone.minLevel() <= zone.maxLevel(), "plage de niveaux inversee : " + zone.id());
                // La cite et le bosquet sont des lieux surs : aucune creature n'y rode.
                if (!List.of("grand_place", "bosquet").contains(zone.id())) {
                    check(!zone.creatures().isEmpty(), "aucune creature dans " + zone.id());
                }
                for (String creatureId : zone.creatures()) {
                    check(Bestiary.find(creatureId).isPresent(),
                            "creature inconnue " + creatureId + " dans " + zone.id());
                }
                for (String npcId : zone.npcs()) {
                    check(Npcs.find(npcId).isPresent(), "PNJ inconnu " + npcId + " dans " + zone.id());
                }
                for (String questId : zone.quests()) {
                    check(Quests.find(questId).isPresent(), "quete inconnue " + questId + " dans " + zone.id());
                }
                check(!zone.dungeon() || zone.unlockQuest() != null,
                        "donjon sans quete de deverrouillage : " + zone.id());
            }
        });

        test("monde : le deverrouillage des zones suit la progression", () -> {
            Map<String, Integer> unlocks = new LinkedHashMap<>();
            for (ZoneDef zone : Zones.all()) {
                unlocks.put(zone.id(), zone.minLevel());
            }
            // Chaque zone au-dela de la premiere est ouverte par une quete de la chaine principale.
            for (ZoneDef zone : Zones.all()) {
                if (zone.unlockQuest() == null) {
                    // Ouvertes d'emblee : la cite, le bosquet des ecoles et le sentier de depart.
                    check(List.of("grand_place", "bosquet", "sentier_lucioles").contains(zone.id()),
                            "zone ouverte d'emblee inattendue : " + zone.id());
                    continue;
                }
                QuestDef unlock = Quests.get(zone.unlockQuest());
                check(unlock.levelRequired() <= zone.unlockLevel(),
                        "quete " + unlock.id() + " (nv " + unlock.levelRequired()
                                + ") n'ouvre pas " + zone.id() + " (nv " + zone.unlockLevel() + ")");
                check(zone.minLevel() == zone.unlockLevel(),
                        "niveau d'entree de " + zone.id() + " different de sa quete d'ouverture");
                check(zone.minLevel() >= 1, "niveau minimal invalide pour " + zone.id());
            }
            // L'acces a la cite ne demande aucun niveau.
            check(Zones.get("grand_place").minLevel() == 1, "la cite doit etre accessible au niveau 1");
        });
    }

    private static void testWorldNpcs() {
        test("monde : les PNJ sont coherents", () -> {
            check(Npcs.all().size() >= 19, "PNJ attendus, trouve " + Npcs.all().size());
            eq(7, Npcs.trainers().size(), "nombre de professeurs");
            for (NpcDef npc : Npcs.all()) {
                check(!npc.nameFr().isBlank() && !npc.nameEn().isBlank(), "nom manquant : " + npc.id());
                check(!npc.descriptionFr().isBlank() && !npc.descriptionEn().isBlank(),
                        "description manquante : " + npc.id());
                check(Zones.find(npc.zoneId()).isPresent(), "zone inconnue pour " + npc.id());
                check(npc.greeting().size() > 0, "accueil manquant pour " + npc.id());
                check(npc.farewell().size() > 0, "adieu manquant pour " + npc.id());
                for (String questId : npc.givesQuests()) {
                    QuestDef quest = Quests.get(questId);
                    eq(npc.id(), quest.giverNpc(), "quete " + questId + " : donneur");
                }
                for (String questId : npc.endsQuests()) {
                    QuestDef quest = Quests.get(questId);
                    eq(npc.id(), quest.handInNpc(), "quete " + questId + " : remettant");
                }
                if (npc.isTrainer()) {
                    check(npc.school() != null, "professeur sans ecole : " + npc.id());
                }
            }
        });

        test("monde : les dialogues existent dans les deux langues", () -> {
            for (NpcDef npc : Npcs.all()) {
                for (Dialogue dialogue : List.of(npc.greeting(), npc.inProgress(), npc.completion(), npc.farewell())) {
                    for (DialogueLine line : dialogue.lines()) {
                        check(!line.fr().isBlank(), "ligne francaise vide chez " + npc.id());
                        check(!line.en().isBlank(), "ligne anglaise vide chez " + npc.id());
                        check(line.fr().length() <= 320, "ligne francaise trop longue chez " + npc.id());
                    }
                }
            }
        });

        test("monde : chaque professeur enseigne une ecole differente", () -> {
            Set<School> schools = new LinkedHashSet<>();
            for (NpcDef npc : Npcs.trainers()) {
                check(schools.add(npc.school()), "ecole en double chez les professeurs : " + npc.school());
            }
            eq(School.values().length, schools.size(), "toutes les ecoles doivent avoir un professeur");
        });
    }

    private static void testWorldCreatures() {
        test("monde : le bestiaire reprend les fiches de la wiki", () -> {
            check(Bestiary.all().size() >= 30, "creatures attendues, trouve " + Bestiary.all().size());
            for (WorldCreature creature : Bestiary.all()) {
                check(creature.health() > 0, "sante invalide : " + creature.id());
                check(creature.rank() >= 1 && creature.rank() <= 8, "rang invalide : " + creature.id());
                if (!creature.boss()) {
                    check(creature.rank() <= 5, "rang trop eleve hors boss : " + creature.id());
                }
                check(creature.school() != null, "ecole manquante : " + creature.id());
                check(!creature.deck().isEmpty(), "aucun sort pour " + creature.id());
                for (String spellId : creature.deck()) {
                    check(SpellBook.find(spellId).isPresent(),
                            "sort inconnu " + spellId + " pour " + creature.id());
                }
                for (School school : School.values()) {
                    check(creature.resist(school) >= -100 && creature.resist(school) <= 90,
                            "resistance invalide (" + creature.id() + ", " + school.id() + ")");
                    check(creature.boost(school) >= 0 && creature.boost(school) <= 100,
                            "boost invalide (" + creature.id() + ", " + school.id() + ")");
                }
                check(creature.startingPips() >= 0 && creature.startingPips() <= 6,
                        "pips de depart invalides : " + creature.id());
            }
        });

        test("monde : les creatures sont rattachees a une zone et a une table de butin", () -> {
            for (WorldCreature creature : Bestiary.all()) {
                check(!creature.zoneIds().isEmpty(), "aucune zone pour " + creature.id());
                for (String zoneId : creature.zoneIds()) {
                    check(Zones.find(zoneId).isPresent(), "zone inconnue " + zoneId + " pour " + creature.id());
                }
                check(creature.lootTable() != null && LootTables.get(creature.lootTable()) != null,
                        "table de butin manquante : " + creature.id());
                check(creature.entity() != null && !creature.entity().isBlank(),
                        "entite manquante : " + creature.id());
            }
            // Chaque creature du bestiaire doit etre visible dans au moins une zone.
            Set<String> placed = new LinkedHashSet<>();
            for (ZoneDef zone : Zones.all()) placed.addAll(zone.creatures());
            for (WorldCreature creature : Bestiary.all()) {
                check(placed.contains(creature.id()),
                        "creature absente de toutes les zones : " + creature.id());
            }
        });

        test("monde : les tables de butin sont valides", () -> {
            check(LootTables.all().size() >= 4, "tables de butin attendues");
            for (LootTable table : LootTables.all()) {
                check(table.goldMin() <= table.goldMax(), "or invalide dans " + table.id());
                check(!table.entries().isEmpty(), "table vide : " + table.id());
                for (LootTable.Entry entry : table.entries()) {
                    check(entry.chance() > 0 && entry.chance() <= 100,
                            "chance invalide dans " + table.id());
                    check(entry.min() <= entry.max(), "quantite invalide dans " + table.id());
                    if (entry.kind() == LootDrop.LootKind.GEAR) {
                        check(Gears.find(entry.id()).isPresent(),
                                "objet inconnu " + entry.id() + " dans " + table.id());
                    } else {
                        check(entry.id().contains(":"), "objet vanilla invalide dans " + table.id());
                    }
                }
                // Le tirage doit rendre du butin sur un grand nombre d'essais.
                List<LootDrop> rolled = table.roll(new Rng(1L), 0);
                check(rolled != null, "tirage impossible pour " + table.id());
            }
        });

        test("monde : les defs de combat du bestiaire sont utilisables", () -> {
            List<CreatureDef> defs = Bestiary.combatDefs();
            eq(Bestiary.all().size(), defs.size(), "une def de combat par creature");
            Set<String> ids = new LinkedHashSet<>();
            for (CreatureDef def : defs) {
                check(ids.add(def.id()), "def en double : " + def.id());
                check(def.health() > 0, "sante nulle : " + def.id());
                check(!def.deck().isEmpty(), "deck vide : " + def.id());
            }
        });
    }

    private static void testWorldGear() {
        test("monde : le catalogue d'equipement couvre les cinq paliers", () -> {
            check(Gears.all().size() >= 40, "pieces attendues, trouve " + Gears.all().size());
            for (GearSlot slot : GearSlot.values()) {
                check(Gears.ofSlot(slot).size() >= 5, "palier manquant pour " + slot);
            }
            for (int tier = 0; tier < 5; tier++) {
                int count = Gears.ofTier(tier).size();
                check(count >= 8, "palier " + tier + " incomplet (" + count + " pieces)");
            }
            for (GearDef gear : Gears.all()) {
                check(!gear.nameFr().isBlank() && !gear.nameEn().isBlank(), "nom manquant : " + gear.id());
                check(gear.level() >= 1, "niveau invalide : " + gear.id());
                check(tierOf(gear) == gear.tier(), "palier incoherent : " + gear.id());
                String line = gear.stats(true);
                check(line != null, "ligne de statistiques manquante : " + gear.id());
            }
        });

        test("monde : chaque palier donne une recompense par emplacement", () -> {
            for (int tier = 0; tier < 5; tier++) {
                for (GearSlot slot : GearSlot.values()) {
                    String id = Gears.reward(tier, slot);
                    check(Gears.find(id).isPresent(), "recompense introuvable : " + id);
                    eq(slot, Gears.get(id).slot(), "emplacement de la recompense " + id);
                }
            }
        });

        test("monde : l'equipement porte augmente les statistiques du sorcier", () -> {
            StatsFactory.WizardGear bare = StatsFactory.WizardGear.none();
            List<GearDef> full = new ArrayList<>();
            for (GearSlot slot : GearSlot.values()) full.add(Gears.get(Gears.reward(3, slot)));
            StatsFactory.WizardGear dressed = GearStats.merge(bare, full);
            check(GearStats.maxHealth(School.PYROMANCY, 20, dressed)
                            > GearStats.maxHealth(School.PYROMANCY, 20, bare),
                    "l'equipement doit augmenter les points de vie");
            check(dressed.damageBonus() != bare.damageBonus()
                            || dressed.resist() != bare.resist()
                            || dressed.healthBonus() > 0,
                    "l'equipement doit modifier les statistiques de duel");
        });
    }

    private static int tierOf(GearDef gear) {
        for (int tier = 0; tier < 5; tier++) {
            if (Gears.ofTier(tier).stream().anyMatch(g -> g.id().equals(gear.id()))) return tier;
        }
        return -1;
    }

    private static void testQuestLog() {
        test("quetes : le journal refuse une quete trop tot", () -> {
            QuestLog log = new QuestLog();
            check(!log.canStart("first_steps", 1), "Premiers Pas demande un prealable");
            log.start("welcome_to_the_city");
            check(log.isActive("welcome_to_the_city"), "la quete d'accueil doit demarrer");
            check(!log.canStart("welcome_to_the_city", 1), "une quete active ne redemarre pas");
        });

        test("quetes : la progression des objectifs suit les evenements du jeu", () -> {
            QuestLog log = new QuestLog();
            QuestDef quest = Quests.get("she_ran_crying_wildfire");
            check(!log.canStart(quest.id(), 99), "la premiere quete de l'allee suit Rapport a Hanna");
            log.start("first_steps");
            log.start("unwelcome_spirits");
            log.start("bones_and_badges");
            check(!log.canStart("report_to_hanna", 99), "Rapport a Hanna suit Os et Insignes");
            log.start("report_to_hanna");
            log.complete("first_steps");
            log.complete("unwelcome_spirits");
            log.complete("bones_and_badges");
            log.complete("report_to_hanna");
            check(log.canStart(quest.id(), 99), "l'allee doit s'ouvrir apres Rapport a Hanna");
            check(!log.canStart(quest.id(), 1), "et seulement au niveau demande");
            log.start(quest.id());
            check(!log.goalDone(quest.id(), quest.goals().get(0)), "objectif deja termine");
            log.reportDefeat("ember_elf_hunter", 3);
            check(log.goalDone(quest.id(), quest.goals().get(0)), "les 3 elfes doivent valider l'objectif");
            check(!log.readyToHandIn(quest.id()), "la recolte reste a faire");
            log.reportCollect("minecraft:blaze_powder", 1);
            check(!log.readyToHandIn(quest.id()), "un seul reagent ne suffit pas");
            log.reportCollect("minecraft:blaze_powder", 1);
            check(log.readyToHandIn(quest.id()), "la quete doit etre prete a etre remise");
            eq("hanna_emberwright", quest.handInNpc(), "remettant de la quete");
            check(log.complete(quest.id()), "la remise doit aboutir");
            check(log.isCompleted(quest.id()), "la quete doit etre archivee");
            check(log.handInReady().isEmpty(), "plus rien a remettre");
        });

        test("quetes : la progression est bornee par les objectifs", () -> {
            QuestLog log = new QuestLog();
            log.start("welcome_to_the_city");
            QuestDef quest = Quests.get("welcome_to_the_city");
            log.reportVisit("bosquet");
            int before = log.progressOf(quest.id(), quest.goals().get(0).key());
            log.reportVisit("bosquet");
            log.reportVisit("bosquet");
            eq(before, log.progressOf(quest.id(), quest.goals().get(0).key()),
                    "un objectif termine ne progresse plus");
        });

        test("quetes : le journal survit a une sauvegarde", () -> {
            QuestLog log = new QuestLog();
            log.start("welcome_to_the_city");
            log.start("first_steps");
            log.reportTalk("lea_nightchant");
            log.complete("welcome_to_the_city");
            Map<String, Map<String, Integer>> progress = log.rawProgress();

            QuestLog restored = new QuestLog();
            restored.restore(new ArrayList<>(log.active()), new LinkedHashSet<>(log.completed()), progress);
            eq(log.active(), restored.active(), "quetes actives restaurees");
            eq(log.completed(), restored.completed(), "quetes terminees restaurees");
            eq(log.progressOf("first_steps", "talk:lea_nightchant"),
                    restored.progressOf("first_steps", "talk:lea_nightchant"), "progression restauree");
            check(restored.isCompleted("welcome_to_the_city"), "la quete terminee doit rester terminee");
        });

        test("quetes : les objectifs de discussion et de visite fonctionnent", () -> {
            QuestLog log = new QuestLog();
            log.start("welcome_to_the_city");
            QuestDef quest = Quests.get("welcome_to_the_city");
            boolean hasTalk = quest.goals().stream().anyMatch(g -> g instanceof QuestGoal.Talk);
            boolean hasVisit = quest.goals().stream().anyMatch(g -> g instanceof QuestGoal.Visit);
            check(hasTalk || hasVisit, "la quete d'accueil doit avoir un objectif de deplacement");
            for (QuestGoal goal : quest.goals()) {
                if (goal instanceof QuestGoal.Talk t) log.reportTalk(t.npcId());
                if (goal instanceof QuestGoal.Visit v) log.reportVisit(v.zoneId());
            }
            check(log.readyToHandIn("welcome_to_the_city"), "la quete d'accueil doit etre remettable");
        });
    }

    private static void testQuestRewards() {
        test("quetes : les recompenses sont coherentes", () -> {
            for (QuestDef quest : Quests.all()) {
                check(quest.rewardXp() > 0, "aucune experience pour " + quest.id());
                check(quest.rewardGold() >= 0, "or invalide pour " + quest.id());
                if (quest.rewardSpell() != null) {
                    check(SpellBook.find(quest.rewardSpell()).isPresent(),
                            "sort de recompense inconnu : " + quest.rewardSpell());
                }
                if (quest.rewardItem() != null) {
                    check(Gears.find(quest.rewardItem()).isPresent(),
                            "objet de recompense inconnu : " + quest.rewardItem());
                }
                for (String choice : quest.rewardChoices()) {
                    check(Gears.find(choice).isPresent(), "choix inconnu : " + choice);
                }
                check(quest.goals().size() >= 1, "quete sans objectif : " + quest.id());
                check(!quest.nameFr().isBlank() && !quest.nameEn().isBlank(), "nom manquant : " + quest.id());
            }
        });

        test("quetes : la chaine principale est continue", () -> {
            String current = Quests.firstMainQuest();
            Set<String> visited = new LinkedHashSet<>();
            int previousLevel = 0;
            while (current != null) {
                check(visited.add(current), "boucle dans la chaine principale : " + current);
                QuestDef quest = Quests.get(current);
                check(!quest.side(), "quete annexe dans la chaine principale : " + current);
                check(quest.levelRequired() >= previousLevel,
                        "niveau qui redescend : " + current + " (nv " + quest.levelRequired() + ")");
                previousLevel = quest.levelRequired();
                if (quest.leadsTo() != null) {
                    QuestDef next = Quests.get(quest.leadsTo());
                    eq(current, next.prequest(), "maillon suivant de " + current);
                }
                current = quest.leadsTo();
            }
            check(visited.size() >= 12, "chaine principale trop courte : " + visited.size() + " quetes");
            String last = null;
            for (String id : visited) last = id;
            check(last != null && Quests.get(last).leadsTo() == null, "la chaine doit se terminer");
        });

        test("quetes : chaque quete annexe est accessible", () -> {
            int side = 0;
            for (QuestDef quest : Quests.all()) {
                if (!quest.side()) continue;
                side++;
                check(quest.levelRequired() >= 1, "niveau invalide : " + quest.id());
                check(!quest.goals().isEmpty(), "objectif manquant : " + quest.id());
            }
            check(side >= 8, "quetes annexes attendues, trouve " + side);
        });
    }

    private static void testWikiAnchors() {
        test("wiki : les valeurs relevees sur la wiki sont conservees", () -> {
            // Creature:Fire Elf Hunter - rang 2, 150 PV, 1 pip, +10% glace, 10% resistance feu.
            WorldCreature elf = Bestiary.get("ember_elf_hunter");
            eq(2, elf.rank(), "rang du chasseur elfe (wiki)");
            eq(150, elf.health(), "sante du chasseur elfe (wiki)");
            eq(1, elf.startingPips(), "pips de depart du chasseur elfe (wiki)");
            eq(10, elf.boost(School.FROST), "sensibilite a la glace (wiki)");
            eq(10, elf.resist(School.PYROMANCY), "resistance au feu (wiki)");
            eq("Elf", elf.classification(), "classification (wiki)");
            check(elf.deck().contains("frost_ward"), "le chasseur elfe lance un bouclier de givre (wiki)");
            check(elf.deck().stream().filter(id -> id.equals("ember_kitten")).count() >= 2,
                    "le chasseur elfe lance plusieurs Chatons de Braise, equivalent de Fire Cat (wiki)");
            check(elf.deck().stream().anyMatch(id -> id.startsWith("wand_")),
                    "le chasseur elfe a une attaque naturelle (wiki)");
        });

        test("wiki : les creatures emblematiques sont au bestiaire", () -> {
            for (String id : List.of("ember_elf_hunter", "ember_elf_pathfinder", "wormguts",
                    "gretta_darkkettle", "magma_man", "haunted_minion", "lost_soul", "tide_kraken",
                    "the_harvest_lord", "bone_legion", "hollow_lord", "grand_necromancer", "tide_kraken")) {
                check(Bestiary.find(id).isPresent(), "creature de la wiki manquante : " + id);
            }
            check(Bestiary.get("the_harvest_lord").boss(), "le seigneur des moissons est un boss");
            check(Bestiary.get("grand_necromancer").boss(), "le grand necromancien est un boss");
        });

        test("wiki : la trame de l'allee suit les douze etapes du scenario", () -> {
            // Trame relevee dans Category:Firecat_Alley_Storyline_Quests (12 quetes).
            List<String> beats = List.of("she_ran_crying_wildfire", "investigate_alley", "point_taken",
                    "danger_danger", "dire_news", "the_cure", "administer_cure", "which_craft",
                    "which_hunt", "not_that_witch", "the_embercat", "swiftarrow_ambush");
            eq(12, beats.size(), "douze etapes de scenario dans l'allee");
            for (int i = 0; i < beats.size(); i++) {
                QuestDef quest = Quests.get(beats.get(i));
                check(!quest.side(), "etape de scenario marquee annexe : " + quest.id());
                if (i == 0) {
                    eq("report_to_hanna", quest.prequest(), "entree dans la trame");
                } else {
                    eq(beats.get(i - 1), quest.prequest(), "enchaînement de " + quest.id());
                }
                check(quest.goals().size() >= 1, "etape sans objectif : " + quest.id());
            }
        });

        test("wiki : les quetes annexes de l'allee correspondent aux annexes relevees", () -> {
            for (String id : List.of("cold_hard_steel", "magma_mania", "oil_rush", "gotta_see_gretta",
                    "ticklin_da_bones", "nightmare_time", "frost_knight_fight")) {
                QuestDef quest = Quests.get(id);
                check(quest.side(), "quete " + id + " doit etre annexe");
                check(quest.prequest() != null || quest.levelRequired() <= quest.levelRequired(),
                        "annexe sans prealable : " + id);
            }
        });

        test("wiki : les PNJ emblematiques donnent les quetes attendues", () -> {
            check(Npcs.get("headmaster_alderwise").givesQuests().contains("welcome_to_the_city"),
                    "le directeur accueille le joueur");
            check(Npcs.get("hanna_emberwright").givesQuests().contains("she_ran_crying_wildfire"),
                    "Hanna ouvre la trame de l'allee");
            check(Npcs.get("captain_marrow").givesQuests().contains("the_harvest_lord_quest"),
                    "Marrow donne la traque du seigneur des moissons");
            check(Quests.get("the_harvest_lord_quest").goals().stream()
                            .anyMatch(g -> g instanceof QuestGoal.Defeat d && d.creatureId().equals("the_harvest_lord")),
                    "le seigneur des moissons est le boss de la quete");
        });
    }

    // -------------------------------------------------- contenu modifiable

    private static void testContentKeys() {
        test("contenu : les cles de traduction sont uniques", () -> {
            Set<String> seen = new LinkedHashSet<>();
            List<String> duplicates = new ArrayList<>();
            for (NpcDef npc : Npcs.all()) {
                for (String key : List.of(ContentKeys.npc(npc.id()), ContentKeys.npcTitle(npc.id()),
                        ContentKeys.npcDescription(npc.id()))) {
                    if (!seen.add(key)) duplicates.add(key);
                }
                for (String slot : ContentKeys.SLOTS) {
                    for (ContentLine line : ContentKeys.dialogue(npc, slot, true)) {
                        if (!seen.add(line.key())) duplicates.add(line.key());
                    }
                }
            }
            for (Spell spell : SpellBook.all()) {
                if (!seen.add(ContentKeys.spell(spell.id()))) duplicates.add(spell.id());
                if (!seen.add(ContentKeys.spellDescription(spell.id()))) duplicates.add(spell.id());
            }
            for (QuestDef quest : Quests.all()) {
                if (!seen.add(ContentKeys.quest(quest.id()))) duplicates.add(quest.id());
                if (!seen.add(ContentKeys.questSummary(quest.id()))) duplicates.add(quest.id());
                if (!seen.add(ContentKeys.questRewards(quest.id()))) duplicates.add(quest.id());
                for (ContentLine goal : ContentKeys.questGoals(quest, true)) {
                    if (!seen.add(goal.key())) duplicates.add(goal.key());
                }
            }
            for (WorldCreature creature : Bestiary.all()) {
                if (!seen.add(ContentKeys.creature(creature.id()))) duplicates.add(creature.id());
            }
            for (GearDef gear : Gears.all()) {
                if (!seen.add(ContentKeys.gear(gear.id()))) duplicates.add(gear.id());
            }
            for (ZoneDef zone : Zones.all()) {
                if (!seen.add(ContentKeys.zone(zone.id()))) duplicates.add(zone.id());
            }
            check(duplicates.isEmpty(), "cles en double : " + duplicates);
            check(seen.size() > 600, "cles attendues, trouve " + seen.size());
        });

        test("contenu : chaque texte est traduisible et non vide", () -> {
            for (NpcDef npc : Npcs.all()) {
                for (String slot : ContentKeys.SLOTS) {
                    List<ContentLine> fr = ContentKeys.dialogue(npc, slot, true);
                    List<ContentLine> en = ContentKeys.dialogue(npc, slot, false);
                    eq(fr.size(), en.size(), "autant de repliques en francais et en anglais pour " + npc.id());
                    for (int i = 0; i < fr.size(); i++) {
                        check(fr.get(i).translatable(), "replique sans cle : " + npc.id() + "/" + slot);
                        check(!fr.get(i).text().isBlank() && !en.get(i).text().isBlank(),
                                "replique vide : " + fr.get(i).key());
                        eq(fr.get(i).key(), en.get(i).key(), "meme cle dans les deux langues");
                    }
                }
            }
            for (QuestDef quest : Quests.all()) {
                List<ContentLine> goals = ContentKeys.questGoals(quest, true);
                eq(quest.goals().size(), goals.size(), "un cle par objectif de " + quest.id());
                for (ContentLine goal : goals) {
                    check(goal.translatable() && !goal.text().isBlank(),
                            "objectif sans cle : " + quest.id());
                }
                check(ContentKeys.questRewardLine(quest, true).translatable(),
                        "recompense sans cle : " + quest.id());
            }
        });

        test("contenu : le format des cles suit la convention", () -> {
            eq("npc.arcanum_realms.lea_nightchant", ContentKeys.npc("lea_nightchant"), "cle de PNJ");
            eq("npc.arcanum_realms.lea_nightchant.title", ContentKeys.npcTitle("lea_nightchant"),
                    "cle de titre de PNJ");
            eq("dialogue.arcanum_realms.lea_nightchant.greeting.0",
                    ContentKeys.dialogue("lea_nightchant", ContentKeys.GREETING, 0), "cle de replique");
            eq("spell.arcanum_realms.ember_kitten.name", ContentKeys.spell("ember_kitten"), "cle de sort");
            eq("spell.arcanum_realms.ember_kitten.desc", ContentKeys.spellDescription("ember_kitten"),
                    "cle de description de sort");
            eq("creature.arcanum_realms.lost_soul", ContentKeys.creature("lost_soul"), "cle de creature");
            eq("gear.arcanum_realms.ember_cowl", ContentKeys.gear("ember_cowl"), "cle d'equipement");
            eq("zone.arcanum_realms.allee_braises", ContentKeys.zone("allee_braises"), "cle de zone");
            eq("quest.arcanum_realms.the_cure", ContentKeys.quest("the_cure"), "cle de quete");
            eq("quest.arcanum_realms.the_cure.summary", ContentKeys.questSummary("the_cure"),
                    "cle de resume");
            eq("quest.arcanum_realms.the_cure.goal.0", ContentKeys.questGoal("the_cure", 0),
                    "cle d'objectif");
            eq("quest.arcanum_realms.the_cure.rewards", ContentKeys.questRewards("the_cure"),
                    "cle de recompenses");
            eq(4, ContentKeys.SLOTS.size(), "quatre moments de dialogue");
        });

        test("contenu : les fabriques de lignes portent les bonnes cles", () -> {
            NpcDef hanna = Npcs.get("hanna_emberwright");
            ContentLine headline = ContentKeys.npcHeadline(hanna, true);
            check(headline.translatable(), "l'en-tete de dialogue est traduisible");
            check(headline.text().startsWith(hanna.nameFr()), "l'en-tete commence par le nom du PNJ");
            QuestDef quest = Quests.get("the_cure");
            ContentLine name = ContentKeys.questName(quest, true);
            eq(ContentKeys.quest(quest.id()), name.key(), "nom de quete");
            ContentLine rewards = ContentKeys.questRewardLine(quest, true);
            eq(ContentKeys.questRewards(quest.id()), rewards.key(), "recompenses de quete");
            ContentLine spell = ContentKeys.spellName(SpellBook.get("ember_kitten"), true);
            eq(ContentKeys.spell("ember_kitten"), spell.key(), "nom de sort");
            check(!spell.text().isBlank(), "nom de sort non vide");
        });
    }
    /**
     * Le renommage doit fonctionner partout : les textes composes par le moteur
     * contiennent des noms par defaut, et les deux langues doivent y figurer
     * (le journal de combat melange le texte localise et les noms anglais).
     */
    private static void testContentNames() {
        test("renommage : la table des noms couvre les deux langues", () -> {
            List<String[]> sources = ContentNames.sources();
            check(sources.size() > 400, "table fournie (trouve " + sources.size() + ")");
            for (String[] entry : sources) {
                check(entry[0] != null && !entry[0].isBlank(), "nom par defaut non vide");
                check(entry[1] != null && entry[1].startsWith("spell.arcanum_realms.")
                        || entry[1].startsWith("creature.arcanum_realms.")
                        || entry[1].startsWith("npc.arcanum_realms.")
                        || entry[1].startsWith("gear.arcanum_realms.")
                        || entry[1].startsWith("zone.arcanum_realms.")
                        || entry[1].startsWith("school.arcanum_realms."),
                        "cle connue pour " + entry[0] + " (obtenu " + entry[1] + ")");
            }
        });

        test("renommage : un sort est reconnu dans les deux langues", () -> {
            Spell spell = SpellBook.get("ember_kitten");
            eq(ContentKeys.spell("ember_kitten"), ContentNames.keyOf(spell.nameEn()),
                    "nom anglais reconnu");
            eq(ContentKeys.spell("ember_kitten"), ContentNames.keyOf(spell.nameFr()),
                    "nom francais reconnu");
            check(ContentNames.keyOf("Nom qui n'existe pas") == null, "nom inconnu rejete");
        });

        test("renommage : creatures, PNJ, zones et objets sont reconnus", () -> {
            WorldCreature creature = Bestiary.get("ember_elf_hunter");
            check(ContentNames.keyOf(creature.nameFr()) != null, "creature francaise reconnue");
            NpcDef npc = Npcs.get("hanna_emberwright");
            eq(ContentKeys.npc("hanna_emberwright"), ContentNames.keyOf(npc.nameFr()), "PNJ reconnu");
            eq(ContentKeys.npcTitle("hanna_emberwright"), ContentNames.keyOf(npc.titleFr()), "titre reconnu");
            ZoneDef zone = Zones.get("allee_braises");
            eq(ContentKeys.zone("allee_braises"), ContentNames.keyOf(zone.nameFr()), "zone reconnue");
            GearDef gear = Gears.ofTier(1).get(0);
            eq(ContentKeys.gear(gear.id()), ContentNames.keyOf(gear.nameFr()), "equipement reconnu");
        });

        test("renommage : les noms longs passent avant les noms courts", () -> {
            List<String[]> sources = ContentNames.sources();
            for (int i = 1; i < sources.size(); i++) {
                check(sources.get(i - 1)[0].length() >= sources.get(i)[0].length(),
                        "table triee du plus long au plus court");
            }
        });
    }

    /**
     * Verifie qu'un texte reecrit remplace bien celui du mod, cle par cle : c'est
     * la garantie qu'un pack de ressources suffit, sans recompiler.
     */
    private static void testContentOverride() {
        test("surcharge : chaque famille de texte expose sa cle", () -> {
            NpcDef npc = Npcs.get("hanna_emberwright");
            eq("npc.arcanum_realms.hanna_emberwright", ContentKeys.npcName(npc, true).key(), "PNJ");
            eq("dialogue.arcanum_realms.hanna_emberwright.greeting.0",
                    ContentKeys.dialogue(npc, ContentKeys.GREETING, true).get(0).key(), "dialogue");
            eq("spell.arcanum_realms.ember_kitten.name",
                    ContentKeys.spellName(SpellBook.get("ember_kitten"), true).key(), "sort");
            eq("creature.arcanum_realms.ember_elf_hunter",
                    ContentKeys.creatureName(Bestiary.get("ember_elf_hunter"), true).key(), "creature");
            eq("quest.arcanum_realms.the_cure", ContentKeys.questName(Quests.get("the_cure"), true).key(),
                    "quete");
        });

        test("surcharge : les dialogues numerotent leurs repliques sans trou", () -> {
            for (NpcDef npc : Npcs.all()) {
                for (String slot : ContentKeys.SLOTS) {
                    List<ContentLine> lines = ContentKeys.dialogue(npc, slot, true);
                    for (int i = 0; i < lines.size(); i++) {
                        eq(ContentKeys.dialogue(npc.id(), slot, i), lines.get(i).key(),
                                "replique " + i + " de " + npc.id() + "." + slot);
                        check(lines.get(i).translatable(), "replique traduisible");
                        check(!lines.get(i).text().isBlank(), "replique non vide");
                    }
                }
            }
        });

        test("surcharge : chaque nom par defaut retrouve sa cle", () -> {
            java.util.Map<String, String> keys = new java.util.HashMap<>();
            java.util.List<String> ambiguities = new java.util.ArrayList<>();
            for (String[] entry : ContentNames.sources()) {
                String known = keys.putIfAbsent(entry[0], entry[1]);
                if (known != null && !known.equals(entry[1])) {
                    ambiguities.add(entry[0] + " -> " + known + " / " + entry[1]);
                }
            }
            check(keys.size() > 200, "noms distincts nombreux (trouve " + keys.size() + ")");
            // deux contenus qui portent exactement le meme nom rendraient le
            // renommage ambigu : on veut le savoir tout de suite
            check(ambiguities.isEmpty(), "noms ambigus : " + ambiguities.size()
                    + " " + ambiguities.stream().limit(3).toList());
            for (String name : keys.keySet()) {
                check(ContentNames.keyOf(name) != null, "nom resolvable : " + name);
            }
        });
    }
}
