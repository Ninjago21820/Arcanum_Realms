package com.arcanumrealms.core.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Suite de repliques affichees quand on parle a un PNJ. */
public final class Dialogue {

    public static final Dialogue EMPTY = new Dialogue(List.of());

    private final List<DialogueLine> lines;

    public Dialogue(List<DialogueLine> lines) {
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    /** Construit un dialogue a partir de paires (francais, anglais). */
    public static Dialogue of(String... frEn) {
        if (frEn.length % 2 != 0) throw new IllegalArgumentException("Il faut des paires fr/en");
        List<DialogueLine> out = new ArrayList<>();
        for (int i = 0; i < frEn.length; i += 2) out.add(new DialogueLine(frEn[i], frEn[i + 1]));
        return new Dialogue(out);
    }

    public List<DialogueLine> lines() {
        return lines;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public int size() {
        return lines.size();
    }

    public List<String> texts(boolean french) {
        List<String> out = new ArrayList<>(lines.size());
        for (DialogueLine line : lines) out.add(line.text(french));
        return out;
    }

    @Override
    public String toString() {
        return lines.isEmpty() ? "(aucun dialogue)" : lines.get(0).fr();
    }
}
