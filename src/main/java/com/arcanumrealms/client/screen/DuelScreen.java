package com.arcanumrealms.client.screen;

import com.arcanumrealms.client.ClientBattleState;
import com.arcanumrealms.client.ContentResolver;
import com.arcanumrealms.core.world.ContentKeys;
import com.arcanumrealms.client.hud.DuelHudOverlay;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.core.spell.SpellDescriber;
import com.arcanumrealms.network.NetworkHandler;
import com.arcanumrealms.network.packet.BattleActionPacket;
import com.arcanumrealms.network.packet.BattleStatePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interface de duel : les cartes en main, les adversaires, les alliees et le journal.
 *
 * <p>L'apparence suit celle de Wizard101 : cartes colorees par ecole, pips en losanges,
 * cible a designer du doigt, journal de combat a droite.
 */
public class DuelScreen extends Screen {

    private static final int CARD_WIDTH = 66;
    private static final int CARD_HEIGHT = 92;
    private static final int CARD_SPACING = 5;

    private BattleStatePacket state;
    private final List<CardRect> cardRects = new ArrayList<>();
    private final List<ActorRect> actorRects = new ArrayList<>();
    private Button passButton;
    private Button fleeButton;
    private final List<String> logLines = new ArrayList<>();

    public DuelScreen(BattleStatePacket state) {
        super(Component.translatable("gui.arcanum_realms.duel.title"));
        this.state = state;
    }

    public void setState(BattleStatePacket state) {
        this.state = state;
    }

    private boolean french() {
        return ClientBattleState.french();
    }

    private String tr(String key) {
        Component component = Component.translatable(key);
        String text = component.getString();
        return text.equals(key) ? key : text;
    }

