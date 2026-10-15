package com.arcanumrealms.core.world;

/**
 * Une ligne de texte affichee au joueur, avec sa cle de traduction.
 *
 * <p>Le moteur ne connait pas la langue du joueur : il voyage donc avec le
 * texte par defaut, et le client remplace ce texte par celui de la cle si le
 * joueur (ou un pack de ressources) en fournit une. C'est ce qui permet de
 * reecrire dialogues, noms de PNJ et noms de sorts sans recompiler le mod.
 *
 * @param key  cle de traduction, ou {@code null} si la ligne n'est pas traduisible
 * @param text texte par defaut, utilise si la cle n'est pas definie
 */
public record ContentLine(String key, String text) {

    public static ContentLine literal(String text) {
        return new ContentLine(null, text == null ? "" : text);
    }

    public static ContentLine of(String key, String text) {
        return new ContentLine(key, text == null ? "" : text);
    }

    public static ContentLine empty() {
        return new ContentLine(null, "");
    }

    public boolean translatable() {
        return key != null && !key.isBlank();
    }

    public boolean isBlank() {
        return text.isBlank() && !translatable();
    }

    @Override
    public String toString() {
        return translatable() ? key + "=" + text : text;
    }
}
