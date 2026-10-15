package com.arcanumrealms.core.battle;

import com.arcanumrealms.core.card.Deck;
import com.arcanumrealms.core.minion.CreatureDef;
import com.arcanumrealms.core.minion.Creatures;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.core.spell.SpellEffect;
import com.arcanumrealms.core.status.Status;
import com.arcanumrealms.core.util.Rng;

import java.util.*;

/**
 * Le moteur de duel : un round se deroule exactement comme dans Wizard101.
 *
 * <pre>
 * 1. Debut de round   : chaque combattant gagne 1 pip (pip de pouvoir selon son equipement)
 *                       et complete sa main a 7 cartes.
 * 2. Planification    : chaque combattant choisit une carte (ou passe / abandonne).
 *                       Un joueur peut aussi defausser des cartes.
 * 3. Resolution       : les sorts partent dans l'ordre du cercle de duel
 *                       (un joueur, une creature, un joueur...).
 * 4. Fin de round     : les degats et soins sur la duree s'appliquent,
 *                       puis les durees diminuent d'un round.
 * </pre>
 *
 * <p>Formules reprises de Wizard101 :
 * <ul>
 *   <li><b>Fizzle</b> : on rate si le jet depasse la precision du sort (+ bonus d'equipement,
 *       +/- charmes de precision).</li>
 *   <li><b>Degats</b> : base x (1 + bonus d'ecole) x (1 + lames) x (1 + pieges)
 *       x (1 - faiblesse) x critique x (1 - resistance + percee) puis boucliers, absorption
 *       et resistance fixe.</li>
 *   <li><b>Critique</b> : chance = 3*l*c/(3c+b) avec l = niveau/100 (plafonne a 1),
 *       c = score de critique, b = score de blocage ; multiplicateur = 2 - 3b/(c+3b).
 *       Un critique bloque ne fait aucun degat supplementaire.</li>
 *   <li><b>Pips de pouvoir</b> : ne comptent double que pour les sorts de son ecole.</li>
 * </ul>
 */
public final class Battle {

    public enum Phase { NOT_STARTED, PLANNING, RESOLUTION, FINISHED }

    /** Aura globale : une seule active a la fois, la derniere lancee remplace la precedente. */
    public record GlobalAura(SpellEffect.AuraType type, int percent, Set<School> schools, int rounds, String sourceName) {}

    /** Resultat d'une touche, pour les evenements et l'interface. */
    public record Hit(int applied, int resisted, int absorbed, boolean critical) {}

    private final String id;
    private final BattleConfig config;
    private final Rng rng;
    private final BattleHost host;

    private final List<Combatant> combatants = new ArrayList<>();
    private final Map<String, Combatant> byId = new LinkedHashMap<>();
    private final List<Combatant> turnOrder = new ArrayList<>();
    private final Map<String, BattleAction> actions = new LinkedHashMap<>();
    private final List<BattleEvent> pending = new ArrayList<>();
    private final List<BattleEvent> log = new ArrayList<>();

    private Phase phase = Phase.NOT_STARTED;
    private int round;
    private Team winner;
    private boolean draw;
    private GlobalAura aura;
    private int minionCounter;

    public Battle(String id, BattleConfig config, Rng rng, BattleHost host) {
        this.id = Objects.requireNonNull(id);
        this.config = Objects.requireNonNull(config);
        this.rng = Objects.requireNonNull(rng);
        this.host = host == null ? new BattleHost() {} : host;
    }

    // ------------------------------------------------------------- setup

    public String id() { return id; }
    public BattleConfig config() { return config; }
    public Phase phase() { return phase; }
    public int round() { return round; }
    public Team winner() { return winner; }
    public boolean isDraw() { return draw; }
    public boolean isFinished() { return phase == Phase.FINISHED; }
    public GlobalAura aura() { return aura; }
    public Rng rng() { return rng; }

    public void addCombatant(Combatant combatant) {
        if (phase != Phase.NOT_STARTED) throw new IllegalStateException("Le duel a deja commence");
        if (byId.containsKey(combatant.id())) throw new IllegalArgumentException("Identifiant en double: " + combatant.id());
        combatants.add(combatant);
        byId.put(combatant.id(), combatant);
    }

    public List<Combatant> combatants() { return Collections.unmodifiableList(combatants); }
    public List<Combatant> living() { return combatants.stream().filter(Combatant::alive).toList(); }
    public Combatant get(String id) { return byId.get(id); }
    public List<Combatant> turnOrder() { return Collections.unmodifiableList(turnOrder); }
    public List<BattleEvent> history() { return Collections.unmodifiableList(log); }

    /** Evenements produits depuis le dernier appel (le serveur les diffuse aux clients). */
    public List<BattleEvent> drainEvents() {
        List<BattleEvent> out = new ArrayList<>(pending);
        pending.clear();
        return out;
    }

