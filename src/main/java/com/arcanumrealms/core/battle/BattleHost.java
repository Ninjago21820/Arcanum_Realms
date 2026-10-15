package com.arcanumrealms.core.battle;

import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;

import java.util.List;

/**
 * Pont entre le moteur de duel (independant du jeu) et Minecraft.
 *
 * <p>Toutes les methodes ont une implementation vide : le serveur ne surcharge
 * que ce dont il a besoin (faire apparaitre un serviteur, jouer un son, etc.).
 */
public interface BattleHost {

    default void onBattleStart(Battle battle) {}

    default void onSpellCast(Battle battle, Combatant caster, Spell spell, List<Combatant> targets) {}

    default void onFizzle(Battle battle, Combatant caster, Spell spell) {}

    default void onDamage(Battle battle, Combatant caster, Combatant target, int amount, School school, boolean critical) {}

    default void onHeal(Battle battle, Combatant caster, Combatant target, int amount, boolean critical) {}

    default void onDefeated(Battle battle, Combatant combatant) {}

    default void onSummon(Battle battle, Combatant summoner, Combatant minion) {}

    default void onEvent(Battle battle, BattleEvent event) {}

    default void onBattleEnd(Battle battle, Team winner) {}
}
