package com.arcanumrealms.client.screen;

import com.arcanumrealms.client.ClientBattleState;
import com.arcanumrealms.network.NetworkHandler;
import com.arcanumrealms.network.packet.NpcInteractPacket;
import com.arcanumrealms.network.packet.NpcDialoguePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialogue d'un PNJ.
 *
 * <p>S'affiche comme un parchemin : le nom du PNJ, ses repliques, puis un
 * bouton par action possible (accepter une quete, la remettre, acheter).
 */
public class NpcDialogueScreen extends Screen {

    private static final int PANEL_WIDTH = 320;
    private static final int LINE_HEIGHT = 11;
    private static final int BUTTON_HEIGHT = 18;

    private final NpcDialoguePacket packet;
    private final List<Button> actionButtons = new ArrayList<>();

    public NpcDialogueScreen(NpcDialoguePacket packet) {
        super(Component.literal(packet.npcId()));
        this.packet = packet;
    }

    private boolean french() {
        return ClientBattleState.french();
    }

    @Override
    protected void init() {
        actionButtons.clear();
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int y = panelTop() + panelHeight() + 4;

        Button close = Button.builder(Component.translatable("gui.arcanum_realms.dialogue.close"),
                        button -> onClose())
                .bounds(panelLeft + PANEL_WIDTH - 70, panelTop() - 20, 70, BUTTON_HEIGHT)
                .build();
        addRenderableWidget(close);

        for (NpcDialoguePacket.Action action : packet.actions()) {
            String label = action.label();
            if (label.length() > 46) label = label.substring(0, 44) + "...";
            Button button = Button.builder(Component.literal(label), clicked -> choose(action))
                    .bounds(panelLeft, y, PANEL_WIDTH, BUTTON_HEIGHT)
                    .build();
            actionButtons.add(button);
            addRenderableWidget(button);
            y += BUTTON_HEIGHT + 3;
        }
    }

    private void choose(NpcDialoguePacket.Action action) {
        int interaction = switch (action.kind()) {
            case com.arcanumrealms.server.DialogueService.KIND_HAND_IN -> NpcInteractPacket.HAND_IN;
            case com.arcanumrealms.server.DialogueService.KIND_BUY -> NpcInteractPacket.BUY;
            default -> NpcInteractPacket.ACCEPT;
        };
        NetworkHandler.sendToServer(new NpcInteractPacket(packet.npcId(), interaction, action.id()));
        // Le serveur renverra le dialogue mis a jour ; on ferme si c'est un achat.
        if (interaction == NpcInteractPacket.BUY) {
            // le serveur rouvre la boutique apres l'achat
        } else {
            this.onClose();
        }
    }

    private int panelTop() {
        return 24;
    }

    private int panelHeight() {
        return Math.min(140, 20 + packet.lines().size() * LINE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int top = panelTop();
        int height = panelHeight();

        graphics.fill(panelLeft - 4, top - 4, panelLeft + PANEL_WIDTH + 4, top + height + 4, 0xCC101820);
        graphics.fill(panelLeft - 2, top - 2, panelLeft + PANEL_WIDTH + 2, top + height + 2, 0xFF2A3550);

        int y = top;
        List<String> lines = packet.lines();
        for (int i = 0; i < lines.size() && y < top + height - LINE_HEIGHT; i++) {
            boolean header = i == 0;
            for (FormattedCharSequence sequence : font.split(
                    Component.literal(lines.get(i)), PANEL_WIDTH - 12)) {
                if (y >= top + height - LINE_HEIGHT) break;
                graphics.drawString(font, sequence, panelLeft + 6, y,
                        header ? 0xFFE8C46A : 0xFFE8ECF5, true);
                y += LINE_HEIGHT;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        // Detail du bouton survole : objectifs et recompenses.
        for (int i = 0; i < actionButtons.size() && i < packet.actions().size(); i++) {
            Button button = actionButtons.get(i);
            if (!button.isHoveredOrFocused()) continue;
            NpcDialoguePacket.Action action = packet.actions().get(i);
            List<Component> tooltip = new ArrayList<>();
            if (!action.goals().isEmpty()) {
                tooltip.add(Component.translatable("gui.arcanum_realms.dialogue.goals"));
                action.goals().forEach(goal -> tooltip.add(Component.literal(" - " + goal)));
            }
            if (!action.rewards().isBlank()) {
                tooltip.add(Component.translatable("gui.arcanum_realms.dialogue.rewards"));
                tooltip.add(Component.literal(" - " + action.rewards()));
            }
            if (action.kind() == com.arcanumrealms.server.DialogueService.KIND_BUY) {
                tooltip.add(Component.translatable("gui.arcanum_realms.dialogue.buy"));
            }
            if (!tooltip.isEmpty()) graphics.renderTooltip(font, tooltip, mouseX, mouseY);
            break;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
