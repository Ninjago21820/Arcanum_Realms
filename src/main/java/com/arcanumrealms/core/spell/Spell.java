package com.arcanumrealms.core.spell;

import com.arcanumrealms.core.school.School;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Une carte de sort. Les valeurs sont immuables et partagees par toutes les parties.
 *
 * <p>Fidelite Wizard101 : le cout en pips, la precision et les effets sont ceux du sort
 * d'origine (voir docs/02-sorts.md pour la table de correspondance et les sources).
 */
public final class Spell {

    private final String id;
    private final School school;
    private final int pipCost;
    private final int minPips;
    private final int maxPips;
    private final int accuracy;
    private final SpellKind kind;
    private final TargetMode targetMode;
    private final List<SpellEffect> effects;
    private final int minLevel;
    private final int maxCopies;
    private final boolean trainableByAll;
    private final boolean noPvp;
    private final String nameEn;
    private final String nameFr;
    private final String descEn;
    private final String descFr;

    Spell(String id, School school, int pipCost, int minPips, int maxPips, int accuracy, SpellKind kind,
          TargetMode targetMode, List<SpellEffect> effects, int minLevel, int maxCopies,
          boolean trainableByAll, boolean noPvp, String nameEn, String nameFr, String descEn, String descFr) {
        this.id = Objects.requireNonNull(id);
        this.school = Objects.requireNonNull(school);
        this.pipCost = pipCost;
        this.minPips = minPips;
        this.maxPips = maxPips;
        this.accuracy = accuracy;
        this.kind = Objects.requireNonNull(kind);
        this.targetMode = Objects.requireNonNull(targetMode);
        this.effects = List.copyOf(effects);
        this.minLevel = minLevel;
        this.maxCopies = maxCopies;
        this.trainableByAll = trainableByAll;
        this.noPvp = noPvp;
        this.nameEn = nameEn;
        this.nameFr = nameFr;
        this.descEn = descEn;
        this.descFr = descFr;
    }

    public String id() { return id; }
    public School school() { return school; }
    /** Cout en pips. {@code -1} pour les sorts a pips variables (Tempete, Jugement). */
    public int pipCost() { return pipCost; }
    public boolean variablePips() { return pipCost < 0; }
    public int minPips() { return variablePips() ? minPips : pipCost; }
    public int maxPips() { return variablePips() ? maxPips : pipCost; }
    public int accuracy() { return accuracy; }
    public SpellKind kind() { return kind; }
    public TargetMode targetMode() { return targetMode; }
    public List<SpellEffect> effects() { return effects; }
    public int minLevel() { return minLevel; }
    /** Nombre maximum d'exemplaires de cette carte dans un deck. */
    public int maxCopies() { return maxCopies; }
    public boolean trainableByAll() { return trainableByAll; }
    public boolean disabledInPvp() { return noPvp; }
    public String nameEn() { return nameEn; }
    public String nameFr() { return nameFr; }
    public String descEn() { return descEn; }
    public String descFr() { return descFr; }

    public String name(boolean french) { return french ? nameFr : nameEn; }
    public String description(boolean french) { return french ? descFr : descEn; }

    /** Cle de langue du nom, utilisee par le client pour l'affichage. */
    public String nameKey() { return "spell.arcanum_realms." + id + ".name"; }
    public String descKey() { return "spell.arcanum_realms." + id + ".desc"; }

    public boolean affectsEnemies() { return targetMode.affectsEnemies(); }
    public boolean isAttack() {
        return switch (kind) {
            case WAND, DAMAGE, DAMAGE_ALL, DRAIN, DAMAGE_OVER_TIME -> true;
            default -> false;
        };
    }

    /** Toutes les ecoles ciblees par les effets de ce sort (pour l'IA et les prismes). */
    public Set<School> damageSchools() {
        Set<School> out = new java.util.LinkedHashSet<>();
        for (SpellEffect e : effects) {
            if (e instanceof SpellEffect.Damage d && d.school() != null) out.add(d.school());
            else if (e instanceof SpellEffect.Dot d && d.school() != null) out.add(d.school());
            else if (e instanceof SpellEffect.Drain d && d.school() != null) out.add(d.school());
        }
        return out;
    }

    /** Vrai si le sort inflige des degats dans l'ecole du lanceur. */
    public boolean isOwnSchoolAttack(School caster) {
        Set<School> s = damageSchools();
        return s.isEmpty() || s.contains(caster);
    }

    @Override
    public String toString() {
        return "Spell[" + id + " " + (pipCost < 0 ? ("X+" + minPips) : pipCost) + "pips " + accuracy + "%]";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Spell s && s.id.equals(id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
