package com.arcanumrealms.client;

import com.arcanumrealms.core.world.ContentLine;
import com.arcanumrealms.core.world.ContentNames;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Affiche les textes du contenu avec la traduction du joueur.
 *
 * <p>Deux mecanismes se completent :
 *
 * <ol>
 *   <li>les lignes qui portent une cle ({@link ContentLine}) affichent la
 *       traduction si elle existe : c'est le cas des dialogues, des noms de PNJ,
 *       des quetes, des objectifs et des recompenses ;</li>
 *   <li>les textes composes par le moteur (journal de combat, noms d'acteurs,
 *       messages d'objectif) sont reecrits nom par nom : chaque nom par defaut
 *       connu ({@link ContentNames}) est remplace par la traduction en cours.</li>
 * </ol>
 *
 * <p>Consequence : renommer un sort, une creature ou un PNJ suffit, sans toucher
 * au code ni recompiler le mod.
 */
public final class ContentResolver {

    /** Duree de validite du cache : un texte modifie apparait en une seconde. */
    private static final long CACHE_MILLIS = 1000L;

    private static Map<String, String> replacements;
    private static long builtAt;

    private ContentResolver() {}

    /** Texte a afficher pour une ligne de contenu. */
    public static String resolve(ContentLine line) {
        String fallback = line.text();
        if (line.translatable()) {
            String shown = Component.translatableWithFallback(line.key(), fallback).getString();
            if (!shown.equals(fallback)) return shown;
        }
        return substitute(fallback);
    }

    /** Composant affichable pour une ligne de contenu. */
    public static Component component(ContentLine line) {
        return Component.literal(resolve(line));
    }

    /** Remplace les noms par defaut par ceux du joueur. */
    public static String substitute(String text) {
        if (text == null || text.isEmpty()) return text;
        Map<String, String> table = table();
        if (table.isEmpty()) return text;

        String out = text;
        for (Map.Entry<String, String> entry : table.entrySet()) {
            if (out.contains(entry.getKey())) {
                out = out.replace(entry.getKey(), entry.getValue());
            }
        }
        return out;
    }

    /** Vrai si le joueur affiche le contenu en francais. */
    public static boolean isFrench() {
        return ClientBattleState.french();
    }

    /**
     * Table de remplacement : nom par defaut du moteur -> texte du joueur.
     *
     * <p>Seuls les noms reellement reecrits y figurent, ce qui evite de parcourir
     * des centaines d'entrees inutiles a chaque ligne de journal. La table est
     * reconstruite quand le joueur change de langue ou recharge ses ressources.
     */
    private static Map<String, String> table() {
        long now = System.currentTimeMillis();
        if (replacements != null && now - builtAt < CACHE_MILLIS) return replacements;

        Map<String, String> built = new LinkedHashMap<>();
        for (String[] source : ContentNames.sources()) {
            String translated = Component.translatableWithFallback(source[1], source[0]).getString();
            if (!translated.equals(source[0])) {
                built.put(source[0], translated);
            }
        }
        replacements = built;
        builtAt = now;
        return replacements;
    }

    /** Invalide le cache : a appeler quand le joueur change de langue ou de pack. */
    public static void invalidate() {
        replacements = null;
    }
}
