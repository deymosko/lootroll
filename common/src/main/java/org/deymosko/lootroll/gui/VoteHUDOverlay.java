package org.deymosko.lootroll.gui;


import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.ClientConfig;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.events.input.Keybinds;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * WoW-style roll frames on the HUD: one compact card per active roll with the item,
 * a shrinking timer bar and the hotkeys to vote on the top card without opening a screen.
 */
public class VoteHUDOverlay {
    private static final int FRAME_WIDTH = 150;
    private static final int FRAME_HEIGHT = 28;
    private static final int GAP = 2;
    private static final int EDGE = 4;

    public static void render(GuiGraphics gui, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (!ClientConfig.HUD_ENABLED.get() || !ClientVoteCache.hasVote()) return;
        if (mc.level == null || mc.options.hideGui || mc.screen instanceof LootVoteScreen) return;

        Font font = mc.font;
        List<UUID> ids = new ArrayList<>(ClientVoteCache.getPendingVotes());
        int shown = Math.min(ids.size(), ClientConfig.HUD_MAX_FRAMES.get());
        int hidden = ids.size() - shown;

        List<Component> footer = new ArrayList<>();
        if (hidden > 0) {
            footer.add(Component.translatable("lootroll.hud.more", hidden).withStyle(ChatFormatting.GRAY));
        }
        if (ClientConfig.HUD_KEY_HINTS.get()) {
            footer.add(keyHints(ClientVoteCache.get(ids.get(0))));
        }

        int totalHeight = shown * (FRAME_HEIGHT + GAP) + footer.size() * (font.lineHeight + 1);
        ClientConfig.Anchor anchor = ClientConfig.HUD_ANCHOR.get();
        int x = switch (anchor) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> EDGE;
            case TOP_CENTER -> (gui.guiWidth() - FRAME_WIDTH) / 2;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> gui.guiWidth() - FRAME_WIDTH - EDGE;
        };
        int y = switch (anchor) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> EDGE;
            case CENTER_LEFT, CENTER_RIGHT -> (gui.guiHeight() - totalHeight) / 2;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> gui.guiHeight() - totalHeight - EDGE;
        };
        x += ClientConfig.HUD_OFFSET_X.get();
        y += ClientConfig.HUD_OFFSET_Y.get();

        for (int i = 0; i < shown; i++) {
            ClientVoteCache.VoteData data = ClientVoteCache.get(ids.get(i));
            if (data != null) renderFrame(gui, font, data, x, y, i == 0);
            y += FRAME_HEIGHT + GAP;
        }
        for (Component line : footer) {
            gui.drawString(font, line, x + 2, y, 0xFFFFFF, true);
            y += font.lineHeight + 1;
        }
    }

    private static void renderFrame(GuiGraphics gui, Font font, ClientVoteCache.VoteData data, int x, int y, boolean top) {
        ItemStack stack = data.items().get(0);
        int rarity = 0xFF000000 | VoteUIEntry.rarityColor(stack);

        gui.fill(x, y, x + FRAME_WIDTH, y + FRAME_HEIGHT, 0xC0101010);
        // Rarity-coloured border; the top card (the one hotkeys vote on) gets a brighter one.
        int border = top ? rarity : (rarity & 0x00FFFFFF) | 0x80000000;
        gui.fill(x, y, x + FRAME_WIDTH, y + 1, border);
        gui.fill(x, y + FRAME_HEIGHT - 1, x + FRAME_WIDTH, y + FRAME_HEIGHT, border);
        gui.fill(x, y, x + 1, y + FRAME_HEIGHT, border);
        gui.fill(x + FRAME_WIDTH - 1, y, x + FRAME_WIDTH, y + FRAME_HEIGHT, border);

        gui.renderItem(stack, x + 5, y + 5);
        gui.renderItemDecorations(font, stack, x + 5, y + 5);

        String name = stack.getHoverName().getString();
        if (data.items().size() > 1) name += " +" + (data.items().size() - 1);
        int textWidth = FRAME_WIDTH - 30;
        if (font.width(name) > textWidth) {
            name = font.plainSubstrByWidth(name, textWidth - font.width("...")) + "...";
        }
        gui.drawString(font, name, x + 25, y + 6, rarity, true);

        float progress = data.progress();
        int barX = x + 25;
        int barY = y + 19;
        int barWidth = FRAME_WIDTH - 31;
        gui.fill(barX, barY, barX + barWidth, barY + 3, 0xFF333333);
        gui.fill(barX, barY, barX + (int) (barWidth * progress), barY + 3, timerColor(progress));
    }

    /** Green while there's plenty of time, through yellow, to red at the end. */
    private static int timerColor(float progress) {
        int red = (int) (255 * Math.min(1.0f, 2.0f * (1.0f - progress)));
        int green = (int) (255 * Math.min(1.0f, 2.0f * progress));
        return 0xFF000000 | (red << 16) | (green << 8) | 0x20;
    }

    private static Component keyHints(ClientVoteCache.VoteData top) {
        MutableComponent hints = Component.empty().withStyle(ChatFormatting.GRAY);
        hint(hints, Keybinds.voteNeed, "lootroll.gui.vote.need");
        if (top != null && top.allowGreed()) hint(hints, Keybinds.voteGreed, "lootroll.gui.vote.greed");
        hint(hints, Keybinds.votePass, "lootroll.gui.vote.pass");
        hint(hints, Keybinds.openVoteMenu, "lootroll.hud.all");
        return hints;
    }

    private static void hint(MutableComponent hints, KeyMapping key, String label) {
        if (key.isUnbound()) return;
        if (!hints.getSiblings().isEmpty()) hints.append("  ");
        hints.append(Component.literal("[").append(key.getTranslatedKeyMessage()).append("] ").withStyle(ChatFormatting.WHITE))
                .append(Component.translatable(label));
    }
}
