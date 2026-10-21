package org.deymosko.lootroll.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.enums.VoteType;
import org.deymosko.lootroll.network.Packets;
import org.deymosko.lootroll.network.c2s.VoteC2SPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LootVoteScreen extends Screen {
    private final List<VoteUIEntry> entries = new ArrayList<>();
    public static int centerX;
    public static int centerY;
    public int startY;
    private int scrollbarX;
    private int scrollbarY;
    private int scrollbarHeight;
    private int contentHeight; // висота всього списку
    private int visibleHeight; // висота видимої частини
    private boolean draggingScrollbar;
    private int dragOffset = 0;

    public LootVoteScreen() {
        super(Component.translatable("screen.lootroll.vote"));
    }

    @Override
    protected void init() {
        centerX = width / 2 - 80;
        centerY = height / 2;
        startY = centerY+20;

        reloadEntries();
        visibleHeight = 180;
        contentHeight = entries.size() * 50;

        scrollbarX = centerX + 165;
        scrollbarY = centerY+20;
        scrollbarHeight = (int)((float)visibleHeight / contentHeight * visibleHeight);
    }

    private void reloadEntries() {
        entries.clear();
        for (UUID id : ClientVoteCache.getPendingVotes()) {
            ClientVoteCache.VoteData data = ClientVoteCache.get(id);
            if (data != null) {
                VoteUIEntry entry = new VoteUIEntry(id, data.items(), data.endTime());
                entries.add(entry);
            }
        }
        layoutEntries();
    }

    private void layoutEntries() {
        clearWidgets();
        int offset = 50;
        for (int i = 0; i < entries.size(); i++) {
            VoteUIEntry entry = entries.get(i);
            int y = startY + i * offset;
            entry.initButtons(centerX, y, this);
        }
        scrollbarHeight = (int)((float)visibleHeight / contentHeight * visibleHeight);
    }


    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        if (contentHeight > 0 && contentHeight > visibleHeight) {
            scrollbarHeight = (int)((float)visibleHeight / contentHeight * visibleHeight);
        } else {
            scrollbarHeight = visibleHeight;
        }
        Font font = Minecraft.getInstance().font;

        int scissorX = centerX-80;
        int scissorY = centerY+20;
        int scissorWidth = 320;
        int scissorHeight = 180;

        int scale = (int) Minecraft.getInstance().getWindow().getGuiScale();
        RenderSystem.enableScissor(
                scissorX * scale,
                (Minecraft.getInstance().getWindow().getHeight() - (scissorY + scissorHeight) * scale),
                scissorWidth * scale,
                scissorHeight * scale
        );

        for (VoteUIEntry entry : entries) {
            entry.render(gui, mouseX, mouseY, font, partialTick);
        }

        RenderSystem.disableScissor();
        // ---- SCROLLBAR ----
        int barMaxY = scrollbarY + visibleHeight - scrollbarHeight;
        int barY = scrollbarY;
        if (contentHeight > visibleHeight) {
            float scrollRatio = (float)(startY - centerY - 20) / (contentHeight - visibleHeight);
            barY = scrollbarY - (int)(scrollRatio * (visibleHeight - scrollbarHeight));
        }


        gui.fill(scrollbarX, scrollbarY, scrollbarX + 5, scrollbarY + visibleHeight, 0xFF222222);


        gui.fill(scrollbarX, barY, scrollbarX + 5, barY + scrollbarHeight, 0xFF888888);

        super.render(gui, mouseX, mouseY, partialTick);
    }


    public void vote(UUID id, VoteType type) {
        Packets.sendToServer(new VoteC2SPacket(id, type));
        ClientVoteCache.remove(id);
        entries.removeIf(e -> e.getVoteId().equals(id));
        if (entries.isEmpty()) {
            Minecraft.getInstance().setScreen(null);
        } else {
            layoutEntries();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        List<VoteUIEntry> expired = new ArrayList<>();
        for (VoteUIEntry entry : entries) {
            if (entry.tick()) {
                expired.add(entry);
            }
        }
        for (VoteUIEntry e : expired) {
            vote(e.getVoteId(), VoteType.PASS);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button == 0)
        {
            if(isOverScrollbar(mouseX, mouseY))
            {
                draggingScrollbar = true;
                dragOffset = (int) mouseY - getScrollBarY();
                return true;
            }
            for (VoteUIEntry entry : entries) {
                if (entry.getNeedButton().mouseClicked(mouseX, mouseY, button)) return true;
                if (entry.getGreedButton().mouseClicked(mouseX, mouseY, button)) return true;
                if (entry.getPassButton().mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (draggingScrollbar) {
            float scrollRange = contentHeight - visibleHeight;
            float scrollbarRange = visibleHeight - scrollbarHeight;
            if (scrollbarRange <= 0) return true;

            int newBarY = (int) mouseY - dragOffset;
            int clampedBarY = Math.max(scrollbarY, Math.min(newBarY, scrollbarY + visibleHeight - scrollbarHeight));

            float ratio = (float)(clampedBarY - scrollbarY) / scrollbarRange;
            startY = (int)(centerY + 20 + (1.0f - ratio) * scrollRange);


            layoutEntries();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(button == 0 && draggingScrollbar)
        {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int scrollAmount = (int)(delta * 12);

        int minY = centerY + 20;
        int maxY = centerY + 20 + (contentHeight - visibleHeight);

        startY = Math.max(minY - (contentHeight - visibleHeight), Math.min(startY + scrollAmount, minY));
        layoutEntries();
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics p_283688_) {}

    private boolean isOverScrollbar(double mouseX, double mouseY)
    {
        int barY = getScrollBarY();
        return mouseX >= scrollbarX && mouseX <= scrollbarX + 5 &&
                mouseY >= barY && mouseY <= barY + scrollbarHeight;

    }
    private int getScrollBarY()
    {
        if(contentHeight <= visibleHeight) return scrollbarY;

        float scrollbarRatio = (float)(startY - (centerY + 20)) / (contentHeight - visibleHeight);
        return scrollbarY + (int)(scrollbarRatio * (visibleHeight - scrollbarHeight));
    }
    public static void drawScaledWrappedString(GuiGraphics guiGraphics, Font font, Component text,
                                               float screenX, float screenY, float scale,
                                               int color, int maxWidth) {
        var lines = font.split(text, (int) (maxWidth / scale));
        for (int i = 0; i < lines.size() && i < 2; i++) {
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(screenX, screenY + i * font.lineHeight * scale, 0.0f);
            pose.scale(scale, scale, 1.0f);
            guiGraphics.drawString(font, lines.get(i), 0, 0, color, false);
            pose.popPose();
        }
    }
}
