package com.arcanumrealms.core;

import com.arcanumrealms.core.minion.Creatures;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;

import java.util.ArrayList;
import java.util.List;

/**
 * Point d'entree du moteur de jeu.
 *
 * <p>Ce package ne depend d'aucune classe Minecraft : il peut etre compile et
 * teste tout seul (voir {@code tools/run-core-tests.sh}). C'est ce qui permet de
 * garantir que les regles du duel fonctionnent avant meme de lancer le jeu.
 */
public final class ArcanumCore {

    public static final String MOD_ID = "arcanum_realms";
    public static final String VERSION = "0.1.0";

    private ArcanumCore() {}

    /** Charge et verifie les donnees du jeu. Renvoie la liste des problemes detectes. */
    public static List<String> init() {
        List<String> problems = new ArrayList<>();
        for (Spell spell : SpellBook.all()) {
            if (spell.effects().isEmpty()) problems.add("Sort sans effet : " + spell.id());
            if (spell.pipCost() >= 0 && spell.pipCost() > 7) problems.add("Sort trop cher (>7 pips) : " + spell.id());
            if (spell.accuracy() <= 0 || spell.accuracy() > 100) problems.add("Precision invalide : " + spell.id());
            if (spell.kind() != com.arcanumrealms.core.spell.SpellKind.WAND && spell.minLevel() < 1) {
                problems.add("Niveau minimum invalide : " + spell.id());
            }
        }
        for (School school : School.values()) {
            boolean hasOnePip = SpellBook.bySchool(school).stream().anyMatch(s -> s.pipCost() == 1);
            if (!hasOnePip) problems.add("L'ecole " + school.id() + " n'a aucun sort a 1 pip");
        }
        Creatures.all().forEach(def -> def.deck().forEach(id ->
                SpellBook.find(id).orElseGet(() -> {
                    problems.add("Deck de " + def.id() + " : sort inconnu " + id);
                    return null;
                })));
        return problems;
    }
}
