package org.deymosko.lootroll.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.events.input.Keybinds;


public class VoteHUDOverlay {
    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        if (!ClientVoteCache.hasVote() || mc.level == null || mc.options.hideGui) return;

        int x = guiGraphics.guiWidth() - 96;
        int y = 0;

        long ticks = mc.level.getGameTime();
        int frame = (int)((ticks / 10) % 2);

        ResourceLocation texture = (frame == 0)
                ? GuiTextures.LOOTALERT_0
                : GuiTextures.LOOTALERT_1;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);

        guiGraphics.blit(texture, x, y, 0, 0, 96, 32, 96, 32);
        LootVoteScreen.drawScaledWrappedString(guiGraphics, mc.font, Component.translatable("lootroll.hud.active_vote"), x+15, y+6, 0.5f, 0xFFFFFF, 80);
        Component keyName = Keybinds.openVoteMenu.getTranslatedKeyMessage();
        Component pressToVote = Component.translatable("lootroll.hud.press_to_vote", keyName);
        LootVoteScreen.drawScaledWrappedString(guiGraphics, mc.font, pressToVote, x + 15, y + 16, 0.5f, 0xFFFFFF, 80);
    }


    public static void drawScaledString(GuiGraphics guiGraphics, Font font, String text, float screenX, float screenY, float scale, int color) {
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(screenX, screenY, 0.0f);
        pose.scale(scale, scale, 1.0f);
        guiGraphics.drawString(font, text, 0, 0, color, false);
        pose.popPose();
    }
}
