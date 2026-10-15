package com.arcanumrealms.core.battle;

import com.arcanumrealms.core.ai.EnemyAI;
import com.arcanumrealms.core.card.Deck;
import com.arcanumrealms.core.minion.CreatureDef;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.status.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Un participant au duel : sorcier joueur, creature adverse ou serviteur invoque.
 *
 * <p>L'objet ne connait rien de Minecraft : le serveur lui associe une entite
 * (joueur ou creature) via {@code BattleHost}.
 */
public final class Combatant {

    public enum Kind { WIZARD, ENEMY, MINION }

    private final String id;
    private final String name;
    private final Kind kind;
    private final School school;
    private final int level;
    private final Team team;
    private final CombatantStats stats;
    private final CreatureDef def;          // null pour un joueur
    private final EnemyAI ai;               // null pour un joueur
    private final CombatantPips pips = new CombatantPips();
    private final List<Status> statuses = new ArrayList<>();
    private final Deck deck;
    private final int maxHealth;
    private final int turnOrder;

    private int health;
    private boolean defeated;
    private boolean fled;

    private Combatant(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.kind = b.kind;
        this.school = b.school;
        this.level = b.level;
        this.team = b.team;
        this.stats = b.stats;
        this.def = b.def;
        this.ai = b.ai;
        this.maxHealth = b.maxHealth;
        this.health = b.maxHealth;
        this.deck = new Deck(b.deck, b.handSize, b.rng);
        this.turnOrder = b.turnOrder;
    }

    // ------------------------------------------------------------ accessors

    public String id() { return id; }
    public String name() { return name; }
    public Kind kind() { return kind; }
    public boolean isPlayer() { return kind == Kind.WIZARD; }
    public boolean isMinion() { return kind == Kind.MINION; }
    public School school() { return school; }
    public int level() { return level; }
    public Team team() { return team; }
    public CombatantStats stats() { return stats; }
    public CreatureDef def() { return def; }
    public EnemyAI ai() { return ai; }
    public boolean isAI() { return ai != null; }
    public CombatantPips pips() { return pips; }
    public Deck deck() { return deck; }
    public int maxHealth() { return maxHealth; }
    public int health() { return health; }
    public int turnOrder() { return turnOrder; }
    public boolean fled() { return fled; }
    public boolean alive() { return !defeated && !fled && health > 0; }
    public boolean defeated() { return defeated; }
    public double healthPercent() { return maxHealth <= 0 ? 0 : (double) health / maxHealth; }

    public List<Status> statuses() { return Collections.unmodifiableList(statuses); }
    public List<Status> statuses(Status.Type type) {
        return statuses.stream().filter(s -> s.type() == type).toList();
    }
    public List<Status> statuses(Predicate<Status> predicate) {
        return statuses.stream().filter(predicate).toList();
    }

    public void addStatus(Status status) { statuses.add(status); }

    /** Retire un effet precis (identite de l'objet). */
    public boolean remove(Status status) { return statuses.remove(status); }

    /** Retire le premier effet correspondant. */
    public boolean removeStatus(Predicate<Status> predicate) {
        return statuses.removeIf(predicate);
    }

    public int removeStatuses(Predicate<Status> predicate) {
        int before = statuses.size();
        statuses.removeIf(predicate);
        return before - statuses.size();
    }

    public boolean isStunned() {
        return statuses.stream().anyMatch(s -> s.type() == Status.Type.STUN);
    }

    public void wakeUp() {
        statuses.removeIf(s -> s.type() == Status.Type.STUN);
    }

    /** Applique des degats et renvoie le montant reellement perdu. */
    public int applyDamage(int amount) {
        if (amount <= 0 || !alive()) return 0;
        int real = Math.min(amount, health);
        health -= real;
        if (real > 0) wakeUp();
        if (health <= 0) {
            health = 0;
            defeated = true;
            statuses.removeIf(s -> s.type() == Status.Type.DOT || s.type() == Status.Type.HOT);
        }
        return real;
    }

    public int applyHeal(int amount) {
        if (amount <= 0 || !alive()) return 0;
        int real = Math.min(amount, maxHealth - health);
        health += real;
        return real;
    }

    /** Le sorcier quitte le duel (fuite). Il garde ses points de vie. */
    public void flee() {
        this.fled = true;
        statuses.clear();
    }

    /** Restaure la creature entre deux duels. */
    public void resetForNextBattle() {
        this.health = maxHealth;
        this.defeated = false;
        this.fled = false;
        this.statuses.clear();
        this.pips.clear();
    }

    // -------------------------------------------------------------- factory

    public static Builder builder(String id, String name) { return new Builder(id, name); }

    public static final class Builder {
        private final String id;
        private final String name;
        private Kind kind = Kind.ENEMY;
        private School school = School.EQUILIBRIUM;
        private int level = 1;
        private Team team = Team.ENEMIES;
        private CombatantStats stats = CombatantStats.EMPTY;
        private CreatureDef def;
        private EnemyAI ai;
        private int maxHealth = 100;
        private List<String> deck = new ArrayList<>();
        private int handSize = 7;
        private int turnOrder;
        private com.arcanumrealms.core.util.Rng rng = new com.arcanumrealms.core.util.Rng();

        Builder(String id, String name) { this.id = id; this.name = name; }

        public Builder kind(Kind k) { this.kind = k; return this; }
        public Builder school(School s) { this.school = s; return this; }
        public Builder level(int l) { this.level = l; return this; }
        public Builder team(Team t) { this.team = t; return this; }
        public Builder stats(CombatantStats s) { this.stats = s; return this; }
        public Builder def(CreatureDef d) { this.def = d; return this; }
        public Builder ai(EnemyAI a) { this.ai = a; return this; }
        public Builder health(int h) { this.maxHealth = h; return this; }
        public Builder deck(List<String> spells) { this.deck = new ArrayList<>(spells); return this; }
        public Builder handSize(int size) { this.handSize = size; return this; }
        public Builder turnOrder(int index) { this.turnOrder = index; return this; }
        public Builder rng(com.arcanumrealms.core.util.Rng rng) { this.rng = rng; return this; }

        public Combatant build() {
            return new Combatant(this);
        }
    }

    @Override
    public String toString() {
        return name + "(" + health + "/" + maxHealth + ", " + school.id() + ")";
    }
}