    /** Le duel demarre : ordre de passage, melange des decks, premier round. */
    public void start() {
        if (phase != Phase.NOT_STARTED) throw new IllegalStateException("Deja demarre");
        rebuildTurnOrder();
        for (Combatant c : combatants) c.deck().shuffle(rng);
        long wizards = combatants.stream().filter(c -> c.team() == Team.PLAYERS).count();
        long creatures = combatants.stream().filter(c -> c.team() == Team.ENEMIES).count();
        emit(new BattleEvent.Start((int) wizards, (int) creatures));
        host.onBattleStart(this);
        round = 1;
        beginRound();
    }

    /** Ordre du cercle de duel : un joueur, une creature, un joueur... */
    private void rebuildTurnOrder() {
        turnOrder.clear();
        List<Combatant> players = combatants.stream().filter(c -> c.team() == Team.PLAYERS).toList();
        List<Combatant> enemies = combatants.stream().filter(c -> c.team() == Team.ENEMIES).toList();
        switch (config.turnOrder()) {
            case PLAYERS_FIRST -> { turnOrder.addAll(players); turnOrder.addAll(enemies); }
            case ENEMIES_FIRST -> { turnOrder.addAll(enemies); turnOrder.addAll(players); }
            case ALTERNATING -> {
                int max = Math.max(players.size(), enemies.size());
                for (int i = 0; i < max; i++) {
                    if (i < players.size()) turnOrder.add(players.get(i));
                    if (i < enemies.size()) turnOrder.add(enemies.get(i));
                }
            }
        }
    }

    private void beginRound() {
        emit(new BattleEvent.RoundStart(round));
        for (Combatant c : living()) {
            boolean power = rng.chance(c.stats().powerPipChance());
            c.pips().add(power);
        }
        for (Combatant c : living()) c.deck().refillHand(round);
        actions.clear();
        phase = Phase.PLANNING;
        emit(new BattleEvent.Planning(round));
        for (Combatant c : living()) {
            if (c.isAI()) submit(c.id(), c.ai().choose(this, c));
        }
    }

    // -------------------------------------------------------- planification

    /**
     * Enregistre le choix d'un combattant.
     * @return {@code null} si tout va bien, sinon la cle de l'erreur a afficher
     */
    public String validate(Combatant c, BattleAction action) {
        if (action.type() == BattleAction.Type.PASS) return null;
        if (action.type() == BattleAction.Type.FLEE) return config.allowFlee() ? null : "battle.flee.disabled";
        if (action.type() != BattleAction.Type.CAST) return "battle.error.unknown";
        List<Deck.Card> hand = c.deck().hand();
        if (action.handIndex() < 0 || action.handIndex() >= hand.size()) return "battle.error.bad_card";
        Spell spell = SpellBook.get(hand.get(action.handIndex()).spellId());
        int cost = pipsFor(c, spell, action.pipsToSpend());
        if (cost < 0) return "battle.error.pips";
        boolean doublePower = c.stats().powerPipsCountDouble(spell.school(), c.school());
        if (!c.pips().canPay(cost, doublePower)) return "battle.error.pips";
        return null;
    }

    public boolean submit(String combatantId, BattleAction action) {
        if (phase != Phase.PLANNING || action == null) return false;
        Combatant c = byId.get(combatantId);
        if (c == null || !c.alive()) return false;
        if (validate(c, action) != null) return false;
        actions.put(combatantId, action);
        return true;
    }

    /** Defausse une carte : immediat, et ca ne consomme pas l'action du round. */
    public boolean discard(String combatantId, int handIndex) {
        if (phase != Phase.PLANNING) return false;
        Combatant c = byId.get(combatantId);
        if (c == null || !c.alive()) return false;
        List<Deck.Card> hand = c.deck().hand();
        if (handIndex < 0 || handIndex >= hand.size()) return false;
        String spellId = hand.get(handIndex).spellId();
        if (!c.deck().discard(handIndex, round)) return false;
        emit(new BattleEvent.Discarded(c.name(), spellId, SpellBook.get(spellId).name(false)));
        return true;
    }

    /** Tous les joueurs (non IA) ont-ils choisi ? */
    public boolean allChosen() {
        return living().stream().filter(c -> !c.isAI()).allMatch(c -> actions.containsKey(c.id()));
    }

    public BattleAction actionOf(String combatantId) { return actions.get(combatantId); }

    /** Pips necessaires pour lancer ce sort ; -1 si impossible. */
    public int pipsFor(Combatant c, Spell spell, int requested) {
        boolean doublePower = c.stats().powerPipsCountDouble(spell.school(), c.school());
        if (!spell.variablePips()) return spell.pipCost();
        int available = c.pips().totalValue(doublePower);
        int max = Math.min(spell.maxPips(), available);
        if (max < spell.minPips()) return -1;
        return Math.max(spell.minPips(), Math.min(requested <= 0 ? spell.minPips() : requested, max));
    }

    /** Valeur de pips detenue pour le lancement d'un sort de cette ecole. */
    public int availablePips(Combatant c, Spell spell) {
        return c.pips().totalValue(c.stats().powerPipsCountDouble(spell.school(), c.school()));
    }

    // ---------------------------------------------------------- resolution

