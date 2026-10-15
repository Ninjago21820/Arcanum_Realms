package com.arcanumrealms.core.ai;

import com.arcanumrealms.core.battle.Battle;
import com.arcanumrealms.core.battle.BattleAction;
import com.arcanumrealms.core.battle.Combatant;
import com.arcanumrealms.core.card.Deck;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.core.spell.SpellEffect;
import com.arcanumrealms.core.status.Status;

import java.util.Comparator;
import java.util.List;

/**
 * Intelligence des creatures et des serviteurs.
 *
 * <p>Le comportement imite celui des creatures de Wizard101 : elles placent une lame
 * quand elles en ont une, posent un piege avant leurs gros sorts, se soignent quand
 * elles sont blessees, puis frappent avec le meilleur sort qu'elles peuvent payer.
 * Si elles n'ont rien d'utile a faire, elles passent leur tour pour economiser des pips.
 */
public final class EnemyAI {

    public enum Aggressiveness {
        /** Les creatures economisent souvent leurs pips. */
        CAREFUL,
        /** Comportement par defaut. */
        NORMAL,
        /** Les creatures frappent des qu'elles peuvent. */
        AGGRESSIVE
    }

    private final Aggressiveness aggressiveness;

    public EnemyAI() { this(Aggressiveness.NORMAL); }

    public EnemyAI(Aggressiveness aggressiveness) { this.aggressiveness = aggressiveness; }

    public BattleAction choose(Battle battle, Combatant self) {
        List<Combatant> enemies = battle.combatants().stream()
                .filter(c -> c.team() != self.team() && c.alive()).toList();
        if (enemies.isEmpty()) return BattleAction.pass();

        List<Combatant> allies = battle.combatants().stream()
                .filter(c -> c.team() == self.team() && c.alive()).toList();
        Combatant weakestEnemy = enemies.stream()
                .min(Comparator.comparingInt(Combatant::health)).orElse(enemies.get(0));
        Combatant hurtAlly = allies.stream()
                .min(Comparator.comparingDouble(Combatant::healthPercent)).orElse(self);

        List<Deck.Card> hand = self.deck().hand();
        boolean hasBlade = self.statuses().stream().anyMatch(s -> s.type() == Status.Type.BLADE);
        boolean hasAttackInHand = hand.stream().anyMatch(card -> SpellBook.get(card.spellId()).isAttack());
        boolean hasMinion = battle.combatants().stream().anyMatch(c -> c.team() == self.team() && c.isMinion());

        int bestIndex = -1;
        double bestScore = 0;
        String bestTarget = null;
        int bestPips = 0;

        for (int i = 0; i < hand.size(); i++) {
            Spell spell = SpellBook.get(hand.get(i).spellId());
            int cost = battle.pipsFor(self, spell, 0);
            if (cost < 0) continue;
            boolean doublePower = self.stats().powerPipsCountDouble(spell.school(), self.school());
            if (!self.pips().canPay(cost, doublePower)) continue;

            double score = score(battle, self, spell, cost, hasBlade, hasAttackInHand, hasMinion,
                    hurtAlly, enemies.size());
            if (aggressiveness == Aggressiveness.CAREFUL) score *= spell.isAttack() ? 0.8 : 1.15;
            if (aggressiveness == Aggressiveness.AGGRESSIVE) score *= spell.isAttack() ? 1.25 : 0.85;

            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
                bestPips = spell.variablePips() ? battle.pipsFor(self, spell, Integer.MAX_VALUE) : 0;
                bestTarget = targetFor(spell, self, weakestEnemy, hurtAlly);
            }
        }

        if (bestIndex < 0) return BattleAction.pass();
        return BattleAction.cast(bestIndex, bestTarget, bestPips);
    }

    private String targetFor(Spell spell, Combatant self, Combatant weakestEnemy, Combatant hurtAlly) {
        if (spell.targetMode().affectsEnemies()) return weakestEnemy.id();
        if (spell.targetMode() == com.arcanumrealms.core.spell.TargetMode.ALLY
                || spell.targetMode() == com.arcanumrealms.core.spell.TargetMode.ALL_ALLIES) {
            return hurtAlly.id();
        }
        return self.id();
    }

    private double score(Battle battle, Combatant self, Spell spell, int cost, boolean hasBlade,
                         boolean hasAttackInHand, boolean hasMinion, Combatant hurtAlly, int enemyCount) {
        double pipsFactor = 1.0 + 0.06 * cost;
        double aoeTargets = Math.min(enemyCount, 3);
        switch (spell.kind()) {
            case HEAL, HEAL_ALL, HEAL_OVER_TIME -> {
                double missing = 1.0 - Math.min(self.healthPercent(), hurtAlly.healthPercent());
                if (missing < 0.25) return 15;
                return 400 + missing * 900 * pipsFactor;
            }
            case BLADE -> {
                if (hasBlade || !hasAttackInHand) return 40;
                return 300 * pipsFactor;
            }
            case TRAP -> {
                if (!hasAttackInHand) return 45;
                return 260 * pipsFactor;
            }
            case SHIELD -> {
                boolean threat = battle.combatants().stream()
                        .anyMatch(c -> c.team() != self.team() && c.statuses().stream()
                                .anyMatch(s -> s.type() == Status.Type.BLADE));
                return threat ? 220 : 90;
            }
            case WEAKNESS -> {
                return 180;
            }
            case ACCURACY_DEBUFF -> {
                return 150;
            }
            case STUN -> {
                return 200 * pipsFactor;
            }
            case SUMMON -> {
                return hasMinion ? 30 : 320;
            }
            case ABSORB -> {
                return (1.0 - self.healthPercent()) > 0.2 ? 260 : 80;
            }
            case DECK, UTILITY -> {
                return 60;
            }
            case WAND -> {
                return 90;
            }
            case DAMAGE_ALL -> {
                return estimateDamage(spell, cost) * aoeTargets * pipsFactor;
            }
            case DAMAGE, DRAIN, DAMAGE_OVER_TIME -> {
                double estimate = estimateDamage(spell, cost);
                if (spell.targetMode() == com.arcanumrealms.core.spell.TargetMode.ALL_ENEMIES) estimate *= aoeTargets;
                return estimate < 1 ? 10 : estimate * pipsFactor;
            }
        }
        return 10;
    }

    /** Estimation des degats d'un sort : sert a choisir entre deux cartes. */
    public static double estimateDamage(Spell spell, int cost) {
        double total = 0;
        for (SpellEffect e : spell.effects()) {
            total += estimateEffect(e, cost);
        }
        return total;
    }

    private static double estimateEffect(SpellEffect e, int cost) {
        if (e instanceof SpellEffect.Damage d) {
            double mid = (d.min() + d.max()) / 2.0;
            return d.perPip() ? mid * Math.max(1, cost) : mid;
        }
        if (e instanceof SpellEffect.Drain d) {
            return (d.min() + d.max()) / 2.0;
        }
        if (e instanceof SpellEffect.Dot d) {
            return ((d.min() + d.max()) / 2.0) * d.rounds();
        }
        if (e instanceof SpellEffect.RandomSchoolDamage r) {
            return (r.min() + r.max()) / 2.0;
        }
        if (e instanceof SpellEffect.OneOf o) {
            double sum = 0;
            for (SpellEffect option : o.options()) sum += estimateEffect(option, cost);
            return sum / Math.max(1, o.options().size());
        }
        return 0;
    }
}
