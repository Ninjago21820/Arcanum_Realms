package com.arcanumrealms.core.world;

/**
 * Coiffe portee par un personnage.
 *
 * <p>La forme est decidee ici, dans le moteur, et lue par les deux cotes qui en
 * ont besoin : le rendu 3D du mod (qui choisit le modele de chapeau) et le
 * generateur d'apparences en Python (qui dessine la coiffe plate sous le
 * modele). Il n'y a donc qu'un seul endroit ou changer la coiffe d'un PNJ.
 *
 * <p>Les valeurs sont ordonnees comme les formes du modele 3D de
 * {@code client/model/WizardRobeModel}.
 */
public enum Attire {

    /** Tete nue (cheveux visibles). */
    NONE,
    /** Chapeau pointu de sorcier. */
    POINTED,
    /** Capuche : le visage reste degage. */
    HOOD,
    /** Casque de garde, avec nasal et protege-joues. */
    HELM,
    /** Petit chapeau souple, bord rabattu. */
    CAP,
    /** Chapeau de paille a large bord. */
    STRAW;

    /** Vrai si cette coiffe est posee par le modele 3D (et non par la peau). */
    public boolean isModelled() {
        return this != NONE;
    }
}
