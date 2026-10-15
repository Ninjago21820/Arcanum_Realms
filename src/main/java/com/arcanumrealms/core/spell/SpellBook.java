package com.arcanumrealms.core.spell;

import com.arcanumrealms.core.school.School;

import java.util.*;
import java.util.stream.Collectors;

import static com.arcanumrealms.core.school.School.*;
import static com.arcanumrealms.core.spell.SpellEffect.*;
import static com.arcanumrealms.core.spell.SpellKind.*;
import static com.arcanumrealms.core.spell.TargetMode.*;

/**
 * Registre de toutes les cartes du mod.
 *
 * <p>Chaque sort reprend le cout en pips, la precision et les effets du sort de
 * Wizard101 dont il s'inspire. Les valeurs suivent la table officielle de degats
 * par pip (DPP) de chaque ecole :
 * <ul>
 *   <li>Givre / Sylve / Necromancie / Equilibre : 85 DPP</li>
 *   <li>Runomancie : 90 DPP</li>
 *   <li>Pyromancie : 100 DPP</li>
 *   <li>Tempete : 125 DPP (mais 70% de precision)</li>
 * </ul>
 * Les sorts de zone valent environ 78% des degats monocible, et les sorts a
 * degats sur plusieurs tours etalent leur total sur 3 a 4 rounds.
 *
 * <p>Voir docs/02-sorts.md pour la table complete et les sources.
 */
public final class SpellBook {

    private static final Map<String, Spell> SPELLS = new LinkedHashMap<>();
    private static final Map<School, List<Spell>> BY_SCHOOL = new EnumMap<>(School.class);
    private static final List<Spell> UNIVERSAL = new ArrayList<>();

    private SpellBook() {}

    // ------------------------------------------------------------------ API

    public static Spell get(String id) {
        Spell s = SPELLS.get(id);
        if (s == null) throw new IllegalArgumentException("Sort inconnu: " + id);
        return s;
    }

    public static Optional<Spell> find(String id) {
        return Optional.ofNullable(SPELLS.get(id));
    }

    public static Collection<Spell> all() { return Collections.unmodifiableCollection(SPELLS.values()); }

    public static List<Spell> bySchool(School school) {
        return Collections.unmodifiableList(BY_SCHOOL.getOrDefault(school, List.of()));
    }

    /** Sorts accessibles a tous, quelle que soit l'ecole du sorcier. */
    public static List<Spell> universal() { return Collections.unmodifiableList(UNIVERSAL); }

    /** Sorts qu'un sorcier peut entrainer : son ecole, plus les sorts universels. */
    public static List<Spell> trainable(School school, int level) {
        List<Spell> out = new ArrayList<>();
        for (Spell s : SPELLS.values()) {
            if (s.minLevel() > level) continue;
            if (s.school() == school || s.trainableByAll()) out.add(s);
        }
        out.sort(Comparator.comparingInt(Spell::minLevel).thenComparing(Spell::id));
        return out;
    }

    /** Sorts de depart : ce qu'un sorcier possede en arrivant a l'ecole. */
    public static List<Spell> starterDeck(School school) {
        List<Spell> out = new ArrayList<>();
        for (Spell s : SPELLS.values()) {
            if (s.school() == school && s.minLevel() <= 1) out.add(s);
            if (s.kind() == SpellKind.WAND && s.school() == school) out.add(s);
        }
        out.sort(Comparator.comparing(Spell::id));
        return out;
    }

    public static int size() { return SPELLS.size(); }

    // -------------------------------------------------------------- Builder

    private static Builder spell(String id) { return new Builder(id); }

    private static final class Builder {
        private final String id;
        private School school = EQUILIBRIUM;
        private int pips = 0, minPips = 0, maxPips = 0;
        private int accuracy = 100;
        private SpellKind kind = UTILITY;
        private TargetMode target = SELF;
        private int level = 1;
        private int maxCopies = 4;
        private boolean trainableByAll = false;
        private boolean noPvp = false;
        private String nameEn, nameFr, descEn, descFr;
        private final List<SpellEffect> effects = new ArrayList<>();

        Builder(String id) { this.id = id; }

        Builder school(School s) { this.school = s; return this; }
        Builder pips(int p, int min, int max) { this.pips = p; this.minPips = min; this.maxPips = max; return this; }
        Builder acc(int a) { this.accuracy = a; return this; }
        Builder kind(SpellKind k) { this.kind = k; return this; }
        Builder target(TargetMode t) { this.target = t; return this; }
        Builder level(int l) { this.level = l; return this; }
        Builder copies(int c) { this.maxCopies = c; return this; }
        Builder all() { this.trainableByAll = true; return this; }
        Builder noPvp() { this.noPvp = true; return this; }
        Builder en(String name) { this.nameEn = name; return this; }
        Builder fr(String name) { this.nameFr = name; return this; }
        Builder desc(String en, String fr) { this.descEn = en; this.descFr = fr; return this; }
        Builder fx(SpellEffect... e) { this.effects.addAll(Arrays.asList(e)); return this; }

