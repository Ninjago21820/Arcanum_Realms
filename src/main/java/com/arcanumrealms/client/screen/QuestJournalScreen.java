package com.arcanumrealms.client.screen;

import com.arcanumrealms.client.ClientBattleState;
import com.arcanumrealms.client.ContentResolver;
import com.arcanumrealms.core.world.ContentLine;
import com.arcanumrealms.network.packet.QuestJournalPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Journal de quetes : les quetes actives, leurs objectifs et leur avancement.
 *
 * <p>Les lignes "[x]" sont terminees, "[ ]" restent a faire ; "[+]" signale une
 * quete prete a etre remise.
 */
public class QuestJournalScreen extends Screen {

    private static final int PANEL_WIDTH = 340;
    private static final int LINE_HEIGHT = 11;

    private final QuestJournalPacket packet;
    private int scroll;

    public QuestJournalScreen(QuestJournalPacket packet) {
        super(Component.translatable("gui.arcanum_realms.journal.title"));
        this.packet = packet;
    }

    private boolean french() {
        return ClientBattleState.french();
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.arcanum_realms.journal.close"),
                        button -> onClose())
                .bounds((this.width - PANEL_WIDTH) / 2 + PANEL_WIDTH - 70, 6, 70, 18)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = 30;
        int bottom = this.height - 12;

        graphics.fill(left - 6, top - 6, left + PANEL_WIDTH + 6, bottom + 6, 0xCC101820);
        graphics.fill(left - 4, top - 4, left + PANEL_WIDTH + 4, bottom + 4, 0xFF2A3550);

        graphics.drawString(font, this.title, left, 12, 0xFFE8C46A, true);

        List<ContentLine> lines = packet.lines();
        int y = top - scroll;
        for (ContentLine content : lines) {
            String line = ContentResolver.resolve(content);
            if (y >= top - LINE_HEIGHT && y <= bottom - LINE_HEIGHT) {
                int colour = 0xFFE8ECF5;
                if (line.startsWith("[+]")) colour = 0xFF7CD87C;
                else if (line.startsWith("[-]")) colour = 0xFFE8C46A;
                else if (line.startsWith("    [x]")) colour = 0xFF7CD87C;
                else if (line.startsWith("    [ ]")) colour = 0xFFB8C2D8;
                for (FormattedCharSequence sequence : font.split(Component.literal(line), PANEL_WIDTH - 10)) {
                    if (y > bottom - LINE_HEIGHT) break;
                    if (y >= top - LINE_HEIGHT) graphics.drawString(font, sequence, left + 4, y, colour, true);
                    y += LINE_HEIGHT;
                }
                continue;
            }
            y += LINE_HEIGHT;
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        if (lines.size() * LINE_HEIGHT > bottom - top) {
            graphics.drawString(font, Component.translatable("gui.arcanum_realms.journal.scroll").getString(),
                    left, this.height - 10, 0xFF8892A8, false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int max = Math.max(0, packet.lines().size() * LINE_HEIGHT - (this.height - 48));
        scroll = (int) Math.max(0, Math.min(max, scroll - delta * 12));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
