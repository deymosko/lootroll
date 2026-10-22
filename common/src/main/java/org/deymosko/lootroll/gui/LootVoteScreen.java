package org.deymosko.lootroll.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.client.LootrollClient;
import org.deymosko.lootroll.enums.VoteType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Scrollable list of every active roll. The list takes as much screen height as it needs
 * (up to what fits), and only rows inside the visible area react to the mouse.
 */
public class LootVoteScreen extends Screen {
    private static final int ROW = VoteUIEntry.HEIGHT + 4;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int PANEL_WIDTH = VoteUIEntry.WIDTH + 4 + SCROLLBAR_WIDTH;
    private static final int HEADER_HEIGHT = 38;
    private static final int MARGIN = 16;

    private final List<VoteUIEntry> entries = new ArrayList<>();

    private int listX;
    private int listTop;
    private int listHeight;
    private double scroll;
    private double targetScroll;
    private boolean draggingScrollbar;
    private double dragStartMouseY;
    private double dragStartScroll;

    private Button greedAllButton;

    public LootVoteScreen() {
        super(Component.translatable("screen.lootroll.vote"));
    }

    @Override
    protected void init() {
        syncEntries();
        layout();

        int buttonWidth = (PANEL_WIDTH - 8) / 3;
        int buttonY = listTop - HEADER_HEIGHT + 14;
        addRenderableWidget(Button.builder(Component.translatable("lootroll.gui.need_all"), b -> voteAll(VoteType.NEED))
                .bounds(listX, buttonY, buttonWidth, 20).build());
        greedAllButton = addRenderableWidget(Button.builder(Component.translatable("lootroll.gui.greed_all"), b -> voteAll(VoteType.GREED))
                .bounds(listX + buttonWidth + 4, buttonY, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("lootroll.gui.pass_all"), b -> voteAll(VoteType.PASS))
                .bounds(listX + 2 * (buttonWidth + 4), buttonY, buttonWidth, 20).build());
        greedAllButton.active = entries.stream().anyMatch(VoteUIEntry::allowsGreed);
    }

    /** Sizes the list to its content, capped to the screen, and centres the panel. */
    private void layout() {
        int maxListHeight = Math.max(ROW, height - HEADER_HEIGHT - 2 * MARGIN);
        listHeight = Mth.clamp(contentHeight(), ROW - 4, maxListHeight);
        listX = (width - PANEL_WIDTH) / 2;
        listTop = (height - HEADER_HEIGHT - listHeight) / 2 + HEADER_HEIGHT;
        targetScroll = Mth.clamp(targetScroll, 0, maxScroll());
        scroll = Mth.clamp(scroll, 0, maxScroll());
    }

    private int contentHeight() {
        return Math.max(0, entries.size() * ROW - 4);
    }

    private double maxScroll() {
        return Math.max(0, contentHeight() - listHeight);
    }

    /** Mirrors the client cache: drops finished rolls and appends new ones, keeping order. */
    private void syncEntries() {
        List<UUID> pending = List.copyOf(ClientVoteCache.getPendingVotes());
        entries.removeIf(e -> !pending.contains(e.getVoteId()) || e.isExpired());
        for (UUID id : pending) {
            if (entries.stream().noneMatch(e -> e.getVoteId().equals(id))) {
                ClientVoteCache.VoteData data = ClientVoteCache.get(id);
                if (data != null && !data.isExpired()) {
                    entries.add(new VoteUIEntry(id, data));
                }
            }
        }
    }

    @Override
    public void tick() {
        int before = entries.size();
        syncEntries();
        if (entries.isEmpty()) {
            onClose();
        } else if (entries.size() != before) {
            rebuildWidgets();
        }
    }

    private void vote(VoteUIEntry entry, VoteType type) {
        LootrollClient.vote(entry.getVoteId(), type);
        entries.remove(entry);
        afterVote();
    }

    private void voteAll(VoteType type) {
        for (VoteUIEntry entry : List.copyOf(entries)) {
            // Rolls without Greed keep waiting rather than being silently passed.
            if (type == VoteType.GREED && !entry.allowsGreed()) continue;
            LootrollClient.vote(entry.getVoteId(), type);
            entries.remove(entry);
        }
        afterVote();
    }

    private void afterVote() {
        if (entries.isEmpty()) {
            onClose();
        } else {
            rebuildWidgets();
        }
    }