        void add() {
            Spell s = new Spell(id, school, pips, minPips, maxPips, accuracy, kind, target,
                    effects, level, maxCopies, trainableByAll, noPvp,
                    nameEn != null ? nameEn : id, nameFr != null ? nameFr : id, descEn, descFr);
            if (SPELLS.put(id, s) != null) throw new IllegalStateException("Sort en double: " + id);
        }
    }

    private static Set<School> set(School... schools) {
        Set<School> s = new LinkedHashSet<>(Arrays.asList(schools));
        return Collections.unmodifiableSet(s);
    }

    // ------------------------------------------------------- Definitions

    static {
        pyromancy();
        tempest();
        frost();
        runomancy();
        sylvan();
        necromancy();
        equilibrium();
        universalSpells();

        // Index par ecole (uniquement les sorts d'ecole, hors baguettes universelles)
        for (Spell s : SPELLS.values()) {
            if (s.kind() == SpellKind.WAND) { UNIVERSAL.add(s); continue; }
            if (s.trainableByAll()) UNIVERSAL.add(s);
            BY_SCHOOL.computeIfAbsent(s.school(), k -> new ArrayList<>()).add(s);
        }
        BY_SCHOOL.values().forEach(list -> list.sort(Comparator.comparing(Spell::id)));
        UNIVERSAL.sort(Comparator.comparing(Spell::id));
    }

    /** PYROMANCIE - degats eleves et degats sur la duree, precision 75%. */
    private static void pyromancy() {
        spell("ember_kitten").school(PYROMANCY).pips(1, 0, 0).acc(75).kind(DAMAGE).target(SINGLE).level(1).copies(5)
                .en("Ember Kitten").fr("Chaton de Braise")
                .fx(new Damage(80, 120, PYROMANCY))
                .add();

        spell("cinder_wisp").school(PYROMANCY).pips(2, 0, 0).acc(75).kind(DAMAGE_OVER_TIME).target(SINGLE).level(2).copies(4)
                .en("Cinder Wisp").fr("Feu Follet de Braise")
                .fx(new Damage(100, 100, PYROMANCY), new Dot(70, 70, PYROMANCY, 3))
                .add();

        spell("pyre_mark").school(PYROMANCY).pips(0, 0, 0).acc(100).kind(TRAP).target(SINGLE).level(3)
                .en("Pyre Mark").fr("Marque du Bucher")
                .fx(new Trap(30, set(PYROMANCY), 1))
                .add();

        spell("emberedge").school(PYROMANCY).pips(0, 0, 0).acc(100).kind(BLADE).target(SELF).level(4)
                .en("Emberedge").fr("Tranchant de Braise")
                .fx(new Blade(35, set(PYROMANCY), 1))
                .add();

        spell("sunhawk").school(PYROMANCY).pips(3, 0, 0).acc(75).kind(DAMAGE).target(SINGLE).level(6).copies(4)
                .en("Sunhawk").fr("Faucon Solaire")
                .fx(new Damage(280, 340, PYROMANCY))
                .add();

        spell("glacial_ward").school(PYROMANCY).pips(0, 0, 0).acc(100).kind(SHIELD).target(SELF).level(8)
                .en("Glacial Ward").fr("Garde Glaciaire")
                .fx(new Shield(70, set(FROST, TEMPEST), 1))
                .add();

        spell("cinderling").school(PYROMANCY).pips(3, 0, 0).acc(100).kind(SUMMON).target(SELF).level(10)
                .en("Summon Cinderling").fr("Invoquer un Braisillon")
                .fx(new Summon("cinderling"))
                .add();

        spell("ashfall").school(PYROMANCY).pips(4, 0, 0).acc(75).kind(DAMAGE_ALL).target(ALL_ENEMIES).level(12).copies(3)
                .en("Ashfall").fr("Pluie de Cendres")
                .fx(new Damage(265, 345, PYROMANCY))
                .add();

        spell("ashen_veil").school(PYROMANCY).pips(1, 0, 0).acc(100).kind(ACCURACY_DEBUFF).target(ALL_ENEMIES).level(14)
                .en("Ashen Veil").fr("Voile de Cendres")
                .fx(new AccuracyMod(-40, 1))
                .add();

        spell("smothering_haze").school(PYROMANCY).pips(2, 0, 0).acc(100).kind(STUN).target(ALL_ENEMIES).level(16)
                .en("Smothering Haze").fr("Brume Etouffante")
                .fx(new Stun(1))
                .add();

        spell("drakeling").school(PYROMANCY).pips(5, 0, 0).acc(75).kind(DAMAGE_OVER_TIME).target(SINGLE).level(18).copies(3)
                .en("Drakeling").fr("Drakeling")
                .fx(new Damage(355, 425, PYROMANCY), new Dot(110, 110, PYROMANCY, 3))
                .add();

        spell("fan_the_flames").school(PYROMANCY).pips(2, 0, 0).acc(100).kind(TRAP).target(SINGLE).level(20)
                .en("Fan the Flames").fr("Attiser les Flammes")
                .fx(new Trap(30, set(PYROMANCY), 3))
                .add();

        spell("inferno_wyrm").school(PYROMANCY).pips(6, 0, 0).acc(75).kind(DAMAGE).target(SINGLE).level(26).copies(3)
                .en("Inferno Wyrm").fr("Wyrm des Enfers")
                .fx(new Damage(570, 670, PYROMANCY))
                .add();
    }