    /**
     * Resout le round complet (resolution + fin de round) puis prepare le round suivant.
     * @return tous les evenements produits
     */
    public List<BattleEvent> resolveRound() {
        if (phase != Phase.PLANNING) throw new IllegalStateException("Pas en phase de planification");
        phase = Phase.RESOLUTION;

        for (Combatant actor : new ArrayList<>(turnOrder)) {
            if (!actor.alive()) continue;
            if (actor.isStunned()) {
                emit(new BattleEvent.Stunned(actor.name()));
                continue;
            }
            BattleAction action = actions.getOrDefault(actor.id(), BattleAction.pass());
            switch (action.type()) {
                case CAST -> performCast(actor, action);
                case FLEE -> {
                    actor.flee();
                    emit(new BattleEvent.Fled(actor.name()));
                }
                case PASS -> emit(new BattleEvent.Passed(actor.name()));
            }
            if (checkEnd()) return drainEvents();
        }

        endOfRound();
        if (checkEnd()) return drainEvents();

        round++;
        if (round > config.maxRounds()) {
            draw = true;
            winner = null;
            finish();
            return drainEvents();
        }
        beginRound();
        return drainEvents();
    }

    private void performCast(Combatant caster, BattleAction action) {
        List<Deck.Card> hand = caster.deck().hand();
        if (action.handIndex() < 0 || action.handIndex() >= hand.size()) return;
        Spell spell = SpellBook.get(hand.get(action.handIndex()).spellId());
        boolean doublePower = caster.stats().powerPipsCountDouble(spell.school(), caster.school());
        int cost = pipsFor(caster, spell, action.pipsToSpend());
        if (cost < 0 || !caster.pips().canPay(cost, doublePower)) {
            emit(new BattleEvent.Info(caster.name() + " n'a pas assez de pips et passe son tour.",
                    caster.name() + " does not have enough pips and passes."));
            return;
        }
        caster.deck().play(action.handIndex());
        caster.pips().pay(cost, doublePower);

        // ---- fizzle
        int accuracy = effectiveAccuracy(caster, spell);
        if (config.fizzleEnabled() && !rng.chance(accuracy)) {
            emit(new BattleEvent.Fizzle(caster.name(), spell.id(), spell.name(false)));
            host.onFizzle(this, caster, spell);
            consumeAccuracyCharms(caster);
            return;
        }

        List<Combatant> targets = resolveTargets(caster, spell, action.targetId());
        List<String> targetNames = targets.stream().map(Combatant::name).toList();
        emit(new BattleEvent.Cast(caster.name(), spell.id(), spell.name(false), targetNames, cost, spell.school(), false));
        host.onSpellCast(this, caster, spell, targets);

        consumeAccuracyCharms(caster);
        int bladePercent = consumeBlades(caster, spell.school());
        int weaknessPercent = consumeWeakness(caster);
        applyEffects(caster, spell, targets, cost, bladePercent, weaknessPercent);
    }

    /** Precision effective d'un sort pour ce lanceur. */
    public int effectiveAccuracy(Combatant caster, Spell spell) {
        int acc = spell.accuracy();
        acc += caster.stats().accuracyBonus();
        for (Status s : caster.statuses(Status.Type.ACCURACY_CHARM)) acc += s.percent();
        if (aura != null && aura.type() == SpellEffect.AuraType.ACCURACY_BOOST) acc += aura.percent();
        return Math.max(0, Math.min(100, acc));
    }

    private void consumeAccuracyCharms(Combatant caster) {
        List<Status> charms = new ArrayList<>(caster.statuses(Status.Type.ACCURACY_CHARM));
        for (Status s : charms) {
            s.consumeUse();
            if (s.expired()) {
                caster.remove(s);
                emit(new BattleEvent.StatusExpired(caster.id(), caster.name(), describe(s)));
            }
        }
    }

    private List<Combatant> resolveTargets(Combatant caster, Spell spell, String targetId) {
        List<Combatant> out = new ArrayList<>();
        switch (spell.targetMode()) {
            case SELF -> out.add(caster);
            case ALL_ALLIES -> out.addAll(sameTeam(caster, true));
            case ALLY -> {
                Combatant t = targetId == null ? caster : byId.get(targetId);
                out.add(t != null && t.alive() && t.team() == caster.team() ? t : caster);
            }
            case ALL_ENEMIES -> out.addAll(otherTeam(caster));
            case SINGLE -> {
                Combatant t = targetId == null ? null : byId.get(targetId);
                if (t == null || !t.alive()) t = defaultTarget(caster, spell);
                if (t != null) out.add(t);
            }
        }
        return out;
    }

    /** Membres vivants de l'equipe du combattant ; {@code includeSelf} ajoute le lanceur. */
    private List<Combatant> sameTeam(Combatant c, boolean includeSelf) {
        List<Combatant> out = new ArrayList<>();
        for (Combatant other : combatants) {
            if (!other.alive() || other.team() != c.team()) continue;
            if (other == c && !includeSelf) continue;
            out.add(other);
        }
        return out;
    }

    /** Membres vivants du camp adverse. */
    private List<Combatant> otherTeam(Combatant c) {
        List<Combatant> out = new ArrayList<>();
        for (Combatant other : combatants) {
            if (other.alive() && other.team() != c.team()) out.add(other);
        }
        return out;
    }

