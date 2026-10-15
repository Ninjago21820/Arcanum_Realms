package com.arcanumrealms.core.spell;

import com.arcanumrealms.core.minion.Creatures;
import com.arcanumrealms.core.school.School;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redige automatiquement la description d'un sort a partir de ses effets,
 * en francais et en anglais.
 *
 * <p>Une description ecrite a la main dans le {@link SpellBook} reste prioritaire :
 * c'est ce qui permet de garder le texte exact des sorts particuliers (Feinte,
 * Eclair Fracture...).
 */
public final class SpellDescriber {

    private SpellDescriber() {}

    public static String describe(Spell spell, boolean french) {
        String manual = french ? spell.descFr() : spell.descEn();
        if (manual != null && !manual.isBlank()) return manual;
        List<String> parts = new ArrayList<>();
        for (SpellEffect effect : spell.effects()) {
            String text = describeEffect(effect, french);
            if (!text.isBlank()) parts.add(text);
        }
        return String.join(" ", parts);
    }

    public static String describeEffect(SpellEffect effect, boolean french) {
        if (effect instanceof SpellEffect.Damage d) {
            String school = d.school() == null ? (french ? "d'ecole" : "school") : d.school().display(french);
            if (d.perPip()) {
                return french ? ("Inflige " + d.min() + " degats de " + school + " par pip depense.")
                              : ("Deals " + d.min() + " " + school + " damage per pip spent.");
            }
            return french ? ("Inflige " + d.min() + "-" + d.max() + " degats de " + school + ".")
                          : ("Deals " + d.min() + "-" + d.max() + " " + school + " damage.");
        }
        if (effect instanceof SpellEffect.Dot d) {
            String school = d.school() == null ? "" : d.school().display(french) + " ";
            return french ? (d.min() + "-" + d.max() + " degats de " + school + "par round pendant " + d.rounds() + " rounds.")
                          : (d.min() + "-" + d.max() + " " + school + "damage per round for " + d.rounds() + " rounds.");
        }
        if (effect instanceof SpellEffect.Drain d) {
            String school = d.school() == null ? "" : d.school().display(french) + " ";
            int pct = (int) Math.round(d.fraction() * 100);
            return french ? ("Inflige " + d.min() + "-" + d.max() + " degats de " + school + "et vous soigne de " + pct + "% des degats infliges.")
                          : ("Deals " + d.min() + "-" + d.max() + " " + school + "damage and heals you for " + pct + "% of the damage dealt.");
        }
        if (effect instanceof SpellEffect.Heal h) {
            if (h.perPip()) {
                return french ? ("Soigne " + h.min() + " points de vie par pip depense.")
                              : ("Heals " + h.min() + " health per pip spent.");
            }
            return french ? ("Soigne " + h.min() + "-" + h.max() + " points de vie.")
                          : ("Heals " + h.min() + "-" + h.max() + " health.");
        }
        if (effect instanceof SpellEffect.Hot h) {
            return french ? ("Soigne " + h.min() + "-" + h.max() + " points de vie par round pendant " + h.rounds() + " rounds.")
                          : ("Heals " + h.min() + "-" + h.max() + " health per round for " + h.rounds() + " rounds.");
        }
        if (effect instanceof SpellEffect.Absorb a) {
            return french ? ("Absorbe jusqu'a " + a.amount() + " degats.") : ("Absorbs up to " + a.amount() + " damage.");
        }
        if (effect instanceof SpellEffect.Blade b) {
            return french ? ("Lame de +" + b.percent() + "%" + schoolsSuffix(b.schools(), french)
                            + " sur vos " + b.uses() + " prochains sorts offensifs.")
                          : ("Adds a +" + b.percent() + "% blade" + schoolsSuffix(b.schools(), french)
                            + " to your next " + b.uses() + " attack spell(s).");
        }
        if (effect instanceof SpellEffect.Trap t) {
            return french ? ("Piege de +" + t.percent() + "%" + schoolsSuffix(t.schools(), french)
                            + " sur la cible (" + t.uses() + " sort).")
                          : ("Places a +" + t.percent() + "% trap" + schoolsSuffix(t.schools(), french)
                            + " on the target (" + t.uses() + " spell).");
        }
        if (effect instanceof SpellEffect.SelfTrap t) {
            return french ? ("Vous subissez +" + t.percent() + "% de degats sur la prochaine attaque recue.")
                          : ("You take +" + t.percent() + "% damage from the next attack received.");
        }
        if (effect instanceof SpellEffect.Shield s) {
            return french ? ("Reduit de " + s.percent() + "% les prochains degats" + schoolsSuffix(s.schools(), french)
                            + " subis (" + s.uses() + " attaque).")
                          : ("Reduces the next incoming damage" + schoolsSuffix(s.schools(), french)
                            + " by " + s.percent() + "% (" + s.uses() + " attack).");
        }
        if (effect instanceof SpellEffect.Weakness w) {
            return french ? ("Reduit de " + w.percent() + "% les degats infliges par la cible (" + w.uses() + " sort).")
                          : ("Reduces the damage dealt by the target by " + w.percent() + "% (" + w.uses() + " spell).");
        }
        if (effect instanceof SpellEffect.AccuracyMod a) {
            return a.percent() >= 0
                    ? (french ? ("Donne +" + a.percent() + "% de precision (" + a.uses() + " sort).")
                              : ("Gives +" + a.percent() + "% accuracy (" + a.uses() + " spell)."))
                    : (french ? ("Retire " + Math.abs(a.percent()) + "% de precision a la cible (" + a.uses() + " sort).")
                              : ("Removes " + Math.abs(a.percent()) + "% accuracy from the target (" + a.uses() + " spell)."));
        }
        if (effect instanceof SpellEffect.Stun s) {
            return french ? ("Etourdit la cible pendant " + s.rounds() + " round(s).")
                          : ("Stuns the target for " + s.rounds() + " round(s).");
        }
        if (effect instanceof SpellEffect.Summon s) {
            String name = Creatures.get(s.minionId()).name(french);
            return french ? ("Invoque " + name + " pour combattre a vos cotes.")
                          : ("Summons " + name + " to fight by your side.");
        }
        if (effect instanceof SpellEffect.GainPips g) {
            return french ? ("Vous donne " + g.pips() + " pip(s).") : ("Gives you " + g.pips() + " pip(s).");
        }
        if (effect instanceof SpellEffect.DrawCards d) {
            return french ? ("Pioche " + d.count() + " carte(s).") : ("Draws " + d.count() + " card(s).");
        }
        if (effect instanceof SpellEffect.Reshuffle) {
            return french ? "Remelange votre defausse dans votre deck." : "Shuffles your discard pile into your deck.";
        }
        if (effect instanceof SpellEffect.Cleanse c) {
            return switch (c.target()) {
                case HARMFUL_CHARM -> french ? "Retire un malus offensif qui vous affecte." : "Removes a harmful charm from yourself.";
                case DAMAGE_OVER_TIME -> french ? "Retire les degats sur la duree qui affectent la cible." : "Removes damage-over-time effects from the target.";
                case ALL -> french ? "Retire tous les effets nefastes de la cible." : "Removes every harmful effect from the target.";
            };
        }
        if (effect instanceof SpellEffect.StealCharm) {
            return french ? "Vole une lame a la cible." : "Steals a blade from the target.";
        }
        if (effect instanceof SpellEffect.Disarm) {
            return french ? "Retire une lame a la cible." : "Removes a blade from the target.";
        }
        if (effect instanceof SpellEffect.PierceAura p) {
            return french ? ("Ignore " + p.percent() + "% de la resistance de la cible sur " + p.uses() + " sort.")
                          : ("Ignores " + p.percent() + "% of the target resistance on " + p.uses() + " spell(s).");
        }
        if (effect instanceof SpellEffect.HealBlade h) {
            return french ? ("Ajoute +" + h.percent() + "% de soins sur votre prochain sort de soin.")
                          : ("Adds +" + h.percent() + "% healing to your next healing spell.");
        }
        if (effect instanceof SpellEffect.Aura a) {
            String scope = a.schools().isEmpty() ? (french ? "toutes les ecoles" : "all schools")
                    : schoolsSuffix(a.schools(), french);
            return switch (a.type()) {
                case DAMAGE_BOOST -> french ? ("Aura globale : +" + a.percent() + "% de degats (" + scope + ") pendant " + a.rounds() + " rounds.")
                                            : ("Global aura: +" + a.percent() + "% damage (" + scope + ") for " + a.rounds() + " rounds.");
                case HEAL_BOOST -> french ? ("Aura globale : +" + a.percent() + "% de soins pendant " + a.rounds() + " rounds.")
                                          : ("Global aura: +" + a.percent() + "% healing for " + a.rounds() + " rounds.");
                case HEAL_REDUCTION -> french ? ("Aura globale : -" + a.percent() + "% de soins pour tout le monde pendant " + a.rounds() + " rounds.")
                                              : ("Global aura: -" + a.percent() + "% healing for everyone for " + a.rounds() + " rounds.");
                case ACCURACY_BOOST -> french ? ("Aura globale : +" + a.percent() + "% de precision pendant " + a.rounds() + " rounds.")
                                              : ("Global aura: +" + a.percent() + "% accuracy for " + a.rounds() + " rounds.");
            };
        }
        if (effect instanceof SpellEffect.SelfDamage sd) {
            if (sd.percentOfMax() > 0) {
                return french ? ("Vous perdez " + sd.percentOfMax() + "% de vos points de vie maximum.")
                              : ("You lose " + sd.percentOfMax() + "% of your maximum health.");
            }
            return french ? ("Vous perdez " + sd.flat() + " points de vie.") : ("You lose " + sd.flat() + " health.");
        }
        if (effect instanceof SpellEffect.PipBlade pb) {
            School s = pb.school();
            return french ? ("Consomme tous vos pips : +" + pb.percentPerPip() + "% de degats " + s.displayFr() + " par pip depense.")
                          : ("Consumes all your pips: +" + pb.percentPerPip() + "% " + s.displayEn() + " damage per pip spent.");
        }
        if (effect instanceof SpellEffect.RandomSchoolDamage r) {
            return french ? ("Inflige " + r.min() + "-" + r.max() + " degats d'une ecole aleatoire" + schoolsSuffix(r.schools(), french) + ".")
                          : ("Deals " + r.min() + "-" + r.max() + " damage of a random school" + schoolsSuffix(r.schools(), french) + ".");
        }
        if (effect instanceof SpellEffect.OneOf o) {
            List<String> options = new ArrayList<>();
            for (SpellEffect option : o.options()) options.add(describeEffect(option, french));
            return french ? ("Au hasard : " + String.join(" OU ", options)) : ("Randomly: " + String.join(" OR ", options));
        }
        return "";
    }

    private static String schoolsSuffix(Set<School> schools, boolean french) {
        if (schools.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(" (");
        int i = 0;
        for (School s : schools) {
            if (i++ > 0) sb.append(", ");
            sb.append(s.display(french));
        }
        return sb.append(")").toString();
    }
}