    /** TEMPETE - les plus gros degats du jeu, mais 70% de precision. */
    private static void tempest() {
        spell("spark_serpent").school(TEMPEST).pips(1, 0, 0).acc(70).kind(DAMAGE).target(SINGLE).level(1).copies(5)
                .en("Spark Serpent").fr("Serpent d'Etincelles")
                .fx(new Damage(125, 165, TEMPEST))
                .add();

        spell("gale_bats").school(TEMPEST).pips(2, 0, 0).acc(70).kind(DAMAGE).target(SINGLE).level(4).copies(4)
                .en("Gale Bats").fr("Chauves-souris de Bourrasque")
                .fx(new Damage(245, 285, TEMPEST))
                .add();

        spell("galeedge").school(TEMPEST).pips(0, 0, 0).acc(100).kind(BLADE).target(SELF).level(5)
                .en("Galeedge").fr("Tranchant de Rafale")
                .fx(new Blade(35, set(TEMPEST), 1))
                .add();

        spell("thermic_ward").school(TEMPEST).pips(0, 0, 0).acc(100).kind(SHIELD).target(SELF).level(6)
                .en("Thermic Ward").fr("Garde Thermique")
                .fx(new Shield(70, set(PYROMANCY, FROST), 1))
                .add();

        spell("tempest_mark").school(TEMPEST).pips(0, 0, 0).acc(100).kind(TRAP).target(SINGLE).level(7)
                .en("Tempest Mark").fr("Marque de Tempete")
                .fx(new Trap(30, set(TEMPEST), 1))
                .add();

        spell("riptide_shark").school(TEMPEST).pips(3, 0, 0).acc(70).kind(DAMAGE).target(SINGLE).level(10).copies(4)
                .en("Riptide Shark").fr("Requin de Ressac")
                .fx(new Damage(375, 435, TEMPEST))
                .add();

        spell("wave_sentinel").school(TEMPEST).pips(3, 0, 0).acc(100).kind(SUMMON).target(SELF).level(12)
                .en("Summon Wave Sentinel").fr("Invoquer une Sentinelle des Flots")
                .fx(new Summon("wave_sentinel"))
                .add();

        spell("fractured_bolt").school(TEMPEST).pips(2, 0, 0).acc(70).kind(DAMAGE).target(SINGLE).level(12)
                .en("Fractured Bolt").fr("Eclair Fracture")
                .desc("Deals either 10, 100 or 1000 Tempest Damage - pure luck!",
                      "Inflige soit 10, soit 100, soit 1000 degats de Tempete : coup de chance !")
                .fx(new OneOf(List.of(new Damage(10, 10, TEMPEST), new Damage(100, 100, TEMPEST), new Damage(1000, 1000, TEMPEST))))
                .add();

        spell("tempest_surge").school(TEMPEST).pips(-1, 4, 7).acc(70).kind(DAMAGE_ALL).target(ALL_ENEMIES).level(14).copies(3)
                .en("Tempest Surge").fr("Deferlante Tempetueuse")
                .desc("Deals 80 Tempest Damage to all enemies for each pip spent.",
                      "Inflige 80 degats de Tempete a tous les ennemis par pip depense.")
                .fx(new Damage(80, 80, TEMPEST, true))
                .add();

        spell("abyssal_kraken").school(TEMPEST).pips(4, 0, 0).acc(70).kind(DAMAGE).target(SINGLE).level(14).copies(3)
                .en("Abyssal Kraken").fr("Kraken Abyssal")
                .fx(new Damage(520, 580, TEMPEST))
                .add();

        spell("darkwind").school(TEMPEST).pips(3, 0, 0).acc(100).kind(UTILITY).target(SELF).level(20)
                .en("Darkwind").fr("Vent Noir")
                .fx(new Aura(AuraType.DAMAGE_BOOST, 25, set(TEMPEST), 4))
                .add();

        spell("thunder_herald").school(TEMPEST).pips(6, 0, 0).acc(70).kind(DAMAGE).target(SINGLE).level(22).copies(3)
                .en("Thunder Herald").fr("Heraut du Tonnerre")
                .fx(new Damage(690, 770, TEMPEST))
                .add();

        spell("supercharge").school(TEMPEST).pips(2, 0, 0).acc(100).kind(UTILITY).target(SELF).level(26)
                .en("Supercharge").fr("Surcarge")
                .desc("Consumes all your pips: adds a +5% Tempest blade per pip spent.",
                      "Consomme tous vos pips : ajoute une lame Tempete de +5% par pip depense.")
                .fx(new PipBlade(5, TEMPEST))
                .add();
    }

