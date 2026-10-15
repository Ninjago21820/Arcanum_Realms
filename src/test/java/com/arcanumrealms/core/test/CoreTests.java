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

import java.util.ArrayList;
import java.util.List;

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
}