    /** Cible par defaut quand le client n'en fournit pas : la plus faible du camp vise. */
    private Combatant defaultTarget(Combatant caster, Spell spell) {
        boolean offensive = spell.isAttack()
                || spell.kind() == com.arcanumrealms.core.spell.SpellKind.TRAP
                || spell.kind() == com.arcanumrealms.core.spell.SpellKind.WEAKNESS
                || spell.kind() == com.arcanumrealms.core.spell.SpellKind.ACCURACY_DEBUFF
                || spell.kind() == com.arcanumrealms.core.spell.SpellKind.STUN;
        List<Combatant> pool = offensive ? otherTeam(caster) : sameTeam(caster, true);
        if (pool.isEmpty()) pool = offensive ? sameTeam(caster, true) : otherTeam(caster);
        return pool.isEmpty() ? null : pool.get(0);
    }

    // ------------------------------------------------------------- effets

    private void applyEffects(Combatant caster, Spell spell, List<Combatant> targets, int pips,
                              int bladePercent, int weaknessPercent) {
        for (SpellEffect effect : spell.effects()) {
            applyEffect(caster, spell, effect, targets, pips, bladePercent, weaknessPercent);
        }
    }

    private void applyEffect(Combatant caster, Spell spell, SpellEffect effect, List<Combatant> targets,
                             int pips, int bladePercent, int weaknessPercent) {
        if (effect instanceof SpellEffect.Damage d) {
            int mult = d.perPip() ? pips : 1;
            School school = d.school() == null ? caster.school() : d.school();
            for (Combatant t : targets) {
                strike(caster, t, rng.between(d.min() * mult, d.max() * mult), school, bladePercent, weaknessPercent, true);
            }
        } else if (effect instanceof SpellEffect.Drain d) {
            School school = d.school() == null ? caster.school() : d.school();
            int total = 0;
            for (Combatant t : targets) {
                Hit hit = strike(caster, t, rng.between(d.min(), d.max()), school, bladePercent, weaknessPercent, true);
                total += hit.applied();
            }
            int healed = (int) Math.round(total * d.fraction());
            if (healed > 0) doHeal(caster, caster, healed);
        } else if (effect instanceof SpellEffect.Dot d) {
            School school = d.school() == null ? caster.school() : d.school();
            int trapPercent = consumeTraps(targets.isEmpty() ? caster : targets.get(0), school);
            for (Combatant t : targets) {
                int perTick = boostedBase(caster, rng.between(d.min(), d.max()), school, bladePercent, trapPercent, weaknessPercent);
                Status status = Status.dot(perTick, perTick, school, d.rounds(), spell.id(), spell.name(false));
                t.addStatus(status);
                emit(new BattleEvent.StatusApplied(t.id(), t.name(), describe(status)));
            }
        } else if (effect instanceof SpellEffect.Heal h) {
            int mult = h.perPip() ? pips : 1;
            for (Combatant t : targets) doHeal(caster, t, rng.between(h.min() * mult, h.max() * mult));
        } else if (effect instanceof SpellEffect.Hot h) {
            for (Combatant t : targets) {
                Status status = Status.hot(h.min(), h.max(), h.rounds(), spell.id(), spell.name(false));
                t.addStatus(status);
                emit(new BattleEvent.StatusApplied(t.id(), t.name(), describe(status)));
            }
        } else if (effect instanceof SpellEffect.Absorb a) {
            for (Combatant t : targets) addStatus(t, Status.absorb(a.amount(), spell.id(), spell.name(false)));
        } else if (effect instanceof SpellEffect.Blade b) {
            for (Combatant t : targets) addStatus(t, Status.blade(b.percent(), b.schools(), b.uses(), spell.id(), spell.name(false)));
        } else if (effect instanceof SpellEffect.Trap t0) {
            for (Combatant t : targets) addStatus(t, Status.trap(t0.percent(), t0.schools(), t0.uses(), spell.id(), spell.name(false)));
        } else if (effect instanceof SpellEffect.SelfTrap st) {
            addStatus(caster, Status.trap(st.percent(), st.schools(), st.uses(), spell.id(), spell.name(false)));
        } else if (effect instanceof SpellEffect.Shield sh) {
            for (Combatant t : targets) addStatus(t, Status.shield(sh.percent(), sh.schools(), sh.uses(), spell.id(), spell.name(false)));
        } else if (effect instanceof SpellEffect.Weakness w) {
            for (Combatant t : targets) addStatus(t, Status.weakness(w.percent(), w.uses(), spell.id(), spell.name(false)));
        } else if (effect instanceof SpellEffect.AccuracyMod a) {
            for (Combatant t : targets) addStatus(t, Status.accuracy(a.percent(), a.uses(), spell.id(), spell.name(false)));
        } else if (effect instanceof SpellEffect.Stun s) {
            for (Combatant t : targets) addStatus(t, Status.stun(s.rounds(), spell.id(), spell.name(false)));
        } else if (effect instanceof SpellEffect.HealBlade hb) {
            for (Combatant t : targets) addStatus(t, Status.healBlade(hb.percent(), hb.uses(), spell.id(), spell.name(false)));
        } else if (effect instanceof SpellEffect.PierceAura p) {
            for (Combatant t : targets) addStatus(t, Status.pierceAura(p.percent(), p.uses(), spell.id(), spell.name(false)));
        } else if (effect instanceof SpellEffect.Aura a) {
            aura = new GlobalAura(a.type(), a.percent(), a.schools(), a.rounds(), spell.name(false));
            emit(new BattleEvent.AuraChanged(describeAura(aura)));
        } else if (effect instanceof SpellEffect.Summon s) {
            summon(caster, s.minionId());
        } else if (effect instanceof SpellEffect.GainPips g) {
            caster.pips().add(g.pips(), false);
        } else if (effect instanceof SpellEffect.DrawCards dc) {
            caster.deck().drawExtra(dc.count(), round);
        } else if (effect instanceof SpellEffect.Reshuffle) {
            caster.deck().reshuffleAll(round);
        } else if (effect instanceof SpellEffect.Cleanse cl) {
            CleanseResult result = cleanse(caster, cl.target());
            emit(new BattleEvent.Info(result.french, result.english));
        } else if (effect instanceof SpellEffect.StealCharm) {
            for (Combatant t : targets) {
                Optional<Status> blade = t.statuses(Status.Type.BLADE).stream().findFirst();
                if (blade.isPresent()) {
                    Status stolen = blade.get();
                    t.remove(stolen);
                    caster.addStatus(stolen);
                    emit(new BattleEvent.Info(caster.name() + " vole " + describe(stolen) + " a " + t.name() + ".",
                            caster.name() + " steals " + describe(stolen) + " from " + t.name() + "."));
                }
            }
        } else if (effect instanceof SpellEffect.Disarm) {
            for (Combatant t : targets) {
                Optional<Status> blade = t.statuses(Status.Type.BLADE).stream().findFirst();
                if (blade.isPresent()) {
                    Status removed = blade.get();
                    t.remove(removed);
                    emit(new BattleEvent.Info(removed.sourceName() + " est retire a " + t.name() + ".",
                            removed.sourceName() + " is removed from " + t.name() + "."));
                }
            }
        } else if (effect instanceof SpellEffect.SelfDamage sd) {
            int amount = sd.flat() + (int) (caster.maxHealth() * (sd.percentOfMax() / 100.0));
            caster.applyDamage(amount);
            emit(new BattleEvent.Info(caster.name() + " se sacrifie " + amount + " points de vie.",
                    caster.name() + " sacrifices " + amount + " health."));
        } else if (effect instanceof SpellEffect.PipBlade pb) {
            boolean doublePower = caster.stats().powerPipsCountDouble(pb.school(), caster.school());
            int invested = caster.pips().totalValue(doublePower);
            caster.pips().clear();
            if (invested > 0) {
                addStatus(caster, Status.blade(invested * pb.percentPerPip(), Set.of(pb.school()), 1, spell.id(), spell.name(false)));
            }
        } else if (effect instanceof SpellEffect.RandomSchoolDamage r) {
            List<School> schools = new ArrayList<>(r.schools());
            School school = rng.pick(schools);
            for (Combatant t : targets) {
                strike(caster, t, rng.between(r.min(), r.max()), school, bladePercent, weaknessPercent, true);
            }
        } else if (effect instanceof SpellEffect.OneOf o) {
            SpellEffect chosen = rng.pick(o.options());
            emit(new BattleEvent.Info("Effet aleatoire : " + describeEffect(chosen), "Random effect: " + describeEffect(chosen)));
            applyEffect(caster, spell, chosen, targets, pips, bladePercent, weaknessPercent);
        }
    }