    /** GIVRE - la plus grosse reserve de vie et les meilleurs boucliers. */
    private static void frost() {
        spell("frost_beetle").school(FROST).pips(1, 0, 0).acc(80).kind(DAMAGE).target(SINGLE).level(1).copies(5)
                .en("Frost Beetle").fr("Scarabee de Givre")
                .fx(new Damage(65, 105, FROST))
                .add();

        spell("rimeedge").school(FROST).pips(0, 0, 0).acc(100).kind(BLADE).target(SELF).level(4)
                .en("Rimeedge").fr("Tranchant de Givre")
                .fx(new Blade(35, set(FROST), 1))
                .add();

        spell("snow_serpent").school(FROST).pips(2, 0, 0).acc(80).kind(DAMAGE).target(SINGLE).level(5).copies(4)
                .en("Snow Serpent").fr("Serpent des Neiges")
                .fx(new Damage(155, 195, FROST))
                .add();

        spell("frost_mark").school(FROST).pips(0, 0, 0).acc(100).kind(TRAP).target(SINGLE).level(6)
                .en("Frost Mark").fr("Marque de Gel")
                .fx(new Trap(30, set(FROST), 1))
                .add();

        spell("volcanic_ward").school(FROST).pips(0, 0, 0).acc(100).kind(SHIELD).target(SELF).level(7)
                .en("Volcanic Ward").fr("Garde Volcanique")
                .fx(new Shield(70, set(PYROMANCY, TEMPEST), 1))
                .add();

        spell("warding_shield").school(FROST).pips(0, 0, 0).acc(100).kind(SHIELD).target(SELF).level(8).all()
                .en("Warding Shield").fr("Bouclier Protecteur")
                .desc("Reduces the next incoming damage by 50%, whatever its school.",
                      "Reduit de 50% les prochains degats subis, quelle que soit leur ecole.")
                .fx(new Shield(50, Set.of(), 1))
                .add();

        spell("glacial_aegis").school(FROST).pips(2, 0, 0).acc(100).kind(ABSORB).target(SELF).level(10)
                .en("Glacial Aegis").fr("Egide Glaciaire")
                .fx(new Absorb(300))
                .add();

        spell("rime_chains").school(FROST).pips(2, 0, 0).acc(80).kind(STUN).target(SINGLE).level(12)
                .en("Rime Chains").fr("Chaînes de Gel")
                .fx(new Stun(1))
                .add();

        spell("blizzard").school(FROST).pips(4, 0, 0).acc(80).kind(DAMAGE_ALL).target(ALL_ENEMIES).level(16).copies(3)
                .en("Blizzard").fr("Blizzard")
                .fx(new Damage(205, 265, FROST))
                .add();

        spell("frostbite").school(FROST).pips(4, 0, 0).acc(80).kind(DAMAGE_OVER_TIME).target(SINGLE).level(18).copies(3)
                .en("Frostbite").fr("Morsure du Gel")
                .fx(new Dot(90, 90, FROST, 4))
                .add();

        spell("rime_sentinel").school(FROST).pips(3, 0, 0).acc(100).kind(SUMMON).target(SELF).level(18)
                .en("Summon Rime Sentinel").fr("Invoquer une Sentinelle de Givre")
                .fx(new Summon("rime_sentinel"))
                .add();

        spell("ice_wyvern").school(FROST).pips(4, 0, 0).acc(80).kind(DAMAGE).target(SINGLE).level(22).copies(3)
                .en("Ice Wyvern").fr("Wyverne de Glace")
                .fx(new Damage(335, 395, FROST))
                .add();

        spell("colossus").school(FROST).pips(6, 0, 0).acc(80).kind(DAMAGE).target(SINGLE).level(26).copies(3)
                .en("Colossus").fr("Colosse")
                .fx(new Damage(545, 635, FROST))
                .add();
    }

