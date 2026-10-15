package com.arcanumrealms.util;

import net.minecraft.locale.Language;

/**
 * Langue courante.
 *
 * <p>Utilise la classe vanilla {@link Language}, disponible des deux cotes :
 * sur un serveur dedie la langue est l'anglais, sur un client c'est celle du
 * joueur. Cela evite de referencer une classe cliente depuis du code partage.
 */
public final class Language {

    private Language() {}

    /** Vrai si la langue courante est le francais. */
    public static boolean isFrench() {
        try {
            String code = Language.getInstance().getLanguage();
            return code != null && code.toLowerCase(java.util.Locale.ROOT).startsWith("fr");
        } catch (Throwable ignored) {
            return false;
        }
    }
}