    private record CleanseResult(String french, String english) {}

    private CleanseResult cleanse(Combatant target, SpellEffect.CleanseTarget type) {
        int removed = switch (type) {
            case HARMFUL_CHARM -> target.removeStatuses(s -> s.type() == Status.Type.WEAKNESS || s.type() == Status.Type.ACCURACY_CHARM);
            case DAMAGE_OVER_TIME -> target.removeStatuses(s -> s.type() == Status.Type.DOT);
            case ALL -> target.removeStatuses(Status::isHarmful);
        };
        return new CleanseResult(removed + " effet(s) nefaste(s) retire(s) a " + target.name() + ".",
                removed + " harmful effect(s) removed from " + target.name() + ".");
    }

    private void addStatus(Combatant target, Status status) {
        target.addStatus(status);
        emit(new BattleEvent.StatusApplied(target.id(), target.name(), describe(status)));
    }

    // ------------------------------------------------------- calcul des degats

    /**
     * Applique une attaque complete sur une cible, dans l'ordre exact de Wizard101 :
     * base -> bonus d'ecole -> lames -> pieges -> faiblesse -> critique -> resistance/percee
     * -> boucliers -> absorption -> resistance fixe -> points de vie.
     *
     * @param base           degats de base deja tires
     * @param bladePercent   bonus de lames consomme par le lanceur (une fois par sort)
     * @param weaknessPercent malus de faiblesse consomme par le lanceur (une fois par sort)
     */
    private Hit strike(Combatant caster, Combatant target, int base, School school,
                       int bladePercent, int weaknessPercent, boolean allowCrit) {
        if (target == null || !target.alive()) return new Hit(0, 0, 0, false);
        int trapPercent = consumeTraps(target, school);

        double boosted = boostedBase(caster, base, school, bladePercent, trapPercent, weaknessPercent);
        double afterBoost = boosted;

        int pierce = caster == null ? 0 : caster.stats().pierce(school) + consumePierceAura(caster);
        int resist = Math.max(0, Math.min(95, target.stats().resist(school) - pierce));
        boosted *= (1 - resist / 100.0);

        boolean critical = false;
        if (allowCrit && caster != null) {
            double mult = rollCrit(caster, target);
            if (mult > 1.0) {
                boosted *= mult;
                critical = true;
                afterBoost *= mult;
            }
        }

        int damage = Math.max(1, (int) Math.round(boosted));
        int resisted = Math.max(0, (int) Math.round(afterBoost - boosted));

        for (Status shield : new ArrayList<>(target.statuses(Status.Type.SHIELD))) {
            if (!shield.matchesSchool(school)) continue;
            int before = damage;
            damage = (int) Math.round(damage * (1 - shield.percent() / 100.0));
            resisted += before - damage;
            shield.consumeUse();
            if (shield.expired()) {
                target.remove(shield);
                emit(new BattleEvent.StatusExpired(target.id(), target.name(), describe(shield)));
            }
        }

        int absorbed = 0;
        if (damage > 0) {
            for (Status absorb : new ArrayList<>(target.statuses(Status.Type.ABSORB))) {
                int used = absorb.absorb(damage);
                absorbed += used;
                damage -= used;
                if (absorb.expired()) {
                    target.remove(absorb);
                    emit(new BattleEvent.StatusExpired(target.id(), target.name(), describe(absorb)));
                }
                if (damage <= 0) break;
            }
        }

        int flat = Math.min(damage, target.stats().flatResist());
        damage -= flat;
        resisted += flat;

        int applied = target.applyDamage(damage);
        Hit hit = new Hit(applied, resisted, absorbed, critical);
        emit(new BattleEvent.Damage(caster == null ? "" : caster.name(), target.id(), target.name(),
                applied, school, critical, resisted, absorbed));
        host.onDamage(this, caster, target, applied, school, critical);
        if (!target.alive()) {
            emit(new BattleEvent.Defeated(target.id(), target.name(), target.isPlayer()));
            host.onDefeated(this, target);
        }
        return hit;
    }