    /** RUNOMANCIE - serviteurs, pieges et etourdissements. */
    private static void runomancy() {
        spell("rune_bat").school(RUNOMANCY).pips(1, 0, 0).acc(80).kind(DAMAGE).target(SINGLE).level(1).copies(5)
                .en("Rune Bat").fr("Chauve-souris Runique")
                .fx(new Damage(70, 110, RUNOMANCY))
                .add();

        spell("stone_troll").school(RUNOMANCY).pips(2, 0, 0).acc(80).kind(DAMAGE).target(SINGLE).level(5).copies(4)
                .en("Stone Troll").fr("Troll de Pierre")
                .fx(new Damage(170, 210, RUNOMANCY))
                .add();

        spell("glyph_edge").school(RUNOMANCY).pips(0, 0, 0).acc(100).kind(BLADE).target(SELF).level(5)
                .en("Glyph Edge").fr("Tranchant Glyphique")
                .fx(new Blade(35, set(RUNOMANCY), 1))
                .add();

        spell("rune_mark").school(RUNOMANCY).pips(0, 0, 0).acc(100).kind(TRAP).target(SINGLE).level(6)
                .en("Rune Mark").fr("Marque Runique")
                .fx(new Trap(30, set(RUNOMANCY), 1))
                .add();

        spell("runestone_minion").school(RUNOMANCY).pips(3, 0, 0).acc(100).kind(SUMMON).target(SELF).level(8)
                .en("Summon Runestone Minion").fr("Invoquer un Serviteur Runique")
                .fx(new Summon("runestone_minion"))
                .add();

        spell("rune_cyclops").school(RUNOMANCY).pips(3, 0, 0).acc(80).kind(DAMAGE).target(SINGLE).level(10).copies(4)
                .en("Rune Cyclops").fr("Cyclope Runique")
                .fx(new Damage(265, 325, RUNOMANCY))
                .add();

        spell("minion_might").school(RUNOMANCY).pips(0, 0, 0).acc(100).kind(BLADE).target(ALLY).level(12)
                .en("Minion Might").fr("Vigueur du Serviteur")
                .desc("Gives an ally or a summoned minion a +40% damage blade.",
                      "Donne a un allie ou a un serviteur une lame de +40% de degats.")
                .fx(new Blade(40, Set.of(), 1))
                .add();

        spell("minion_shield").school(RUNOMANCY).pips(0, 0, 0).acc(100).kind(SHIELD).target(ALLY).level(12)
                .en("Minion Shield").fr("Bouclier du Serviteur")
                .desc("Gives an ally or a summoned minion a -70% damage shield.",
                      "Donne a un allie ou a un serviteur un bouclier de -70% de degats.")
                .fx(new Shield(70, Set.of(), 1))
                .add();

        spell("croaking_idol").school(RUNOMANCY).pips(4, 0, 0).acc(80).kind(DAMAGE_ALL).target(ALL_ENEMIES).level(16).copies(3)
                .en("Croaking Idol").fr("Idole Coassante")
                .fx(new Damage(265, 325, RUNOMANCY))
                .add();

        spell("wailing_rune").school(RUNOMANCY).pips(3, 0, 0).acc(80).kind(DAMAGE).target(SINGLE).level(20).copies(3)
                .en("Wailing Rune").fr("Rune Lamentable")
                .fx(new Damage(240, 300, RUNOMANCY), new Weakness(25, 1))
                .add();

        spell("runic_fury").school(RUNOMANCY).pips(3, 0, 0).acc(100).kind(UTILITY).target(SELF).level(22)
                .en("Runic Fury").fr("Fureur Runique")
                .fx(new Aura(AuraType.DAMAGE_BOOST, 25, set(RUNOMANCY), 4))
                .add();

        spell("rune_colossus").school(RUNOMANCY).pips(6, 0, 0).acc(100).kind(SUMMON).target(SELF).level(26)
                .en("Summon Rune Colossus").fr("Invoquer un Colosse Runique")
                .fx(new Summon("rune_colossus"))
                .add();

        spell("petrifying_gaze").school(RUNOMANCY).pips(5, 0, 0).acc(80).kind(DAMAGE).target(SINGLE).level(28).copies(3)
                .en("Petrifying Gaze").fr("Regard Petrifiant")
                .fx(new Damage(480, 560, RUNOMANCY), new Stun(1))
                .add();
    }

    /** SYLVE - l'ecole des soins, precision 90%. */
    private static void sylvan() {
        spell("sprout_imp").school(SYLVAN).pips(1, 0, 0).acc(90).kind(DAMAGE).target(SINGLE).level(1).copies(5)
                .en("Sprout Imp").fr("Lutin Germe")
                .fx(new Damage(65, 105, SYLVAN))
                .add();

        spell("minor_blessing").school(SYLVAN).pips(0, 0, 0).acc(100).kind(HEAL).target(SELF).level(2)
                .en("Minor Blessing").fr("Benediction Mineure")
                .fx(new Heal(65, 65))
                .add();

        spell("cloverkin").school(SYLVAN).pips(2, 0, 0).acc(90).kind(DAMAGE).target(SINGLE).level(5).copies(4)
                .en("Cloverkin").fr("Trèfle Vivant")
                .fx(new Damage(155, 195, SYLVAN))
                .add();

        spell("verdant_edge").school(SYLVAN).pips(0, 0, 0).acc(100).kind(BLADE).target(SELF).level(5)
                .en("Verdant Edge").fr("Tranchant Verdoyant")
                .fx(new Blade(35, set(SYLVAN), 1))
                .add();

        spell("thorn_mark").school(SYLVAN).pips(0, 0, 0).acc(100).kind(TRAP).target(SINGLE).level(6)
                .en("Thorn Mark").fr("Marque d'Epines")
                .fx(new Trap(30, set(SYLVAN), 1))
                .add();

        spell("faerie_gift").school(SYLVAN).pips(2, 0, 0).acc(100).kind(HEAL).target(ALLY).level(8).copies(4)
                .en("Faerie Gift").fr("Don de la Fee")
                .fx(new Heal(420, 420))
                .add();

        spell("legend_ward").school(SYLVAN).pips(0, 0, 0).acc(100).kind(SHIELD).target(SELF).level(8)
                .en("Legend Ward").fr("Garde Legendarie")
                .fx(new Shield(70, set(NECROMANCY, RUNOMANCY), 1))
                .add();

        spell("sprite_bloom").school(SYLVAN).pips(1, 0, 0).acc(100).kind(HEAL_OVER_TIME).target(ALLY).level(10).copies(4)
                .en("Sprite Bloom").fr("Floraison de Lutins")
                .fx(new Heal(30, 30), new Hot(90, 90, 3))
                .add();

        spell("unicorn_blessing").school(SYLVAN).pips(3, 0, 0).acc(100).kind(HEAL_ALL).target(ALL_ALLIES).level(12).copies(3)
                .en("Unicorn Blessing").fr("Benediction de la Licorne")
                .fx(new Heal(275, 275))
                .add();

        spell("guiding_light").school(SYLVAN).pips(1, 0, 0).acc(100).kind(UTILITY).target(SELF).level(20)
                .en("Guiding Light").fr("Lumiere Guide")
                .fx(new HealBlade(30, 1))
                .add();

        spell("radiant_seraph").school(SYLVAN).pips(4, 0, 0).acc(90).kind(DAMAGE).target(SINGLE).level(22).copies(3)
                .en("Radiant Seraph").fr("Seraphin Radieux")
                .fx(new Damage(335, 395, SYLVAN))
                .add();

        spell("sanctuary").school(SYLVAN).pips(4, 0, 0).acc(100).kind(UTILITY).target(SELF).level(24)
                .en("Sanctuary").fr("Sanctuaire")
                .fx(new Aura(AuraType.HEAL_BOOST, 40, Set.of(), 4))
                .add();

        spell("renewal").school(SYLVAN).pips(5, 0, 0).acc(100).kind(HEAL_OVER_TIME).target(ALLY).level(26).copies(3)
                .en("Renewal").fr("Renouveau")
                .fx(new Heal(300, 300), new Hot(150, 150, 3))
                .add();

        spell("centaur_charge").school(SYLVAN).pips(6, 0, 0).acc(90).kind(DAMAGE).target(SINGLE).level(28).copies(3)
                .en("Centaur Charge").fr("Charge du Centaure")
                .fx(new Damage(500, 580, SYLVAN))
                .add();
    }

