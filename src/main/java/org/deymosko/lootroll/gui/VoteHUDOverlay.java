package org.deymosko.lootroll.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.deymosko.lootroll.ClientVoteCache;



public class VoteHUDOverlay {
    private static final Minecraft mc = Minecraft.getInstance();

    public static void render(GuiGraphics gui) {
        if (mc.player == null || mc.level == null) return;
        if (!ClientVoteCache.hasVote()) return;

        int x = mc.getWindow().getGuiScaledWidth() - 160;
        int y = 10;

        gui.drawString(mc.font, Component.literal("📦 Active loot vote"), x, y, 0xFFFF55, false);
        gui.drawString(mc.font, Component.literal("Press [G] to vote"), x, y + 10, 0xFFFFFF, false);
    }
}