    private int entryY(int index) {
        return listTop + index * ROW - (int) Math.round(scroll);
    }

    private boolean isInList(double mouseX, double mouseY) {
        return mouseX >= listX && mouseX < listX + VoteUIEntry.WIDTH && mouseY >= listTop && mouseY < listTop + listHeight;
    }

    /** Index of the row under the mouse, only within the visible list area; -1 if none. */
    private int entryAt(double mouseX, double mouseY) {
        if (!isInList(mouseX, mouseY)) return -1;
        for (int i = 0; i < entries.size(); i++) {
            int y = entryY(i);
            if (mouseY >= y && mouseY < y + VoteUIEntry.HEIGHT) return i;
        }
        return -1;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        // Ease towards the scroll target for smooth wheel scrolling.
        scroll += (targetScroll - scroll) * 0.4;
        if (Math.abs(targetScroll - scroll) < 0.5) scroll = targetScroll;

        int panelTop = listTop - HEADER_HEIGHT;
        gui.fill(listX - 4, panelTop - 4, listX + PANEL_WIDTH + 4, listTop + listHeight + 4, 0xA0000000);
        gui.drawCenteredString(font, Component.translatable("lootroll.gui.title", entries.size()), listX + PANEL_WIDTH / 2, panelTop + 2, 0xFFFFFF);

        int hoveredIndex = entryAt(mouseX, mouseY);

        gui.enableScissor(listX, listTop, listX + VoteUIEntry.WIDTH, listTop + listHeight);
        for (int i = 0; i < entries.size(); i++) {
            int y = entryY(i);
            if (y + VoteUIEntry.HEIGHT < listTop || y > listTop + listHeight) continue;
            VoteUIEntry entry = entries.get(i);
            VoteType hovered = i == hoveredIndex ? entry.buttonAt(listX, y, mouseX, mouseY) : null;
            entry.render(gui, font, listX, y, hovered);
        }
        gui.disableScissor();

        renderScrollbar(gui);
        super.render(gui, mouseX, mouseY, partialTick);

        // Tooltips last and outside the scissor so they aren't clipped by the list.
        if (hoveredIndex >= 0) {
            entries.get(hoveredIndex).renderTooltip(gui, font, listX, entryY(hoveredIndex), mouseX, mouseY);
        }
    }

    private void renderScrollbar(GuiGraphics gui) {
        if (maxScroll() <= 0) return;
        int x = listX + VoteUIEntry.WIDTH + 4;
        gui.fill(x, listTop, x + SCROLLBAR_WIDTH, listTop + listHeight, 0xFF222222);
        int thumbHeight = thumbHeight();
        int thumbY = listTop + (int) ((listHeight - thumbHeight) * (scroll / maxScroll()));
        gui.fill(x, thumbY, x + SCROLLBAR_WIDTH, thumbY + thumbHeight, draggingScrollbar ? 0xFFCCCCCC : 0xFF888888);
    }

    private int thumbHeight() {
        return Math.max(16, (int) ((long) listHeight * listHeight / Math.max(1, contentHeight())));
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int x = listX + VoteUIEntry.WIDTH + 4;
        return maxScroll() > 0 && mouseX >= x - 1 && mouseX < x + SCROLLBAR_WIDTH + 1 && mouseY >= listTop && mouseY < listTop + listHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isOverScrollbar(mouseX, mouseY)) {
                draggingScrollbar = true;
                dragStartMouseY = mouseY;
                dragStartScroll = targetScroll;
                return true;
            }
            int index = entryAt(mouseX, mouseY);
            if (index >= 0) {
                VoteUIEntry entry = entries.get(index);
                VoteType type = entry.buttonAt(listX, entryY(index), mouseX, mouseY);
                if (type != null) {
                    vote(entry, type);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (draggingScrollbar) {
            double trackRange = listHeight - thumbHeight();
            if (trackRange > 0) {
                targetScroll = Mth.clamp(dragStartScroll + (mouseY - dragStartMouseY) * maxScroll() / trackRange, 0, maxScroll());
                scroll = targetScroll;
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll() <= 0) return false;
        targetScroll = Mth.clamp(targetScroll - scrollY * ROW / 2.0, 0, maxScroll());
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Keep the world visible behind the list.
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

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