    /** NECROMANCIE - voleur de vie : les degats soignent le lanceur. */
    private static void necromancy() {
        spell("wisp_wraith").school(NECROMANCY).pips(1, 0, 0).acc(85).kind(DAMAGE).target(SINGLE).level(1).copies(5)
                .en("Wisp Wraith").fr("Spectre Follet")
                .fx(new Damage(65, 105, NECROMANCY))
                .add();

        spell("ghoul").school(NECROMANCY).pips(2, 0, 0).acc(85).kind(DRAIN).target(SINGLE).level(5).copies(4)
                .en("Ghoul").fr("Goule")
                .fx(new Drain(160, 160, NECROMANCY, 0.5))
                .add();

        spell("grave_edge").school(NECROMANCY).pips(0, 0, 0).acc(100).kind(BLADE).target(SELF).level(5)
                .en("Grave Edge").fr("Tranchant Sepulcral")
                .fx(new Blade(35, set(NECROMANCY), 1))
                .add();

        spell("grave_mark").school(NECROMANCY).pips(0, 0, 0).acc(100).kind(TRAP).target(SINGLE).level(6)
                .en("Grave Mark").fr("Marque Funebre")
                .fx(new Trap(30, set(NECROMANCY), 1))
                .add();

        spell("dream_ward").school(NECROMANCY).pips(0, 0, 0).acc(100).kind(SHIELD).target(SELF).level(8)
                .en("Dream Ward").fr("Garde Onirique")
                .fx(new Shield(70, set(SYLVAN, RUNOMANCY), 1))
                .add();

        spell("bone_corsair").school(NECROMANCY).pips(3, 0, 0).acc(85).kind(DAMAGE).target(SINGLE).level(10).copies(4)
                .en("Bone Corsair").fr("Corsaire d'Os")
                .fx(new Damage(275, 345, NECROMANCY))
                .add();

        spell("bloodmoth").school(NECROMANCY).pips(4, 0, 0).acc(85).kind(DRAIN).target(SINGLE).level(16).copies(3)
                .en("Bloodmoth").fr("Papillon de Sang")
                .fx(new Drain(300, 360, NECROMANCY, 0.5))
                .add();

        spell("plague").school(NECROMANCY).pips(2, 0, 0).acc(100).kind(WEAKNESS).target(ALL_ENEMIES).level(18)
                .en("Plague").fr("Peste")
                .fx(new Weakness(20, 1))
                .add();

        spell("wraith").school(NECROMANCY).pips(5, 0, 0).acc(85).kind(DRAIN).target(SINGLE).level(20).copies(3)
                .en("Wraith").fr("Spectre")
                .fx(new Drain(400, 470, NECROMANCY, 0.5))
                .add();

        spell("blood_offering").school(NECROMANCY).pips(3, 0, 0).acc(100).kind(HEAL).target(ALLY).level(22)
                .en("Blood Offering").fr("Offrande de Sang")
                .desc("Sacrifices 250 of your own health to heal an ally for 500.",
                      "Sacrifie 250 de vos points de vie pour soigner un allie de 500.")
                .fx(new SelfDamage(250, 0), new Heal(500, 500))
                .add();

        spell("harvest_of_bones").school(NECROMANCY).pips(4, 0, 0).acc(85).kind(DRAIN).target(ALL_ENEMIES).level(24).copies(3)
                .en("Harvest of Bones").fr("Moisson d'Os")
                .desc("Deals 250-310 Necromancy Damage to all enemies and heals you for half of the damage dealt.",
                      "Inflige 250-310 degats de Necromancie a tous les ennemis et vous soigne de la moitie des degats infliges.")
                .fx(new Drain(250, 310, NECROMANCY, 0.5))
                .add();

        spell("feint").school(NECROMANCY).pips(0, 0, 0).acc(100).kind(TRAP).target(SINGLE).level(26).copies(2)
                .en("Feint").fr("Feinte")
                .desc("Places a +70% trap on the enemy and a +30% trap on yourself. A double-edged classic!",
                      "Pose un piege de +70% sur l'ennemi et un piege de +30% sur vous-meme. Un classique a double tranchant !")
                .fx(new Trap(70, Set.of(), 1), new SelfTrap(30, Set.of(), 1))
                .add();

        spell("doom_and_gloom").school(NECROMANCY).pips(3, 0, 0).acc(100).kind(UTILITY).target(SELF).level(30)
                .en("Doom and Gloom").fr("Ruine et Tenebres")
                .fx(new Aura(AuraType.HEAL_REDUCTION, 25, Set.of(), 4))
                .add();
    }