    /** Degats de base augmentes par l'equipement, les lames, les pieges et la faiblesse (sans critique). */
    private int boostedBase(Combatant caster, int base, School school, int bladePercent, int trapPercent, int weaknessPercent) {
        double dmg = base;
        if (caster != null) {
            int damageBonus = caster.stats().damageBonus(school) + auraDamageBonus(school);
            dmg *= 1 + damageBonus / 100.0;
            dmg *= 1 + bladePercent / 100.0;
            dmg *= 1 - weaknessPercent / 100.0;
        }
        dmg *= 1 + trapPercent / 100.0;
        dmg *= config.damageScale();
        return Math.max(1, (int) Math.round(dmg));
    }

    /**
     * Jet de critique d'une attaque.
     * @return le multiplicateur applique (1.0 si pas de critique)
     */
    private double rollCrit(Combatant caster, Combatant target) {
        if (config.critMode() == null) return 1.0;
        if (config.critMode() == BattleConfig.CritMode.FLAT) {
            if (config.flatCritChance() <= 0) return 1.0;
            return rng.chance(config.flatCritChance()) ? config.critMultiplier() : 1.0;
        }
        int c = caster.stats().critRating();
        if (c <= 0) return 1.0;
        int b = target.stats().blockRating();
        double chance = critChance(caster, target);
        // Formule de Wizard101 : le critique doit passer son jet ET eviter le jet de blocage
        boolean lands = rng.chance(chance) && !rng.chance(1 - chance);
        if (!lands) return 1.0;
        return config.critMultiplier() * (1.0 - (1.5 * b) / (c + 3.0 * b));
    }

    /**
     * Chance de critique selon la formule de Wizard101 : {@code 3*l*c/(3c+b)},
     * avec {@code l = niveau/100} plafonne a 1, {@code c} le score de critique
     * et {@code b} le score de blocage de la cible.
     */
    public double critChance(Combatant caster, Combatant target) {
        if (config.critMode() == BattleConfig.CritMode.FLAT) return config.flatCritChance() / 100.0;
        int c = caster.stats().critRating();
        if (c <= 0) return 0;
        int b = target.stats().blockRating();
        double l = Math.min(1.0, caster.level() / 100.0);
        return (3.0 * l * c) / (3.0 * c + b);
    }

