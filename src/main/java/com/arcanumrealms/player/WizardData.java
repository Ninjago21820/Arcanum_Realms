package com.arcanumrealms.player;

import com.arcanumrealms.ArcanumConfig;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Donnees d'un sorcier : ecole, niveau, sorts appris et deck.
 *
 * <p>Elles sont stockees dans les donnees persistantes de l'entite joueur
 * (Forge), donc sauvegardees avec le monde et conservees a la mort.
 */
public final class WizardData {

    /** Cle NBT racine des donnees du sorcier dans les donnees persistantes du joueur. */
    public static final String NBT_ROOT = "arcanum_realms:wizard";

    private static final String ROOT = NBT_ROOT;

    private School school = School.PYROMANCY;
    private int level = 1;
    private int xp;
    /** -1 = valeur de la configuration. */
    private int powerPipChance = -1;
    private int critRating = -1;
    private final Set<String> learned = new LinkedHashSet<>();
    private final Map<String, Integer> deck = new LinkedHashMap<>();
    /** Or gagne en duel et en quete (comme les pieces lachees par les creatures). */
    private int gold;
    /** Journal de quetes du joueur. */
    private final com.arcanumrealms.core.world.QuestLog quests = new com.arcanumrealms.core.world.QuestLog();
    /** Equipement porte, par emplacement. */
    private final Map<com.arcanumrealms.core.world.GearSlot, String> equipped =
            new EnumMap<>(com.arcanumrealms.core.world.GearSlot.class);

    // ------------------------------------------------------------ acces

