package com.arcanumrealms.core.world;

/**
 * Une replique de dialogue.
 *
 * <p>Les textes sont ecrits a la main (pas de copie du jeu d'origine) : le wiki
 * sert de reference pour savoir <em>qui</em> dit <em>quoi</em>, a quel moment et
 * pour quelle quete.
 */
public record DialogueLine(String fr, String en) {

    public String text(boolean french) {
        return french ? fr : en;
    }

    public static DialogueLine of(String fr, String en) {
        return new DialogueLine(fr, en);
    }
}
