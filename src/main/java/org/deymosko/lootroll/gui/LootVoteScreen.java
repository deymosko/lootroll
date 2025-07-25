package org.deymosko.lootroll.gui;

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

    public LootVoteScreen() {
        super(Component.translatable("screen.lootroll.vote"));
    }

    @Override
    protected void init() {
        reloadEntries();
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
        int centerX = width / 2 - 80;
        int centerY = height / 2;
        int startY = centerY+20;
        int offset = 50;
        for (int i = 0; i < entries.size(); i++) {
            VoteUIEntry entry = entries.get(i);
            int y = startY + i * offset;
            entry.initButtons(centerX, y, this);
            addRenderableWidget(entry.getNeedButton());
            addRenderableWidget(entry.getGreedButton());
            addRenderableWidget(entry.getPassButton());
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < entries.size(); i++) {
            VoteUIEntry entry = entries.get(i);
            entry.render(gui, mouseX, mouseY, font);
        }
        super.render(gui, mouseX, mouseY, partialTick);
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
    public void renderBackground(GuiGraphics p_283688_) {}

    public static void drawScaledString(GuiGraphics guiGraphics, Font font, String text, float screenX, float screenY, float scale, int color) {
        var pose = guiGraphics.pose();
        pose.pushPose();
        float scaledX = screenX / scale;
        float scaledY = screenY / scale;
        pose.scale(scale, scale, 1.0f);
        guiGraphics.drawString(font, text, scaledX, scaledY, color, false);
        pose.popPose();
    }
    public static void drawScaledWrappedString(GuiGraphics guiGraphics, Font font, Component text,
                                               float screenX, float screenY, float scale,
                                               int color, int maxWidth) {
        var lines = font.split(text, (int) (maxWidth / scale));
        for (int i = 0; i < lines.size() && i < 2; i++) {
            var pose = guiGraphics.pose();
            pose.pushPose();
            float scaledX = screenX / scale;
            float scaledY = (screenY + i * font.lineHeight * scale) / scale;
            pose.scale(scale, scale, 1.0f);
            guiGraphics.drawString(font, lines.get(i), scaledX, scaledY, color, false);
            pose.popPose();
        }
    }
}