    @Override
    protected void init() {
        super.init();
        this.passButton = Button.builder(Component.translatable("gui.arcanum_realms.button.pass"), button -> {
            NetworkHandler.sendToServer(BattleActionPacket.pass());
            ClientBattleState.clearSelection();
        }).bounds(this.width / 2 - 104, this.height - CARD_HEIGHT - 30, 60, 20).build();

        this.fleeButton = Button.builder(Component.translatable("gui.arcanum_realms.button.flee"), button -> {
            NetworkHandler.sendToServer(BattleActionPacket.flee());
            ClientBattleState.clearSelection();
        }).bounds(this.width / 2 + 44, this.height - CARD_HEIGHT - 30, 60, 20).build();

        addRenderableWidget(passButton);
        addRenderableWidget(fleeButton);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        buildLog();
        layout();

        // ---------------------------------------------------------- en-tete
        String header = tr("gui.arcanum_realms.duel.round") + " " + (state == null ? 1 : state.round);
        if (state != null) {
            header += " — " + ("PLANNING".equals(state.phase) ? tr("gui.arcanum_realms.phase.planning")
                    : tr("gui.arcanum_realms.phase.resolving"));
            if ("PLANNING".equals(state.phase) && state.secondsLeft > 0) header += " (" + state.secondsLeft + "s)";
        }
        graphics.drawCenteredString(this.font, header, this.width / 2, 6, 0xFFE8D9A0);

        if (state == null) {
            graphics.drawCenteredString(this.font, tr("gui.arcanum_realms.duel.waiting"),
                    this.width / 2, this.height / 2, 0xFFFFFFFF);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        // -------------------------------------------------------- acteurs
        for (ActorRect rect : actorRects) {
            BattleStatePacket.Actor actor = rect.actor;
            School school = safeSchool(actor.school);
            int schoolColor = school == null ? 0xFFFFFFFF : 0xFF000000 | school.color();
            boolean highlighted = rect.id.equals(ClientBattleState.selectedTarget());
            int border = highlighted ? 0xFFFFE066 : (actor.self ? 0xFF7080FF : 0xFF303030);
            int top = rect.y;
            int bottom = rect.y + rect.height;
            graphics.fill(rect.x - 1, top - 1, rect.x + rect.width + 1, bottom + 1, 0xCC101010);
            graphics.fill(rect.x, top, rect.x + rect.width, bottom, actor.defeated ? 0x88302020 : 0xBB202020);
            graphics.fill(rect.x, top, rect.x + rect.width, top + 1, border);
            graphics.fill(rect.x, bottom - 1, rect.x + rect.width, bottom, border);
            graphics.fill(rect.x, top, rect.x + 1, bottom, border);
            graphics.fill(rect.x + rect.width - 1, top, rect.x + rect.width, bottom, border);

            String name = actor.name + (actor.defeated ? " (" + tr("gui.arcanum_realms.state.defeated") + ")" : "");
            graphics.drawString(this.font, name, rect.x + 4, top + 3, actor.defeated ? 0xFF909090 : schoolColor);

            // barre de vie
            int barY = top + 14;
            int barWidth = rect.width - 8;
            graphics.fill(rect.x + 4, barY, rect.x + 4 + barWidth, barY + 6, 0xFF401010);
            graphics.fill(rect.x + 4, barY, rect.x + 4 + (int) (barWidth * actor.healthPercent()), barY + 6, 0xFFC03030);
            graphics.drawString(this.font, actor.health + "/" + actor.maxHealth, rect.x + 4, barY + 8, 0xFFE0E0E0);

            // pips
            DuelHudOverlay.drawPipRow(graphics, actor.pips, rect.x + 2, barY + 17);

            // effets actifs
            int statusY = top + 40;
            for (String status : actor.statuses) {
                if (statusY > bottom - 8) break;
                graphics.drawString(this.font, status, rect.x + 4, statusY, 0xFFB8E0B8);
                statusY += 9;
            }
        }

        // ---------------------------------------------------------- cartes
        int selected = ClientBattleState.selectedCard();
        for (CardRect rect : cardRects) {
            BattleStatePacket.Card card = rect.card;
            Spell spell = SpellBook.get(card.spellId);
            School school = spell.school();
            int schoolColor = 0xFF000000 | school.color();
            boolean affordable = card.affordable && "PLANNING".equals(state.phase) && state.canAct;
            boolean isSelected = rect.index == selected;

            int base = affordable ? 0xE0202020 : 0xA0202020;
            graphics.fill(rect.x, rect.y, rect.x + CARD_WIDTH, rect.y + CARD_HEIGHT, base);
            int border = isSelected ? 0xFFFFE066 : schoolColor;
            int borderWidth = isSelected ? 2 : 1;
            for (int i = 0; i < borderWidth; i++) {
                graphics.fill(rect.x + i, rect.y + i, rect.x + CARD_WIDTH - i, rect.y + i + 1, border);
                graphics.fill(rect.x + i, rect.y + CARD_HEIGHT - i - 1, rect.x + CARD_WIDTH - i, rect.y + CARD_HEIGHT - i, border);
                graphics.fill(rect.x + i, rect.y + i, rect.x + i + 1, rect.y + CARD_HEIGHT - i, border);
                graphics.fill(rect.x + CARD_WIDTH - i - 1, rect.y + i, rect.x + CARD_WIDTH - i, rect.y + CARD_HEIGHT - i, border);
            }
            // bandeau d'ecole
            graphics.fill(rect.x + 2, rect.y + 2, rect.x + CARD_WIDTH - 2, rect.y + 12, (affordable ? 0xFF000000 : 0x80000000) | school.color());

            String cost = spell.variablePips()
                    ? tr("gui.arcanum_realms.card.variable") + " " + Math.max(spell.minPips(), ClientBattleState.pipsToSpend())
                    : String.valueOf(spell.pipCost());
            graphics.drawString(this.font, cost + " " + tr("gui.arcanum_realms.card.pips"), rect.x + 4, rect.y + 3, 0xFF101010);

            // nom (sur plusieurs lignes)
            List<String> nameLines = wrap(ContentResolver.resolve(ContentKeys.spellName(spell, french())), CARD_WIDTH - 8);
            int textY = rect.y + 15;
            for (String line : nameLines) {
                graphics.drawString(this.font, line, rect.x + 4, textY, affordable ? 0xFFFFFFFF : 0xFF909090);
                textY += 9;
            }

            // description
            int descColor = affordable ? 0xFFC8C8C8 : 0xFF808080;
            List<String> descLines = wrap(SpellDescriber.describe(spell, french()), CARD_WIDTH - 8);
            int shown = 0;
            for (String line : descLines) {
                if (textY > rect.y + CARD_HEIGHT - 22) break;
                graphics.drawString(this.font, line, rect.x + 4, textY, descColor);
                textY += 9;
                shown++;
            }

            // precision
            graphics.drawString(this.font, spell.accuracy() + "%", rect.x + 4, rect.y + CARD_HEIGHT - 20, 0xFFA0C8FF);
            String schoolName = school.display(french());
            if (this.font.width(schoolName) < CARD_WIDTH - 30) {
                graphics.drawString(this.font, schoolName, rect.x + 4, rect.y + CARD_HEIGHT - 10, schoolColor);
            }
            if (!card.affordable) {
                graphics.drawString(this.font, tr("gui.arcanum_realms.card.too_expensive"),
                        rect.x + 4, rect.y + CARD_HEIGHT - 20, 0xFFFF7070);
            }
        }

        // --------------------------------------------------------- journal
        renderLog(graphics);

        // --------------------------------------------------------- boutons
        passButton.active = state.canAct;
        fleeButton.active = state.canAct && state.canFlee;
        super.render(graphics, mouseX, mouseY, partialTick);

        // ----------------------------------------------------- info survol
        for (CardRect rect : cardRects) {
            if (mouseX >= rect.x && mouseX < rect.x + CARD_WIDTH && mouseY >= rect.y && mouseY < rect.y + CARD_HEIGHT) {
                Spell spell = SpellBook.get(rect.card.spellId);
                List<String> lines = new ArrayList<>();
                lines.add(ContentResolver.resolve(ContentKeys.spellName(spell, french())) + " — " + spell.school().display(french()));
                lines.add((spell.variablePips() ? ("X+" + spell.minPips()) : String.valueOf(spell.pipCost()))
                        + " " + tr("gui.arcanum_realms.card.pips") + ", " + spell.accuracy() + "%");
                lines.addAll(wrap(SpellDescriber.describe(spell, french()), 200));
                lines.add(tr("gui.arcanum_realms.card.hint"));
                drawInfoBox(graphics, lines, mouseX, mouseY);
                break;
            }
        }
    }

    private void renderLog(GuiGraphics graphics) {
        int x = this.width - 156;
        int y = 24;
        int width = 150;
        graphics.fill(x - 2, y - 2, x + width, y + 128, 0x99202020);
        graphics.drawString(this.font, tr("gui.arcanum_realms.duel.log"), x, y, 0xFFE8D9A0);
        int lineY = y + 12;
        List<String> lines = logLines;
        int start = Math.max(0, lines.size() - 11);
        for (int i = start; i < lines.size(); i++) {
            for (String wrapped : wrap(lines.get(i), width - 4)) {
                if (lineY > y + 124) break;
                graphics.drawString(this.font, wrapped, x, lineY, 0xFFD0D0D0);
                lineY += 9;
            }
        }
    }

    private void drawInfoBox(GuiGraphics graphics, List<String> lines, int mouseX, int mouseY) {
        int maxWidth = 0;
        for (String line : lines) maxWidth = Math.max(maxWidth, this.font.width(line));
        int boxWidth = maxWidth + 8;
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
        if (state == null) return super.mouseClicked(mouseX, mouseY, button);

        // cartes
        for (CardRect rect : cardRects) {
            if (mouseX >= rect.x && mouseX < rect.x + CARD_WIDTH && mouseY >= rect.y && mouseY < rect.y + CARD_HEIGHT) {
                if (button == 1) {
                    NetworkHandler.sendToServer(BattleActionPacket.discard(rect.index));
                    ClientBattleState.clearSelection();
                    return true;
                }
                if (!rect.card.affordable || !state.canAct) return true;
                Spell spell = SpellBook.get(rect.card.spellId);
                ClientBattleState.selectCard(rect.index);
                if (spell.variablePips()) {
                    ClientBattleState.setPipsToSpend(Math.max(spell.minPips(), ClientBattleState.pipsToSpend()));
                }
                if (!spell.targetMode().affectsEnemies()) {
                    cast(rect.index, null);
                }
                return true;
            }
        }

        // cibles
        int selected = ClientBattleState.selectedCard();
        if (selected >= 0) {
            for (ActorRect rect : actorRects) {
                if (rect.actor.defeated) continue;
                if (mouseX >= rect.x && mouseX < rect.x + rect.width && mouseY >= rect.y && mouseY < rect.y + rect.height) {
                    cast(selected, rect.actor.id);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int selected = ClientBattleState.selectedCard();
        if (selected >= 0 && state != null) {
            Spell spell = null;
            for (CardRect rect : cardRects) {
                if (rect.index == selected) {
                    spell = SpellBook.get(rect.card.spellId);
                    break;
                }
            }
            if (spell != null && spell.variablePips()) {
                int value = ClientBattleState.pipsToSpend() + (delta > 0 ? 1 : -1);
                ClientBattleState.setPipsToSpend(Math.max(spell.minPips(), Math.min(spell.maxPips(), value)));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void cast(int handIndex, String targetId) {
        int pips = ClientBattleState.pipsToSpend();
        NetworkHandler.sendToServer(BattleActionPacket.cast(handIndex, targetId, pips));
        ClientBattleState.clearSelection();
    }

    /** Calcule la position des cartes et des panneaux pour la taille d'ecran courante. */
    private void layout() {
        cardRects.clear();
        actorRects.clear();

        // cartes en main (en eventail en bas de l'ecran)
        List<BattleStatePacket.Card> hand = state == null ? List.of() : state.hand;
        int total = hand.size();
        int totalWidth = total * CARD_WIDTH + Math.max(0, total - 1) * CARD_SPACING;
        int startX = (this.width - totalWidth) / 2;
        int cardY = this.height - CARD_HEIGHT - 26;
        for (int i = 0; i < total; i++) {
            cardRects.add(new CardRect(startX + i * (CARD_WIDTH + CARD_SPACING), cardY, hand.get(i)));
        }

        if (state == null) return;

        // ennemis en haut, allies en bas
        List<BattleStatePacket.Actor> enemies = state.actors.stream().filter(a -> !a.player).toList();
        List<BattleStatePacket.Actor> allies = state.actors.stream().filter(a -> a.player).toList();
        placeActors(actorRects, enemies, 20, true);
        placeActors(actorRects, allies, cardY - 62, false);
    }

    private void placeActors(List<ActorRect> out, List<BattleStatePacket.Actor> actors, int y, boolean enemy) {
        int width = 140;
        int height = 62;
        int spacing = 6;
        int total = actors.size();
        int totalWidth = total * width + Math.max(0, total - 1) * spacing;
        int startX = (this.width - totalWidth) / 2;
        for (int i = 0; i < total; i++) {
            out.add(new ActorRect(startX + i * (width + spacing), y, width, height, actors.get(i)));
        }
    }

    /** Reconstruit le journal de combat a partir des evenements recus. */
    private void buildLog() {
        logLines.clear();
        if (state == null) return;
        for (BattleStatePacket.Line line : state.events) {
            logLines.add(french() ? line.fr : line.en);
        }
    }

    /** Decoupe un texte pour l'afficher sur la largeur donnee. */
    private List<String> wrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
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

    private static School safeSchool(String id) {
        try {
            return School.byId(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Rectangle d'une carte dans l'interface. */
    private static final class CardRect {
        final int x;
        final int y;
        final BattleStatePacket.Card card;
        final int index;

        CardRect(int x, int y, BattleStatePacket.Card card) {
            this.x = x;
            this.y = y;
            this.card = card;
            this.index = card.index;
        }
    }

    /** Rectangle d'un combattant dans l'interface. */
    private static final class ActorRect {
        final int x;
        final int y;
        final int width;
        final int height;
        final BattleStatePacket.Actor actor;
        final String id;

        ActorRect(int x, int y, int width, int height, BattleStatePacket.Actor actor) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.actor = actor;
            this.id = actor.id;
        }
    }

    /** Couleur d'ecole en texte (utilisee par les journaux de debug). */
    public static String schoolLabel(School school) {
        return school.name().toLowerCase(Locale.ROOT);
    }
}