    private int consumeBlades(Combatant caster, School spellSchool) {
        int total = 0;
        for (Status s : new ArrayList<>(caster.statuses(Status.Type.BLADE))) {
            if (!s.matchesSchool(spellSchool)) continue;
            total += s.percent();
            s.consumeUse();
            if (s.expired()) {
                caster.remove(s);
                emit(new BattleEvent.StatusExpired(caster.id(), caster.name(), describe(s)));
            }
        }
        return total;
    }

    private int consumeTraps(Combatant target, School school) {
        int total = 0;
        for (Status s : new ArrayList<>(target.statuses(Status.Type.TRAP))) {
            if (!s.matchesSchool(school)) continue;
            total += s.percent();
            s.consumeUse();
            if (s.expired()) {
                target.remove(s);
                emit(new BattleEvent.StatusExpired(target.id(), target.name(), describe(s)));
            }
        }
        return total;
    }

    private int consumeWeakness(Combatant caster) {
        int total = 0;
        for (Status s : new ArrayList<>(caster.statuses(Status.Type.WEAKNESS))) {
            total += s.percent();
            s.consumeUse();
            if (s.expired()) {
                caster.remove(s);
                emit(new BattleEvent.StatusExpired(caster.id(), caster.name(), describe(s)));
            }
        }
        return total;
    }

    private int consumePierceAura(Combatant caster) {
        int total = 0;
        for (Status s : new ArrayList<>(caster.statuses(Status.Type.PIERCE_AURA))) {
            total += s.percent();
            s.consumeUse();
            if (s.expired()) {
                caster.remove(s);
                emit(new BattleEvent.StatusExpired(caster.id(), caster.name(), describe(s)));
            }
        }
        return total;
    }

    private void doHeal(Combatant caster, Combatant target, int amount) {
        if (target == null || !target.alive()) return;
        double heal = amount;
        if (caster != null) {
            heal *= 1 + (caster.stats().healBoost() + auraHealBonus()) / 100.0;
            heal *= 1 + consumeHealBlades(caster) / 100.0;
        }
        heal *= 1 + target.stats().incomingHealBoost() / 100.0;
        if (aura != null && aura.type() == SpellEffect.AuraType.HEAL_REDUCTION) {
            heal *= 1 - aura.percent() / 100.0;
        }
        heal *= config.healScale();
        boolean critical = false;
        if (caster != null && config.critMode() != null && caster.stats().critRating() > 0) {
            if (rng.chance(critChance(caster, target))) {
                heal *= config.critMultiplier();
                critical = true;
            }
        }
        int applied = target.applyHeal((int) Math.round(heal));
        emit(new BattleEvent.Heal(caster == null ? "" : caster.name(), target.name(), applied, critical));
        host.onHeal(this, caster, target, applied, critical);
    }

    private int consumeHealBlades(Combatant caster) {
        int total = 0;
        for (Status s : new ArrayList<>(caster.statuses(Status.Type.HEAL_BLADE))) {
            total += s.percent();
            s.consumeUse();
            if (s.expired()) caster.remove(s);
        }
        return total;
    }

    private int auraDamageBonus(School school) {
        if (aura == null || aura.type() != SpellEffect.AuraType.DAMAGE_BOOST) return 0;
        return aura.schools().isEmpty() || aura.schools().contains(school) ? aura.percent() : 0;
    }

    private int auraHealBonus() {
        if (aura == null || aura.type() != SpellEffect.AuraType.HEAL_BOOST) return 0;
        return aura.percent();
    }

    // ------------------------------------------------------------- minions

    private void summon(Combatant caster, String minionId) {
        CreatureDef def = Creatures.get(minionId);
        long existing = combatants.stream().filter(c -> c.team() == caster.team() && c.isMinion()).count();
        if (existing >= 3) {
            emit(new BattleEvent.Info("Trop de serviteurs deja presents.", "Too many minions already."));
            return;
        }
        String id = "minion-" + (++minionCounter);
        Combatant minion = Combatant.builder(id, def.name(false))
                .kind(Combatant.Kind.MINION)
                .school(def.school())
                .level(def.level())
                .team(caster.team())
                .stats(com.arcanumrealms.core.util.StatsFactory.forCreature(def))
                .def(def)
                .ai(new com.arcanumrealms.core.ai.EnemyAI())
                .health(def.health())
                .deck(expandDeck(def))
                .handSize(config.handSize())
                .rng(rng)
                .build();
        combatants.add(minion);
        byId.put(id, minion);
        turnOrder.add(minion);
        minion.deck().refillHand(round);
        emit(new BattleEvent.Summoned(caster.name(), def.name(false), id));
        host.onSummon(this, caster, minion);
    }

    private static List<String> expandDeck(CreatureDef def) {
        return new ArrayList<>(def.deck());
    }

    // ------------------------------------------------------- fin de round

