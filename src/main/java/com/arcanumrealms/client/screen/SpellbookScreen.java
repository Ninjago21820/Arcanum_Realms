package com.arcanumrealms.client.screen;

import com.arcanumrealms.client.ClientBattleState;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.client.ContentResolver;
import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.core.world.ContentKeys;
import com.arcanumrealms.core.spell.SpellDescriber;
import com.arcanumrealms.network.NetworkHandler;
import com.arcanumrealms.network.packet.SaveDeckPacket;
import com.arcanumrealms.network.packet.WizardDataPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Grimoire : fiche du sorcier et construction du deck.
 *
 * <p>Deux colonnes : a gauche les sorts appris (clic gauche = ajouter, clic droit = retirer),
 * a droite le deck en cours d'edition. Le bouton enregistrer envoie le deck au serveur.
 */
public class SpellbookScreen extends Screen {

    private static final int ROW_HEIGHT = 11;
    private static final int COLUMN_WIDTH = 168;

    private final WizardDataPacket data;
    private final Map<String, Integer> deck = new LinkedHashMap<>();
    private final List<SpellRow> learnedRows = new ArrayList<>();
    private final List<SpellRow> deckRows = new ArrayList<>();

    private int scrollLearned;
    private int scrollDeck;
    private Button saveButton;

    public SpellbookScreen(WizardDataPacket data) {
        super(Component.translatable("gui.arcanum_realms.grimoire.title"));
        this.data = data;
        if (data != null) this.deck.putAll(data.deck);
    }

    private boolean french() {
        return ClientBattleState.french();
    }

    private String tr(String key) {
        Component component = Component.translatable(key);
        String text = component.getString();
        return text.equals(key) ? key : text;
    }

    private int deckSize() {
        int total = 0;
        for (int count : deck.values()) total += count;
        return total;
    }

    private int deckLimit() {
        int limit = 0;
        try {
            limit = com.arcanumrealms.ArcanumConfig.deckSizeLimitClient();
        } catch (Throwable ignored) {
            limit = 40;
        }
        return limit <= 0 ? 40 : limit;
    }

