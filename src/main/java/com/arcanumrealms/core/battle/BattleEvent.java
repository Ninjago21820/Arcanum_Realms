package com.arcanumrealms.core.battle;

import com.arcanumrealms.core.school.School;

import java.util.List;

/**
 * Tout ce qui se passe dans un duel, sous forme d'evenements.
 *
 * <p>Le serveur envoie ces evenements au client (journal de combat, animations,
 * messages dans le chat) et peut aussi les journaliser pour le debogage.
 */
public sealed interface BattleEvent {

    /** Texte affiche au joueur. */
    String text(boolean french);

    record Start(int wizards, int creatures) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? ("Le duel commence : " + wizards + " sorcier(s) contre " + creatures + " creature(s).")
                      : ("The duel begins: " + wizards + " wizard(s) against " + creatures + " creature(s).");
        }
    }

    record RoundStart(int round) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? ("--- Round " + round + " ---") : ("--- Round " + round + " ---");
        }
    }

    record Planning(int round) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? "Choisissez votre carte." : "Choose your card.";
        }
    }

    record Cast(String casterName, String spellId, String spellName, List<String> targetNames, int pips, School school,
                boolean critical) implements BattleEvent {
        @Override public String text(boolean fr) {
            String targets = targetNames.isEmpty() ? "" : " -> " + String.join(", ", targetNames);
            String crit = critical ? (fr ? " (COUP CRITIQUE !)" : " (CRITICAL!)") : "";
            return casterName + (fr ? " lance " : " casts ") + spellName + " (" + pips + (fr ? " pips)" : " pips)") + targets + crit;
        }
    }

    record Fizzle(String casterName, String spellId, String spellName) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? (casterName + " rate " + spellName + " : le sort echoue !")
                      : (casterName + " fizzles " + spellName + "!");
        }
    }

    record Damage(String casterName, String targetId, String targetName, int amount, School school,
                  boolean critical, int resisted, int absorbed) implements BattleEvent {
        @Override public String text(boolean fr) {
            StringBuilder sb = new StringBuilder();
            sb.append(fr ? (casterName + " inflige " + amount + " degats " + school.displayFr() + " a " + targetName)
                         : (casterName + " deals " + amount + " " + school.displayEn() + " damage to " + targetName));
            if (critical) sb.append(fr ? " (critique)" : " (critical)");
            if (resisted > 0) sb.append(fr ? (" [" + resisted + " resiste]") : (" [" + resisted + " resisted]"));
            if (absorbed > 0) sb.append(fr ? (" [" + absorbed + " absorbe]") : (" [" + absorbed + " absorbed]"));
            return sb.toString();
        }
    }

    record Heal(String casterName, String targetName, int amount, boolean critical) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? (casterName + " soigne " + targetName + " de " + amount + " points de vie" + (critical ? " (critique)" : ""))
                      : (casterName + " heals " + targetName + " for " + amount + (critical ? " (critical)" : ""));
        }
    }

    record StatusApplied(String targetId, String targetName, String description) implements BattleEvent {
        @Override public String text(boolean fr) {
            return targetName + " : " + description;
        }
    }

    record StatusExpired(String targetId, String targetName, String description) implements BattleEvent {
        @Override public String text(boolean fr) {
            return (fr ? "Fin de l'effet sur " : "Effect expired on ") + targetName + " : " + description;
        }
    }

    record Defeated(String targetId, String targetName, boolean player) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? (targetName + " est vaincu !") : (targetName + " is defeated!");
        }
    }

    record Summoned(String summonerName, String minionName, String minionId) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? (summonerName + " invoque " + minionName + " !") : (summonerName + " summons " + minionName + "!");
        }
    }

    record Passed(String casterName) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? (casterName + " passe son tour.") : (casterName + " passes.");
        }
    }

    record Fled(String casterName) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? (casterName + " quitte le duel.") : (casterName + " flees the duel.");
        }
    }

    record Discarded(String casterName, String spellId, String spellName) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? (casterName + " defausse " + spellName + ".") : (casterName + " discards " + spellName + ".");
        }
    }

    record Stunned(String casterName) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? (casterName + " est etourdi et passe son tour !") : (casterName + " is stunned and skips the turn!");
        }
    }

    record AuraChanged(String description) implements BattleEvent {
        @Override public String text(boolean fr) { return description; }
    }

    record RoundEnd(int round) implements BattleEvent {
        @Override public String text(boolean fr) {
            return fr ? ("--- Fin du round " + round + " ---") : ("--- End of round " + round + " ---");
        }
    }

    record Ended(boolean playersWon, boolean draw) implements BattleEvent {
        @Override public String text(boolean fr) {
            if (draw) return fr ? "Match nul : le duel s'arrete." : "Draw: the duel stops.";
            return playersWon ? (fr ? "Victoire ! Les sorciers remportent le duel." : "Victory! The wizards win the duel.")
                              : (fr ? "Defaite... les creatures l'emportent." : "Defeat... the creatures win.");
        }
    }

    record Info(String fr, String en) implements BattleEvent {
        @Override public String text(boolean french) { return french ? fr : en; }
    }
}
