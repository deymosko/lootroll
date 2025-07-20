package org.deymosko.lootroll.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.enums.VoteType;

import java.util.List;
import java.util.UUID;

public class VoteUIEntry {
    private final UUID voteId;
    private final List<ItemStack> items;
    private final long endTime;
    private int timerTicks;
    private HoverableImageButton needButton;
    private HoverableImageButton greedButton;
    private HoverableImageButton passButton;
    private int x;
    private int y;

    public VoteUIEntry(UUID voteId, List<ItemStack> items, long endTime) {
        this.voteId = voteId;
        this.items = items;
        this.endTime = endTime;
        this.timerTicks = (int)Math.ceil(Math.max(0, endTime - System.currentTimeMillis()) / 50.0);
    }

    public UUID getVoteId() {
        return voteId;
    }

    public HoverableImageButton getNeedButton() {
        return needButton;
    }

    public HoverableImageButton getGreedButton() {
        return greedButton;
    }

    public HoverableImageButton getPassButton() {
        return passButton;
    }

    public void initButtons(int x, int y, LootVoteScreen screen) {
        this.x = x;
        this.y = y;
        needButton = new HoverableImageButton(x + 117, y + 6, 16, 16,
                GuiTextures.NEED_BUTTON, GuiTextures.NEED_BUTTON_HOVER,
                () -> screen.vote(voteId, VoteType.NEED), "");
        greedButton = new HoverableImageButton(x + 116, y + 25, 17, 10,
                GuiTextures.GREED_BUTTON, GuiTextures.GREED_BUTTON_HOVER,
                () -> screen.vote(voteId, VoteType.GREED), "");
        passButton = new HoverableImageButton(x + 139, y + 6, 14, 14,
                GuiTextures.PASS_BUTTON, GuiTextures.PASS_BUTTON_HOVER,
                () -> screen.vote(voteId, VoteType.PASS), "");
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        if (needButton != null) {
            needButton.setX(x + 117);
            needButton.setY(y + 6);
        }
        if (greedButton != null) {
            greedButton.setX(x + 116);
            greedButton.setY(y + 25);
        }
        if (passButton != null) {
            passButton.setX(x + 139);
            passButton.setY(y + 6);
        }
    }

    public boolean tick() {
        timerTicks = (int)Math.ceil(Math.max(0, endTime - System.currentTimeMillis()) / 50.0);
        return timerTicks <= 0;
    }

    public void render(GuiGraphics gui, int mouseX, int mouseY, Font font) {
        ItemStack stack = items.get(0);

        RenderSystem.setShaderTexture(0, GuiTextures.LOOTFRAME);
        gui.blit(GuiTextures.LOOTFRAME, x, y, 0, 0, 160, 40, 160, 40);

        int itemX = x + 8;
        int itemY = y + 10;
        gui.renderItem(stack, itemX, itemY);
        if (mouseX >= itemX && mouseX <= itemX + 16 && mouseY >= itemY && mouseY <= itemY + 16) {
            gui.renderTooltip(font, stack, mouseX, mouseY);
        }
        LootVoteScreen.drawScaledString(gui, font, stack.getDisplayName().getString(), x + 33, y + 9, 0.7f, 0xFFFFFF);
        float progress = timerTicks / 600.0f;
        drawProgressBar(gui, progress, x + 6, y + 33, 104, 5, 0xFFB2CA5D);

        if (needButton.isMouseOver(mouseX, mouseY)) {
            gui.renderTooltip(font, Component.translatable("lootroll.gui.vote.need"), mouseX, mouseY);
        } else if (greedButton.isMouseOver(mouseX, mouseY)) {
            gui.renderTooltip(font, Component.translatable("lootroll.gui.vote.greed"), mouseX, mouseY);
        } else if (passButton.isMouseOver(mouseX, mouseY)) {
            gui.renderTooltip(font, Component.translatable("lootroll.gui.vote.pass"), mouseX, mouseY);
        }
    }

    private void drawProgressBar(GuiGraphics graphics, float progress, int x, int y, int width, int height, int color) {
        progress = Math.min(Math.max(progress, 0.0f), 1.0f);
        int filledWidth = (int) (width * progress);
        graphics.fill(x, y, x + filledWidth, y + height, color);
    }
}
