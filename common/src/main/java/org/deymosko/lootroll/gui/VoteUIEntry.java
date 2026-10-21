package org.deymosko.lootroll.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.enums.VoteType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** One roll in the vote screen: a 160x40 frame with the item, timer and Need/Greed/Pass buttons. */
public class VoteUIEntry {
    public static final int WIDTH = 160;
    public static final int HEIGHT = 40;

    // Button rectangles relative to the frame: x, y, width, height.
    private static final int[] NEED = {117, 6, 16, 16};
    private static final int[] GREED = {116, 25, 17, 10};
    private static final int[] PASS = {139, 6, 14, 14};
    private static final int ITEM_X = 8;
    private static final int ITEM_Y = 10;

    private final UUID voteId;
    private final ClientVoteCache.VoteData data;

    public VoteUIEntry(UUID voteId, ClientVoteCache.VoteData data) {
        this.voteId = voteId;
        this.data = data;
    }

    public UUID getVoteId() {
        return voteId;
    }

    public boolean allowsGreed() {
        return data.allowGreed();
    }

    public boolean isExpired() {
        return data.isExpired();
    }

    /** The button under the mouse for a frame drawn at (x, y), or null. */
    public VoteType buttonAt(int x, int y, double mouseX, double mouseY) {
        if (inside(NEED, x, y, mouseX, mouseY)) return VoteType.NEED;
        if (allowsGreed() && inside(GREED, x, y, mouseX, mouseY)) return VoteType.GREED;
        if (inside(PASS, x, y, mouseX, mouseY)) return VoteType.PASS;
        return null;
    }

    public boolean isOverItem(int x, int y, double mouseX, double mouseY) {
        return mouseX >= x + ITEM_X && mouseX < x + ITEM_X + 16 && mouseY >= y + ITEM_Y && mouseY < y + ITEM_Y + 16;
    }

    private static boolean inside(int[] r, int x, int y, double mouseX, double mouseY) {
        return mouseX >= x + r[0] && mouseX < x + r[0] + r[2] && mouseY >= y + r[1] && mouseY < y + r[1] + r[3];
    }

    /** @param hovered the button the mouse is over (only when the mouse is in the visible list area), or null */
    public void render(GuiGraphics gui, Font font, int x, int y, VoteType hovered) {
        List<ItemStack> items = data.items();
        ItemStack stack = items.get(0);

        gui.blit(GuiTextures.LOOTFRAME, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);

        gui.renderItem(stack, x + ITEM_X, y + ITEM_Y);
        gui.renderItemDecorations(font, stack, x + ITEM_X, y + ITEM_Y);

        Component name = items.size() > 1
                ? Component.translatable("lootroll.gui.lot", stack.getHoverName(), items.size() - 1)
                : stack.getHoverName();
        LootVoteScreen.drawScaledWrappedString(gui, font, name, x + 33, y + 9, 0.7f, rarityColor(stack), 80);

        drawProgressBar(gui, data.progress(), x + 6, y + 33, 104, 5);

        button(gui, NEED, x, y, hovered == VoteType.NEED ? GuiTextures.NEED_BUTTON_HOVER : GuiTextures.NEED_BUTTON);
        if (allowsGreed()) {
            button(gui, GREED, x, y, hovered == VoteType.GREED ? GuiTextures.GREED_BUTTON_HOVER : GuiTextures.GREED_BUTTON);
        }
        button(gui, PASS, x, y, hovered == VoteType.PASS ? GuiTextures.PASS_BUTTON_HOVER : GuiTextures.PASS_BUTTON);
    }

    /** Tooltip for the hovered part of the frame; draw outside any scissor. */
    public void renderTooltip(GuiGraphics gui, Font font, int x, int y, int mouseX, int mouseY) {
        VoteType button = buttonAt(x, y, mouseX, mouseY);
        if (button != null) {
            gui.renderTooltip(font, Component.translatable("lootroll.gui.vote." + button.name().toLowerCase()), mouseX, mouseY);
        } else if (isOverItem(x, y, mouseX, mouseY)) {
            List<ItemStack> items = data.items();
            if (items.size() == 1) {
                gui.renderTooltip(font, items.get(0), mouseX, mouseY);
            } else {
                List<Component> lines = new ArrayList<>();
                for (ItemStack s : items) {
                    lines.add(Component.literal(s.getCount() + "x ").append(s.getHoverName()).withStyle(s.getRarity().color));
                }
                gui.renderComponentTooltip(font, lines, mouseX, mouseY);
            }
        }
    }

    private static void button(GuiGraphics gui, int[] r, int x, int y, ResourceLocation texture) {
        gui.blit(texture, x + r[0], y + r[1], 0, 0, r[2], r[3], r[2], r[3]);
    }

    public static int rarityColor(ItemStack stack) {
        Integer color = stack.getRarity().color.getColor();
        return color != null ? color : 0xFFFFFF;
    }

    private static void drawProgressBar(GuiGraphics graphics, float progress, int x, int y, int width, int height) {
        progress = Math.min(Math.max(progress, 0.0f), 1.0f);
        int filledWidth = (int) (width * progress);

        if (filledWidth > 0) {
            graphics.blit(GuiTextures.TIMER_PROGRESS_BAR,
                    x, y,
                    0, 0,
                    filledWidth, height,
                    width, height
            );
        }
    }
}