    /** EQUILIBRE - buffs universels, gestion du deck, equilibre. */
    private static void equilibrium() {
        spell("scarab").school(EQUILIBRIUM).pips(1, 0, 0).acc(85).kind(DAMAGE).target(SINGLE).level(1).copies(5)
                .en("Scarab").fr("Scarabee")
                .fx(new Damage(65, 105, EQUILIBRIUM))
                .add();

        spell("scorpion").school(EQUILIBRIUM).pips(2, 0, 0).acc(85).kind(DAMAGE).target(SINGLE).level(5).copies(4)
                .en("Scorpion").fr("Scorpion")
                .fx(new Damage(160, 200, EQUILIBRIUM))
                .add();

        spell("equilibrium_edge").school(EQUILIBRIUM).pips(0, 0, 0).acc(100).kind(BLADE).target(SELF).level(5)
                .en("Equilibrium Edge").fr("Tranchant d'Equilibre")
                .desc("Adds a +25% blade to your next spell, whatever its school.",
                      "Ajoute une lame de +25% a votre prochain sort, quelle que soit son ecole.")
                .fx(new Blade(25, Set.of(), 1))
                .add();

        spell("weakness").school(EQUILIBRIUM).pips(0, 0, 0).acc(100).kind(WEAKNESS).target(SINGLE).level(8)
                .en("Weakness").fr("Faiblesse")
                .fx(new Weakness(25, 1))
                .add();

        spell("hex").school(EQUILIBRIUM).pips(1, 0, 0).acc(100).kind(TRAP).target(SINGLE).level(16)
                .en("Hex").fr("Malefice")
                .desc("Places a +30% trap on the target, whatever the school of the next spell.",
                      "Pose un piege de +30% sur la cible, quelle que soit l'ecole du prochain sort.")
                .fx(new Trap(30, Set.of(), 1))
                .add();

        spell("sandstorm").school(EQUILIBRIUM).pips(4, 0, 0).acc(85).kind(DAMAGE_ALL).target(ALL_ENEMIES).level(16).copies(3)
                .en("Sandstorm").fr("Tempete de Sable")
                .fx(new Damage(240, 300, EQUILIBRIUM))
                .add();

        spell("elemental_edge").school(EQUILIBRIUM).pips(0, 0, 0).acc(100).kind(BLADE).target(SELF).level(18)
                .en("Elemental Edge").fr("Tranchant Elementaire")
                .desc("Adds a +25% blade for the next Pyromancy, Tempest or Frost spell.",
                      "Ajoute une lame de +25% pour le prochain sort de Pyromancie, Tempete ou Givre.")
                .fx(new Blade(25, set(PYROMANCY, TEMPEST, FROST), 1))
                .add();

        spell("spirit_edge").school(EQUILIBRIUM).pips(0, 0, 0).acc(100).kind(BLADE).target(SELF).level(18)
                .en("Spirit Edge").fr("Tranchant Spirituel")
                .desc("Adds a +25% blade for the next Sylvan, Necromancy or Runomancy spell.",
                      "Ajoute une lame de +25% pour le prochain sort de Sylve, Necromancie ou Runomancie.")
                .fx(new Blade(25, set(SYLVAN, NECROMANCY, RUNOMANCY), 1))
                .add();

        spell("helping_hands").school(EQUILIBRIUM).pips(3, 0, 0).acc(100).kind(HEAL).target(ALLY).level(20).copies(3)
                .en("Helping Hands").fr("Mains Secourables")
                .fx(new Heal(400, 400))
                .add();

        spell("judgement").school(EQUILIBRIUM).pips(-1, 1, 7).acc(85).kind(DAMAGE).target(SINGLE).level(22).copies(3)
                .en("Judgement").fr("Jugement")
                .desc("Deals 100 Equilibrium Damage for each pip spent.",
                      "Inflige 100 degats d'Equilibre par pip depense.")
                .fx(new Damage(100, 100, EQUILIBRIUM, true))
                .add();

        spell("spectral_blast").school(EQUILIBRIUM).pips(3, 0, 0).acc(85).kind(DAMAGE).target(SINGLE).level(24).copies(3)
                .en("Spectral Blast").fr("Decharge Spectrale")
                .desc("Deals 300-400 damage of a random Elemental school (Pyromancy, Tempest or Frost).",
                      "Inflige 300-400 degats d'une ecole elementaire aleatoire (Pyromancie, Tempete ou Givre).")
                .fx(new RandomSchoolDamage(300, 400, set(PYROMANCY, TEMPEST, FROST)))
                .add();

        spell("reshuffle").school(EQUILIBRIUM).pips(4, 0, 0).acc(100).kind(DECK).target(SELF).level(24)
                .en("Reshuffle").fr("Rebattre")
                .desc("Shuffles your discard pile back into your deck and draws a new hand.",
                      "Remelange votre defausse dans votre deck et pioche une nouvelle main.")
                .fx(new Reshuffle(), new DrawCards(7))
                .add();

        spell("equilibrium_aura").school(EQUILIBRIUM).pips(3, 0, 0).acc(100).kind(UTILITY).target(SELF).level(28)
                .en("Equilibrium Aura").fr("Aura d'Equilibre")
                .fx(new Aura(AuraType.DAMAGE_BOOST, 20, Set.of(), 4))
                .add();

        spell("hydra").school(EQUILIBRIUM).pips(6, 0, 0).acc(85).kind(DAMAGE).target(SINGLE).level(30).copies(2)
                .en("Hydra").fr("Hydre")
                .desc("Strikes three times: 130-150 Pyromancy, Frost and Tempest damage.",
                      "Frappe trois fois : 130-150 degats de Pyromancie, de Givre et de Tempete.")
                .fx(new Damage(130, 150, PYROMANCY), new Damage(130, 150, FROST), new Damage(130, 150, TEMPEST))
                .add();
    }

