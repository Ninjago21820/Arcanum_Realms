package com.arcanumrealms.core.util;

import java.util.List;
import java.util.Random;

/**
 * Generateur aleatoire du moteur de duel.
 *
 * <p>Il est isole du reste pour deux raisons :
 * <ul>
 *   <li>les tests peuvent fournir une graine fixe pour obtenir des resultats deterministes ;</li>
 *   <li>le serveur peut journaliser une graine et rejouer un duel (anti-triche PvP).</li>
 * </ul>
 */
public final class Rng {

    private final Random random;

    public Rng() { this(new Random()); }

    public Rng(long seed) { this(new Random(seed)); }

    private Rng(Random random) { this.random = random; }

    public long seed() { return 0L; } // la graine n'est pas exposee par Random : journalisee par l'appelant

    /** Entier entre min et max inclus. */
    public int between(int min, int max) {
        if (max <= min) return min;
        return min + random.nextInt(max - min + 1);
    }

    /** Jet de pourcentage : vrai avec la probabilite {@code percent}%. */
    public boolean chance(int percent) {
        if (percent <= 0) return false;
        if (percent >= 100) return true;
        return random.nextInt(100) < percent;
    }

    /** Jet de pourcentage en virgule flottante (0..1). */
    public boolean chance(double probability) {
        return random.nextDouble() < probability;
    }

    public <T> T pick(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    public <T> void shuffle(List<T> list) {
        java.util.Collections.shuffle(list, random);
    }

    public double nextDouble() { return random.nextDouble(); }
}
