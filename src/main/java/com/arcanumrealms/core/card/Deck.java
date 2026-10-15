package com.arcanumrealms.core.card;

import com.arcanumrealms.core.util.Rng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deck de duel, fidele aux regles de Wizard101 :
 *
 * <ul>
 *   <li>On commence chaque round avec <b>7 cartes</b> en main (si le deck le permet).</li>
 *   <li>Les cartes non utilisees restent en main d'un round a l'autre.</li>
 *   <li>On peut defausser n'importe quelle carte (sauf celles piochees pendant le round en cours).</li>
 *   <li>Une carte utilisee ou defaussee part a la defausse et ne revient pas
 *       de la partie (sauf sort Rebattre).</li>
 *   <li>Quand la pioche est vide, la defausse est automatiquement remelangee dedans.</li>
 * </ul>
 */
public final class Deck {

    /** Taille de la main. */
    public static final int HAND_SIZE = 7;

    /** Une carte en main : elle connait le round ou elle a ete piochee. */
    public static final class Card {
        private final String spellId;
        private final int drawnRound;

        public Card(String spellId, int drawnRound) {
            this.spellId = spellId;
            this.drawnRound = drawnRound;
        }

        public String spellId() { return spellId; }
        public int drawnRound() { return drawnRound; }
    }

    private final List<String> drawPile = new ArrayList<>();
    private final List<String> discardPile = new ArrayList<>();
    private final List<Card> hand = new ArrayList<>();
    private final int handSize;

    public Deck(List<String> cards, int handSize, Rng rng) {
        this.handSize = handSize;
        this.drawPile.addAll(cards);
        rng.shuffle(drawPile);
    }

    public List<Card> hand() { return Collections.unmodifiableList(hand); }

    public int drawPileSize() { return drawPile.size(); }
    public int discardPileSize() { return discardPile.size(); }
    public int handSize() { return handSize; }
    public int totalCards() { return drawPile.size() + discardPile.size() + hand.size(); }

    /** Pioche jusqu'a remplir la main (debut de phase de planification). */
    public void refillHand(int currentRound) {
        while (hand.size() < handSize) {
            if (drawPile.isEmpty()) {
                if (discardPile.isEmpty()) return;
                recycleDiscard();
            }
            hand.add(new Card(drawPile.remove(0), currentRound));
        }
    }

    /**
     * Defausse une carte de la main.
     * Comme dans Wizard101, une carte piochee pendant le round en cours ne peut pas etre defaussee.
     */
    public boolean discard(int handIndex, int currentRound) {
        if (handIndex < 0 || handIndex >= hand.size()) return false;
        Card card = hand.get(handIndex);
        if (card.drawnRound() == currentRound) return false;
        hand.remove(handIndex);
        discardPile.add(card.spellId());
        return true;
    }

    /** Retire une carte de la main pour la jouer ; elle part a la defausse. */
    public String play(int handIndex) {
        if (handIndex < 0 || handIndex >= hand.size()) return null;
        String id = hand.remove(handIndex).spellId();
        discardPile.add(id);
        return id;
    }

    public void addToHand(String spellId, int round) {
        hand.add(new Card(spellId, round));
    }

    public void addToDiscard(String spellId) {
        discardPile.add(spellId);
    }

    /** Sort "Rebattre" : la defausse retourne dans la pioche, puis on repioche une main complete. */
    public void reshuffleAll(int currentRound) {
        drawPile.addAll(discardPile);
        discardPile.clear();
        for (Card c : hand) discardPile.add(c.spellId());
        hand.clear();
        recycleDiscard();
        refillHand(currentRound);
    }

    /** Pioche des cartes supplementaires (sort "Rebattre", effets de pioche). */
    public int drawExtra(int count, int currentRound) {
        int drawn = 0;
        while (drawn < count && hand.size() < handSize) {
            if (drawPile.isEmpty()) {
                if (discardPile.isEmpty()) break;
                recycleDiscard();
            }
            hand.add(new Card(drawPile.remove(0), currentRound));
            drawn++;
        }
        return drawn;
    }

    private void recycleDiscard() {
        drawPile.addAll(discardPile);
        discardPile.clear();
    }

    /** Melange la pioche (debut de duel). */
    public void shuffle(Rng rng) {
        rng.shuffle(drawPile);
    }
}