    /**
     * Sorts utilisables par tous les sorciers : protections monocible,
     * nettoyage de charmes et attaques de baguette.
     */
    private static void universalSpells() {
        // Protections monocible -80% (entrainables par tous, comme chez Sabrina Greenstar)
        ward("ember_ward", PYROMANCY, "Ember Ward", "Garde de Braise");
        ward("tempest_ward", TEMPEST, "Tempest Ward", "Garde de Tempete");
        ward("frost_ward", FROST, "Frost Ward", "Garde de Givre");
        ward("rune_ward", RUNOMANCY, "Rune Ward", "Garde Runique");
        ward("verdant_ward", SYLVAN, "Verdant Ward", "Garde Verdoyante");
        ward("grave_ward", NECROMANCY, "Grave Ward", "Garde Sepulcrale");
        ward("karmic_ward", EQUILIBRIUM, "Karmic Ward", "Garde Karmique");

        spell("cleanse_charm").school(EQUILIBRIUM).pips(0, 0, 0).acc(100).kind(UTILITY).target(SELF).level(10).all()
                .en("Cleanse Charm").fr("Purifier le Sort")
                .desc("Removes a harmful charm (Weakness, accuracy debuff) from yourself.",
                      "Retire un charme nefaste (Faiblesse, malus de precision) qui vous affecte.")
                .fx(new Cleanse(CleanseTarget.HARMFUL_CHARM))
                .add();

        spell("disarm").school(EQUILIBRIUM).pips(1, 0, 0).acc(100).kind(UTILITY).target(SINGLE).level(16).all()
                .en("Disarm").fr("Desarmer")
                .desc("Removes a blade from the target.",
                      "Retire une lame de la cible.")
                .fx(new Disarm())
                .add();

        spell("steal_charm").school(EQUILIBRIUM).pips(1, 0, 0).acc(100).kind(UTILITY).target(SINGLE).level(20).all()
                .en("Steal Charm").fr("Voler le Sort")
                .desc("Steals a blade from the target and keeps it for yourself.",
                      "Vole une lame de la cible et la garde pour vous.")
                .fx(new StealCharm())
                .add();

        spell("triage").school(EQUILIBRIUM).pips(1, 0, 0).acc(100).kind(UTILITY).target(ALLY).level(22).all()
                .en("Triage").fr("Triage")
                .desc("Removes all damage-over-time effects from an ally.",
                      "Retire tous les effets de degats sur la duree qui affectent un allie.")
                .fx(new Cleanse(CleanseTarget.DAMAGE_OVER_TIME))
                .add();

        spell("piercing_aegis").school(EQUILIBRIUM).pips(2, 0, 0).acc(100).kind(UTILITY).target(SELF).level(26).all()
                .en("Piercing Aegis").fr("Egide Perforante")
                .desc("Your next spell ignores 20% of the target's resistance.",
                      "Votre prochain sort ignore 20% de la resistance de la cible.")
                .fx(new PierceAura(20, 1))
                .add();

        // Attaques de baguette : 0 pip, 100% de precision
        for (School school : School.values()) {
            spell("wand_" + school.id()).school(school).pips(0, 0, 0).acc(100).kind(WAND).target(SINGLE).level(1).copies(4).all()
                    .en(cap(school.displayEn()) + " Wand Strike")
                    .fr("Frappe de Baguette " + school.displayFr())
                    .fx(new Damage(40, 60, school))
                    .add();
        }
    }

    private static void ward(String id, School school, String en, String fr) {
        spell(id).school(EQUILIBRIUM).pips(0, 0, 0).acc(100).kind(SHIELD).target(SELF).level(12).all()
                .en(en).fr(fr)
                .desc("Reduces the next " + school.displayEn() + " damage by 80%.",
                      "Reduit de 80% les prochains degats de " + school.displayFr() + ".")
                .fx(new Shield(80, set(school), 1))
                .add();
    }

    private static String cap(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }
}