    @Override
    protected void init() {
        super.init();
        this.saveButton = Button.builder(Component.translatable("gui.arcanum_realms.button.save"), button -> {
            NetworkHandler.sendToServer(new SaveDeckPacket(deck));
        }).bounds(this.width / 2 - 60, this.height - 24, 120, 20).build();
        addRenderableWidget(saveButton);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        learnedRows.clear();
        deckRows.clear();

        if (data == null) {
            graphics.drawCenteredString(this.font, tr("gui.arcanum_realms.grimoire.waiting"),
                    this.width / 2, this.height / 2, 0xFFFFFFFF);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        School school = data.parsedSchool();
        int schoolColor = 0xFF000000 | school.color();

        // ----------------------------------------------------------- en-tete
        graphics.drawString(this.font, tr("gui.arcanum_realms.grimoire.wizard") + " : " + school.display(french()),
                16, 12, schoolColor);
        graphics.drawString(this.font, tr("gui.arcanum_realms.grimoire.level") + " " + data.level
                + "  (" + data.xp + "/" + data.xpNext + " XP)", 16, 24, 0xFFE0E0E0);
        graphics.drawString(this.font, tr("gui.arcanum_realms.grimoire.powerpips") + " : "
                + data.powerPipChance + "%   " + tr("gui.arcanum_realms.grimoire.crit") + " : " + data.critRating,
                16, 36, 0xFFB0D0FF);

        // ------------------------------------------------------- liste sorts
        List<Spell> learned = new ArrayList<>();
        for (String id : data.learned) SpellBook.find(id).ifPresent(learned::add);
        learned.sort((a, b) -> Integer.compare(a.minLevel(), b.minLevel()));

        int listTop = 54;
        int listBottom = this.height - 42;
        int leftX = 16;
        int rightX = this.width - 16 - COLUMN_WIDTH;

        graphics.fill(leftX - 2, listTop - 14, leftX + COLUMN_WIDTH, listBottom + 2, 0x99202020);
        graphics.fill(rightX - 2, listTop - 14, rightX + COLUMN_WIDTH, listBottom + 2, 0x99202020);
        graphics.drawString(this.font, tr("gui.arcanum_realms.grimoire.learned") + " (" + learned.size() + ")",
                leftX, listTop - 11, 0xFFE8D9A0);
        graphics.drawString(this.font, tr("gui.arcanum_realms.grimoire.deck") + " (" + deckSize() + "/" + deckLimit() + ")",
                rightX, listTop - 11, 0xFFE8D9A0);

        // sorts appris
        int y = listTop;
        int index = 0;
        for (Spell spell : learned) {
            if (index++ < scrollLearned) continue;
            if (y > listBottom - ROW_HEIGHT) break;
            drawSpellRow(graphics, leftX, y, spell, deck.getOrDefault(spell.id(), 0), mouseX, mouseY);
            learnedRows.add(new SpellRow(leftX, y, spell, true));
            y += ROW_HEIGHT;
        }

        // deck en construction
        y = listTop;
        index = 0;
        for (Map.Entry<String, Integer> entry : deck.entrySet()) {
            if (index++ < scrollDeck) continue;
            if (y > listBottom - ROW_HEIGHT) break;
            Spell spell = SpellBook.find(entry.getKey()).orElse(null);
            if (spell == null) continue;
            drawSpellRow(graphics, rightX, y, spell, entry.getValue(), mouseX, mouseY);
            deckRows.add(new SpellRow(rightX, y, spell, false));
            y += ROW_HEIGHT;
        }

        // info-bulle du sort survole
        for (SpellRow row : learnedRows) {
            if (row.contains(mouseX, mouseY)) {
                drawTooltip(graphics, row.spell, mouseX, mouseY);
                break;
            }
        }
        for (SpellRow row : deckRows) {
            if (row.contains(mouseX, mouseY)) {
                drawTooltip(graphics, row.spell, mouseX, mouseY);
                break;
            }
        }

        graphics.drawString(this.font, tr("gui.arcanum_realms.grimoire.hint"), 16, this.height - 36, 0xFFA0A0A0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawSpellRow(GuiGraphics graphics, int x, int y, Spell spell, int count, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + COLUMN_WIDTH && mouseY >= y - 1 && mouseY < y + ROW_HEIGHT - 1;
        if (hovered) graphics.fill(x, y - 1, x + COLUMN_WIDTH, y + ROW_HEIGHT - 1, 0x55FFFFFF);
        School school = spell.school();
        graphics.fill(x, y, x + 4, y + 9, 0xFF000000 | school.color());
        String cost = spell.variablePips() ? "X" : String.valueOf(spell.pipCost());
        String label = ContentResolver.resolve(ContentKeys.spellName(spell, french()));
        int maxLabel = COLUMN_WIDTH - 66;
        while (this.font.width(label) > maxLabel && label.length() > 3) {
            label = label.substring(0, label.length() - 1);
        }
        graphics.drawString(this.font, label, x + 8, y, 0xFFE0E0E0);
        graphics.drawString(this.font, cost + "p " + spell.accuracy() + "%", x + COLUMN_WIDTH - 44, y, 0xFFA0C8FF);
        if (count > 0) {
            graphics.drawString(this.font, "x" + count, x + COLUMN_WIDTH - 14, y, 0xFFFFE066);
        }
    }

    private void drawTooltip(GuiGraphics graphics, Spell spell, int mouseX, int mouseY) {
        List<String> lines = new ArrayList<>();
        lines.add(ContentResolver.resolve(ContentKeys.spellName(spell, french())) + " — " + spell.school().display(french()));
        lines.add((spell.variablePips() ? ("X+" + spell.minPips()) : String.valueOf(spell.pipCost()))
                + " pips, " + spell.accuracy() + "%, " + tr("gui.arcanum_realms.grimoire.minlevel") + " " + spell.minLevel());
        for (String line : wrap(SpellDescriber.describe(spell, french()), 190)) lines.add(line);
        lines.add(tr("gui.arcanum_realms.grimoire.add_hint"));

        int boxWidth = 0;
        for (String line : lines) boxWidth = Math.max(boxWidth, this.font.width(line));
        boxWidth += 8;
        int boxHeight = lines.size() * 10 + 6;
        int x = Math.min(mouseX + 8, this.width - boxWidth - 4);
        int y = Math.max(4, mouseY - boxHeight - 6);
        graphics.fill(x - 2, y - 2, x + boxWidth, y + boxHeight, 0xF0101010);
        graphics.fill(x - 2, y - 2, x + boxWidth, y - 1, 0xFFFFE066);
        int textY = y + 1;
        for (String line : lines) {
            graphics.drawString(this.font, line, x, textY, 0xFFE0E0E0);
            textY += 10;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (data == null) return super.mouseClicked(mouseX, mouseY, button);
        for (SpellRow row : learnedRows) {
            if (row.contains((int) mouseX, (int) mouseY)) {
                if (button == 0) {
                    if (deckSize() < deckLimit()) deck.merge(row.spell.id(), 1, Integer::sum);
                } else if (button == 1) {
                    deck.computeIfPresent(row.spell.id(), (key, value) -> value <= 1 ? null : value - 1);
                }
                return true;
            }
        }
        for (SpellRow row : deckRows) {
            if (row.contains((int) mouseX, (int) mouseY)) {
                if (button == 1 || button == 0) {
                    deck.computeIfPresent(row.spell.id(), (key, value) -> value <= 1 ? null : value - 1);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int listTop = 54;
        int listBottom = this.height - 42;
        int visible = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
        if (mouseX < this.width / 2) {
            int max = Math.max(0, learnedRows.size() + scrollLearned - visible);
            scrollLearned = (int) Math.max(0, Math.min(max, scrollLearned - delta));
        } else {
            int max = Math.max(0, deckRows.size() + scrollDeck - visible);
            scrollDeck = (int) Math.max(0, Math.min(max, scrollDeck - delta));
        }
        return true;
    }

    private List<String> wrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (this.font.width(candidate) <= maxWidth) {
                current = new StringBuilder(candidate);
            } else {
                if (!current.isEmpty()) lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    /** Une ligne cliquable du grimoire. */
    private static final class SpellRow {
        final int x;
        final int y;
        final Spell spell;
        final boolean learned;

        SpellRow(int x, int y, Spell spell, boolean learned) {
            this.x = x;
            this.y = y;
            this.spell = spell;
            this.learned = learned;
        }

        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + COLUMN_WIDTH && mouseY >= y - 1 && mouseY < y + ROW_HEIGHT - 1;
        }
    }
}
