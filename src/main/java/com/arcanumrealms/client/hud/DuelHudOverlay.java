package com.arcanumrealms.client.hud;

import com.arcanumrealms.client.ClientBattleState;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.network.packet.BattleStatePacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Affichage permanent pendant un duel : pips en losanges, vie, round et rappel des touches.
 * Reprend l'allure du bandeau de pips de Wizard101.
 */
public final class DuelHudOverlay {

    private DuelHudOverlay() {}

    public static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        BattleStatePacket state = ClientBattleState.battle();
        if (state == null) return;

        BattleStatePacket.Actor self = null;
        for (BattleStatePacket.Actor actor : state.actors) {
            if (actor.self) {
                self = actor;
                break;
            }
        }

        int x = 8;
        int y = 8;

        // --- bandeau de pips en losanges
        if (self != null) {
            drawPipRow(graphics, self.pips, x, y);
            y += 22;
        }

        // --- vie du sorcier
        if (self != null) {
            int width = 120;
            int height = 8;
            graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xAA000000);
            graphics.fill(x, y, x + width, y + height, 0xFF3B0A0A);
            int filled = (int) (width * self.healthPercent());
            graphics.fill(x, y, x + filled, y + height, 0xFFD33A3A);
            graphics.drawString(mc.font, self.health + " / " + self.maxHealth, x + width + 6, y, 0xFFFFFF);
            y += 14;
        }

        // --- round et temps
        String phase = switch (state.phase) {
            case "PLANNING" -> tr("gui.arcanum_realms.phase.planning", "Choix des cartes");
            default -> tr("gui.arcanum_realms.phase.resolving", "Resolution");
        };
        String header = tr("gui.arcanum_realms.hud.round", "Round") + " " + state.round + " - " + phase;
        if ("PLANNING".equals(state.phase) && state.secondsLeft > 0) header += " (" + state.secondsLeft + "s)";
        graphics.drawString(mc.font, header, x, y, 0xFFE8D9A0);
        y += 12;

        if (!state.aura.isEmpty()) {
            graphics.drawString(mc.font, tr("gui.arcanum_realms.hud.aura", "Aura") + " : " + state.aura, x, y, 0xFF9AD0FF);
            y += 12;
        }

        // --- creatures adverses
        int enemyY = y + 4;
        for (BattleStatePacket.Actor actor : state.actors) {
            if (actor.self || !actor.player) continue;
            School school = safeSchool(actor.school);
            int color = school == null ? 0xFFFFFFFF : 0xFF000000 | school.color();
            String label = actor.name + " " + actor.health + "/" + actor.maxHealth;
            graphics.drawString(mc.font, label, x, enemyY, actor.defeated ? 0xFF808080 : color);
            int width = mc.font.width(label);
            graphics.fill(x, enemyY + 10, x + width, enemyY + 12, 0xFF301010);
            graphics.fill(x, enemyY + 10, x + (int) (width * actor.healthPercent()), enemyY + 12, 0xFFB03030);
            enemyY += 18;
        }

        boolean screenOpen = mc.screen instanceof com.arcanumrealms.client.screen.DuelScreen;
        if (!screenOpen) {
            graphics.drawString(mc.font, Component.translatable("gui.arcanum_realms.hud.hint"),
                    x, enemyY + 4, 0xFFB0B0B0);
        }
    }

    /** Dessine les 7 emplacements de pips sous forme de losanges (style Wizard101). */
    public static void drawPipRow(GuiGraphics graphics, List<Boolean> pips, int x, int y) {
        int slot = 16;
        for (int i = 0; i < 7; i++) {
            int cx = x + 8 + i * slot;
            int cy = y + 8;
            if (i < pips.size()) {
                boolean power = pips.get(i);
                int color = power ? 0xFFF2C14E : 0xFFD9D9D9;
                int edge = power ? 0xFF8A6A12 : 0xFF5C5C5C;
                drawDiamond(graphics, cx, cy, 7, edge);
                drawDiamond(graphics, cx, cy, 5, color);
            } else {
                drawDiamond(graphics, cx, cy, 7, 0xFF3A3A3A);
            }
        }
    }

    /** Petit losange dessine ligne par ligne (pas de texture necessaire). */
    public static void drawDiamond(GuiGraphics graphics, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int halfWidth = radius - Math.abs(dy);
            graphics.fill(cx - halfWidth, cy + dy, cx + halfWidth + 1, cy + dy + 1, color);
        }
    }

    private static School safeSchool(String id) {
        try {
            return School.byId(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String tr(String key, String fallback) {
        Component component = Component.translatable(key);
        String text = component.getString();
        return text.equals(key) ? fallback : text;
    }

    /** Non utilise : reserve pour de futurs effets. */
    static void noop(PoseStack stack) {}
}