    private void endOfRound() {
        for (Combatant c : combatants) {
            if (!c.alive()) continue;
            for (Status s : new ArrayList<>(c.statuses())) {
                if (s.type() == Status.Type.DOT) {
                    int raw = rng.between(s.minValue(), s.maxValue());
                    int resist = Math.max(0, Math.min(95, c.stats().resist(s.school())));
                    int dmg = Math.max(1, (int) Math.round(raw * (1 - resist / 100.0)));
                    for (Status shield : new ArrayList<>(c.statuses(Status.Type.SHIELD))) {
                        if (!shield.matchesSchool(s.school())) continue;
                        dmg = (int) Math.round(dmg * (1 - shield.percent() / 100.0));
                        shield.consumeUse();
                        if (shield.expired()) c.remove(shield);
                    }
                    int applied = c.applyDamage(dmg);
                    emit(new BattleEvent.Damage(s.sourceName(), c.id(), c.name(), applied, s.school(), false, 0, 0));
                    host.onDamage(this, null, c, applied, s.school(), false);
                    if (!c.alive()) {
                        emit(new BattleEvent.Defeated(c.id(), c.name(), c.isPlayer()));
                        host.onDefeated(this, c);
                    }
                } else if (s.type() == Status.Type.HOT) {
                    doHeal(null, c, rng.between(s.minValue(), s.maxValue()));
                }
            }
            for (Status s : new ArrayList<>(c.statuses())) {
                s.tickRound();
                if (s.expired()) {
                    c.remove(s);
                    emit(new BattleEvent.StatusExpired(c.id(), c.name(), describe(s)));
                }
            }
        }
        if (aura != null) {
            GlobalAura ticked = new GlobalAura(aura.type(), aura.percent(), aura.schools(), aura.rounds() - 1, aura.sourceName());
            aura = ticked.rounds() > 0 ? ticked : null;
            if (aura == null) emit(new BattleEvent.AuraChanged("L'aura globale se dissipe."));
        }
        emit(new BattleEvent.RoundEnd(round));
    }

    private boolean checkEnd() {
        boolean playersAlive = combatants.stream().anyMatch(c -> c.team() == Team.PLAYERS && c.alive());
        boolean enemiesAlive = combatants.stream().anyMatch(c -> c.team() == Team.ENEMIES && c.alive());
        if (playersAlive && enemiesAlive) return false;
        if (!playersAlive && !enemiesAlive) {
            draw = true;
            winner = null;
        } else {
            winner = playersAlive ? Team.PLAYERS : Team.ENEMIES;
        }
        finish();
        return true;
    }

    private void finish() {
        phase = Phase.FINISHED;
        emit(new BattleEvent.Ended(winner == Team.PLAYERS, draw));
        emit(new BattleEvent.Info("Fin du duel en " + round + " round(s).", "Duel finished in " + round + " round(s)."));
        host.onBattleEnd(this, winner);
    }

    // ------------------------------------------------------------- sortie

    private void emit(BattleEvent event) {
        pending.add(event);
        log.add(event);
        host.onEvent(this, event);
    }

    private String describe(Status status) {
        return switch (status.type()) {
            case BLADE -> "lame +" + status.percent() + "%" + schoolSuffix(status) + " (" + status.uses() + " sort)";
            case TRAP -> "piege +" + status.percent() + "%" + schoolSuffix(status) + " (" + status.uses() + " sort)";
            case SHIELD -> "bouclier -" + status.percent() + "%" + schoolSuffix(status) + " (" + status.uses() + " attaque)";
            case WEAKNESS -> "faiblesse -" + status.percent() + "% de degats";
            case ACCURACY_CHARM -> (status.percent() >= 0 ? "precision +" : "precision ") + status.percent() + "%";
            case HEAL_BLADE -> "soins +" + status.percent() + "%";
            case PIERCE_AURA -> "percee " + status.percent() + "%";
            case DOT -> status.minValue() + " degats par round pendant " + status.rounds() + " rounds";
            case HOT -> status.minValue() + " soins par round pendant " + status.rounds() + " rounds";
            case ABSORB -> "absorption " + status.remainingValue();
            case STUN -> "etourdi pendant " + status.rounds() + " round(s)";
        };
    }

    private String schoolSuffix(Status status) {
        if (status.schools().isEmpty()) return "";
        StringBuilder sb = new StringBuilder(" (");
        int i = 0;
        for (School s : status.schools()) {
            if (i++ > 0) sb.append(", ");
            sb.append(s.displayFr());
        }
        return sb.append(")").toString();
    }

    private String describeAura(GlobalAura a) {
        String scope = a.schools().isEmpty() ? "toutes les ecoles" : a.schools().toString();
        return switch (a.type()) {
            case DAMAGE_BOOST -> "Aura : +" + a.percent() + "% de degats (" + scope + ") pendant " + a.rounds() + " rounds";
            case HEAL_BOOST -> "Aura : +" + a.percent() + "% de soins pendant " + a.rounds() + " rounds";
            case HEAL_REDUCTION -> "Aura : -" + a.percent() + "% de soins pendant " + a.rounds() + " rounds";
            case ACCURACY_BOOST -> "Aura : +" + a.percent() + "% de precision pendant " + a.rounds() + " rounds";
        };
    }

    private String describeEffect(SpellEffect effect) {
        if (effect instanceof SpellEffect.Damage d) return d.min() + "-" + d.max() + " degats";
        return effect.toString();
    }
}
