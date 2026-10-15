package com.arcanumrealms.core.world;

import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Correspondance entre les noms par defaut du moteur et leurs cles de traduction.
 *
 * <p>Le moteur compose certains textes lui-meme (journal de combat, noms d'acteurs,
 * messages de quete) : ils contiennent donc des noms ecrits par le mod, pas par le
 * joueur. Cette classe fournit la table permettant au client de retrouver, pour
 * chaque nom connu, la cle a interroger — ce qui rend un sort ou un PNJ renomme
 * visible jusque dans le journal de combat.
 *
 * <p>Les <b>deux langues</b> sont couvertes : le journal melange les deux (le texte
 * des evenements suit la langue de l'affichage, mais les noms de sorts envoyes par
 * le moteur sont toujours ceux de la version anglaise). Un joueur qui renomme un
 * sort le voit donc renomme partout, quelle que soit la langue de l'interface.
 */
public final class ContentNames {

    private static List<String[]> sources;

    private ContentNames() {}

    /**
     * Paires {@code {nom par defaut, cle de traduction}}, les noms les plus longs
     * d'abord (pour ne pas couper « Chasseur Elfe de Braise » en remplacant
     * « Braise » d'abord).
     */
    public static synchronized List<String[]> sources() {
        if (sources != null) return sources;

        List<String[]> entries = new ArrayList<>();
        for (Spell spell : SpellBook.all()) {
            add(entries, spell.name(false), spell.name(true), ContentKeys.spell(spell.id()));
        }
        for (WorldCreature creature : Bestiary.all()) {
            add(entries, creature.name(false), creature.name(true), ContentKeys.creature(creature.id()));
        }
        for (NpcDef npc : Npcs.all()) {
            add(entries, npc.name(false), npc.name(true), ContentKeys.npc(npc.id()));
            add(entries, npc.title(false), npc.title(true), ContentKeys.npcTitle(npc.id()));
        }
        for (GearDef gear : Gears.all()) {
            add(entries, gear.name(false), gear.name(true), ContentKeys.gear(gear.id()));
        }
        for (ZoneDef zone : Zones.all()) {
            add(entries, zone.name(false), zone.name(true), ContentKeys.zone(zone.id()));
        }
        for (School school : School.values()) {
            add(entries, school.display(false), school.display(true), school.langKey());
        }

        entries.sort(Comparator.comparingInt((String[] entry) -> entry[0].length()).reversed());
        sources = entries;
        return sources;
    }

    /** Cle de traduction d'un nom par defaut, ou {@code null} si ce nom est inconnu. */
    public static String keyOf(String defaultName) {
        if (defaultName == null || defaultName.isBlank()) return null;
        for (String[] entry : sources()) {
            if (entry[0].equals(defaultName)) return entry[1];
        }
        return null;
    }

    private static void add(List<String[]> entries, String english, String french, String key) {
        put(entries, english, key);
        put(entries, french, key);
    }

    private static void put(List<String[]> entries, String name, String key) {
        if (name != null && !name.isBlank()) {
            entries.add(new String[]{name, key});
        }
    }
}