    public School school() { return school; }
    public void setSchool(School school) { this.school = school; }
    public int level() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); }
    public int xp() { return xp; }
    public int powerPipChance() { return powerPipChance; }
    public void setPowerPipChance(int value) { this.powerPipChance = value; }
    public int critRating() { return critRating; }
    public void setCritRating(int value) { this.critRating = value; }
    public Set<String> learnedIds() { return Collections.unmodifiableSet(learned); }
    public Map<String, Integer> deck() { return Collections.unmodifiableMap(deck); }

    public List<Spell> learnedSpells() {
        List<Spell> out = new ArrayList<>();
        for (String id : learned) SpellBook.find(id).ifPresent(out::add);
        out.sort(Comparator.comparingInt(Spell::minLevel).thenComparing(Spell::id));
        return out;
    }

    public int gold() { return gold; }

    public void addGold(int amount) { gold = Math.max(0, gold + amount); }

    public boolean spendGold(int amount) {
        if (amount <= 0) return true;
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }

    public void setGold(int value) { gold = Math.max(0, value); }

    public com.arcanumrealms.core.world.QuestLog quests() { return quests; }

    /** Equipement porte, dans l'ordre des emplacements. */
    public List<com.arcanumrealms.core.world.GearDef> equippedGear() {
        List<com.arcanumrealms.core.world.GearDef> out = new ArrayList<>();
        for (String id : equipped.values()) {
            com.arcanumrealms.core.world.Gears.find(id).ifPresent(out::add);
        }
        return out;
    }

    public String equippedId(com.arcanumrealms.core.world.GearSlot slot) {
        return equipped.get(slot);
    }

    /**
     * Porte une piece d'equipement.
     *
     * @return l'identifiant de la piece remplacee, ou null s'il n'y en avait pas
     */
    public String equip(com.arcanumrealms.core.world.GearDef gear) {
        return equipped.put(gear.slot(), gear.id());
    }

    public String unequip(com.arcanumrealms.core.world.GearSlot slot) {
        return equipped.remove(slot);
    }

    public int deckSize() {
        int total = 0;
        for (int n : deck.values()) total += n;
        return total;
    }

    /** Liste de cartes developpee, prete pour le moteur de duel. */
    public List<String> buildCardList() {
        List<String> cards = new ArrayList<>();
        for (Map.Entry<String, Integer> e : deck.entrySet()) {
            for (int i = 0; i < e.getValue(); i++) cards.add(e.getKey());
        }
        return cards;
    }

    // -------------------------------------------------- contenu de depart

    /** Apprend tous les sorts debloques par le niveau courant. */
    /** Apprend un sort (recompense de quete). Renvoie faux s'il est deja connu. */
    public boolean learn(String spellId) {
        if (SpellBook.find(spellId).isEmpty()) return false;
        return learned.add(spellId);
    }

    public List<Spell> refreshLearnedSpells() {
        List<Spell> newlyLearned = new ArrayList<>();
        for (Spell spell : SpellBook.trainable(school, level)) {
            if (learned.add(spell.id())) newlyLearned.add(spell);
        }
        if (ArcanumConfig.AUTO_LEARN_SPELLS.get()) {
            for (Spell spell : newlyLearned) addCard(spell.id());
        }
        return newlyLearned;
    }

    /** Construit un deck de depart raisonnable (attaques pas cheres + protections). */
    public void ensureStarterDeck() {
        refreshLearnedSpells();
        if (deckSize() > 0) return;
        List<Spell> available = learnedSpells();
        // 1. les baguettes et petites attaques
        for (Spell spell : available) {
            if (spell.isAttack() && spell.minPips() <= 2) fillTo(spell, 3);
        }
        // 2. une lame et un bouclier
        for (Spell spell : available) {
            if ((spell.kind() == com.arcanumrealms.core.spell.SpellKind.BLADE
                    || spell.kind() == com.arcanumrealms.core.spell.SpellKind.SHIELD
                    || spell.kind() == com.arcanumrealms.core.spell.SpellKind.TRAP) && deckSize() < deckLimit()) {
                fillTo(spell, 2);
            }
        }
        // 3. de quoi remplir
        for (Spell spell : available) {
            if (deckSize() >= deckLimit()) break;
            fillTo(spell, 2);
        }
    }

    private void fillTo(Spell spell, int copies) {
        int limit = Math.min(spell.maxCopies(), Math.max(1, copies));
        int current = deck.getOrDefault(spell.id(), 0);
        int room = deckLimit() - deckSize();
        if (room <= 0) return;
        int add = Math.min(limit - current, room);
        if (add > 0) deck.put(spell.id(), current + add);
    }

    public static int deckLimit() {
        return ArcanumConfig.DECK_SIZE_LIMIT.get();
    }

    // ------------------------------------------------------------- deck

    public boolean addCard(String spellId) {
        Spell spell = SpellBook.get(spellId);
        if (spell.minLevel() > level || deckSize() >= deckLimit()) return false;
        int current = deck.getOrDefault(spellId, 0);
        if (current >= spell.maxCopies()) return false;
        deck.put(spellId, current + 1);
        return true;
    }

    public boolean removeCard(String spellId) {
        Integer current = deck.get(spellId);
        if (current == null || current <= 0) return false;
        if (current == 1) deck.remove(spellId);
        else deck.put(spellId, current - 1);
        return true;
    }

    public void clearDeck() { deck.clear(); }

    public void setDeck(Map<String, Integer> newDeck) {
        deck.clear();
        int total = 0;
        for (Map.Entry<String, Integer> e : newDeck.entrySet()) {
            Optional<Spell> spell = SpellBook.find(e.getKey());
            if (spell.isEmpty() || !learned.contains(e.getKey())) continue;
            int copies = Math.max(0, Math.min(e.getValue(), spell.get().maxCopies()));
            if (copies == 0) continue;
            if (total + copies > deckLimit()) copies = deckLimit() - total;
            if (copies <= 0) break;
            deck.put(e.getKey(), copies);
            total += copies;
        }
    }

    // --------------------------------------------------------- experience

    /** Ajoute de l'experience et renvoie les niveaux gagnes. */
    public int grantXp(int amount) {
        xp += Math.max(0, amount);
        int gained = 0;
        while (xp >= xpForNextLevel()) {
            xp -= xpForNextLevel();
            level++;
            gained++;
        }
        if (gained > 0) refreshLearnedSpells();
        return gained;
    }

    public int xpForNextLevel() {
        return Math.max(10, ArcanumConfig.XP_TO_LEVEL_UP.get()) * level;
    }

    // -------------------------------------------------- persistance NBT

    public static WizardData load(Player player) {
        WizardData data = new WizardData();
        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        if (!root.isEmpty()) data.read(root);
        return data;
    }

    public void save(Player player) {
        player.getPersistentData().put(ROOT, write());
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("school", school.id());
        tag.putInt("level", level);
        tag.putInt("xp", xp);
        tag.putInt("powerPipChance", powerPipChance);
        tag.putInt("critRating", critRating);
        ListTag learnedTag = new ListTag();
        for (String id : learned) learnedTag.add(net.minecraft.nbt.StringTag.valueOf(id));
        tag.put("learned", learnedTag);
        CompoundTag deckTag = new CompoundTag();
        deck.forEach(deckTag::putInt);
        tag.put("deck", deckTag);
        tag.putInt("gold", gold);
        CompoundTag questTag = new CompoundTag();
        questTag.put("active", stringList(quests.active()));
        questTag.put("completed", stringList(new ArrayList<>(quests.completed())));
        CompoundTag progressTag = new CompoundTag();
        quests.rawProgress().forEach((questId, goals) -> {
            CompoundTag goalsTag = new CompoundTag();
            goals.forEach(goalsTag::putInt);
            progressTag.put(questId, goalsTag);
        });
        questTag.put("progress", progressTag);
        tag.put("quests", questTag);
        CompoundTag gearTag = new CompoundTag();
        equipped.forEach((slot, id) -> gearTag.putString(slot.name(), id));
        tag.put("gear", gearTag);
        return tag;
    }

    private static ListTag stringList(List<String> values) {
        ListTag list = new ListTag();
        for (String value : values) list.add(net.minecraft.nbt.StringTag.valueOf(value));
        return list;
    }

    private static List<String> readStringList(CompoundTag tag, String key) {
        List<String> out = new ArrayList<>();
        if (!tag.contains(key, Tag.TAG_LIST)) return out;
        ListTag list = tag.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) out.add(list.getString(i));
        return out;
    }

    public void read(CompoundTag tag) {
        if (tag.contains("school")) {
            try {
                school = School.byId(tag.getString("school"));
            } catch (IllegalArgumentException ignored) {
                school = School.PYROMANCY;
            }
        }
        level = Math.max(1, tag.getInt("level"));
        xp = tag.getInt("xp");
        powerPipChance = tag.contains("powerPipChance") ? tag.getInt("powerPipChance") : -1;
        critRating = tag.contains("critRating") ? tag.getInt("critRating") : -1;
        learned.clear();
        if (tag.contains("learned", Tag.TAG_LIST)) {
            ListTag list = tag.getList("learned", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) learned.add(list.getString(i));
        }
        deck.clear();
        CompoundTag deckTag = tag.getCompound("deck");
        for (String key : deckTag.getAllKeys()) deck.put(key, deckTag.getInt(key));
        gold = tag.getInt("gold");
        CompoundTag questTag = tag.getCompound("quests");
        Map<String, Map<String, Integer>> progress = new LinkedHashMap<>();
        CompoundTag progressTag = questTag.getCompound("progress");
        for (String questId : progressTag.getAllKeys()) {
            CompoundTag goalsTag = progressTag.getCompound(questId);
            Map<String, Integer> goals = new LinkedHashMap<>();
            for (String goalKey : goalsTag.getAllKeys()) goals.put(goalKey, goalsTag.getInt(goalKey));
            progress.put(questId, goals);
        }
        quests.restore(readStringList(questTag, "active"),
                new LinkedHashSet<>(readStringList(questTag, "completed")), progress);
        equipped.clear();
        CompoundTag gearTag = tag.getCompound("gear");
        for (String slotName : gearTag.getAllKeys()) {
            try {
                com.arcanumrealms.core.world.GearSlot slot =
                        com.arcanumrealms.core.world.GearSlot.valueOf(slotName);
                equipped.put(slot, gearTag.getString(slotName));
            } catch (IllegalArgumentException ignored) {
                // emplacement inconnu : ignore (contenu retire dans une version plus recente)
            }
        }
    }
}
