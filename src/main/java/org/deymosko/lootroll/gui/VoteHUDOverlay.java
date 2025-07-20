package org.deymosko.lootroll.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.Lootroll;


public class VoteHUDOverlay{
    public static final IGuiOverlay HUD_WARN = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();

        if (!ClientVoteCache.hasVote()) return;

        int x = screenWidth - 96;
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

        drawScaledString(guiGraphics, mc.font,
                Component.translatable("lootroll.hud.active_vote").getString(),
                x+15, y+6, 0.5f, 0xFFFF55);
        drawScaledString(guiGraphics, mc.font,
                Component.translatable("lootroll.hud.press_to_vote").getString(),
                x+15, y+16, 0.5f, 0xFFFF55);
    });

    public static void drawScaledString(GuiGraphics guiGraphics, Font font, String text, float screenX, float screenY, float scale, int color) {
        var pose = guiGraphics.pose();
        pose.pushPose();
        float scaledX = screenX / scale;
        float scaledY = screenY / scale;
        pose.scale(scale, scale, 1.0f);
        guiGraphics.drawString(font, text, scaledX, scaledY, color, false);
        pose.popPose();
    }



}





