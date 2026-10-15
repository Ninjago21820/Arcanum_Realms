package com.arcanumrealms.core.battle;

import com.arcanumrealms.core.school.School;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Les pips d'un sorcier, avec exactement les regles de Wizard101 :
 *
 * <ul>
 *   <li>On gagne 1 emplacement de pip au debut de chaque round.</li>
 *   <li>Cet emplacement est un <b>pip de pouvoir</b> avec une certaine probabilite (equipement).</li>
 *   <li>Un pip de pouvoir compte pour <b>2 pips pour les sorts de son ecole</b> seulement,
 *       et pour 1 pip pour toutes les autres ecoles (sauf maitrise, ex. amulette).</li>
 *   <li>Un pip ne se coupe pas : payer un sort de 3 pips avec 1 pip de pouvoir et 1 pip normal
 *       consomme 2 emplacements et gaspille 1 pip.</li>
 *   <li>Maximum 7 emplacements (donc jusqu'a 14 pips).</li>
 * </ul>
 */
public final class CombatantPips {

    /** Nombre maximum d'emplacements de pips (comme dans Wizard101). */
    public static final int MAX_SLOTS = 7;

    /** {@code true} = pip de pouvoir. */
    private final List<Boolean> slots = new ArrayList<>();

    public int slotCount() { return slots.size(); }

    public List<Boolean> view() { return Collections.unmodifiableList(slots); }

    public void clear() { slots.clear(); }

    /** Ajoute un pip. Renvoie {@code false} si la barre de pips est deja pleine. */
    public boolean add(boolean power) {
        if (slots.size() >= MAX_SLOTS) return false;
        slots.add(power);
        return true;
    }

    public void add(int count, boolean power) {
        for (int i = 0; i < count; i++) if (!add(power)) return;
    }

    public int powerPips() { return (int) slots.stream().filter(b -> b).count(); }
    public int normalPips() { return slots.size() - powerPips(); }

    /** Valeur totale des pips pour un sort : les pips de pouvoir comptent double ou non. */
    public int totalValue(boolean powerCountsDouble) {
        return powerCountsDouble ? normalPips() + 2 * powerPips() : slots.size();
    }

    /** Vrai si le cout peut etre paye. */
    public boolean canPay(int cost, boolean powerCountsDouble) {
        if (cost <= 0) return true;
        return findPayment(cost, powerCountsDouble) != null;
    }

    /**
     * Paie un sort et renvoie le nombre de pips gaspilles (pips de pouvoir non fractionnables).
     *
     * @return le gaspillage, ou {@code -1} si le paiement est impossible
     */
    public int pay(int cost, boolean powerCountsDouble) {
        if (cost <= 0) return 0;
        int[] payment = findPayment(cost, powerCountsDouble);
        if (payment == null) return -1;
        int power = payment[0], normal = payment[1];
        int waste = payment[2];
        for (int i = slots.size() - 1; i >= 0 && power > 0; i--) {
            if (slots.get(i)) { slots.remove(i); power--; }
        }
        for (int i = slots.size() - 1; i >= 0 && normal > 0; i--) {
            if (!slots.get(i)) { slots.remove(i); normal--; }
        }
        return waste;
    }

    /**
     * Cherche la combinaison de pips qui gaspille le moins possible.
     * En cas d'egalite, on prefere consommer des pips normaux (comportement de Wizard101).
     */
    private int[] findPayment(int cost, boolean powerCountsDouble) {
        int pips = powerPips();
        int normals = normalPips();
        if (!powerCountsDouble) {
            if (slots.size() < cost) return null;
            int usePower = Math.max(0, cost - normals);
            return new int[]{usePower, cost - usePower, 0};
        }
        int bestPower = -1, bestNormal = 0, bestWaste = Integer.MAX_VALUE;
        for (int p = 0; p <= pips; p++) {
            int remaining = cost - 2 * p;
            int n = Math.max(0, remaining);
            if (n > normals) continue;
            int waste = (2 * p + n) - cost;
            if (waste < 0) continue;
            if (waste < bestWaste || (waste == bestWaste && n > bestNormal)) {
                bestPower = p;
                bestNormal = n;
                bestWaste = waste;
            }
        }
        if (bestPower < 0) return null;
        return new int[]{bestPower, bestNormal, bestWaste};
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Boolean b : slots) sb.append(b ? "P" : "o");
        return sb.toString();
    }
}
