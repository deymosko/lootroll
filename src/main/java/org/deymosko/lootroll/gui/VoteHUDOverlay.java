package org.deymosko.lootroll.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
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

//        if (mc.player == null) {
//            Lootroll.LOGGER.warn("HUD not rendered: player is null");
//            return;
//        }
//
//        if (mc.level == null) {
//            Lootroll.LOGGER.warn("HUD not rendered: level is null");
//            return;
//        }
//
//        if (mc.screen != null) {
//            Lootroll.LOGGER.info("HUD not rendered: screen is open ({})", mc.screen.getClass().getSimpleName());
//            return;
//        }
//
        if (!ClientVoteCache.hasVote()) {
           // Lootroll.LOGGER.info("HUD not rendered: no active vote");
            return;
        }

        Lootroll.LOGGER.info("Rendering loot vote HUD");

        int x = screenWidth - 160;
        int y = 10;

        //guiGraphics.blit(GuiTextures.LOOTFRAME, x - 10, y - 5, 0, 0, 160, 32, 160, 32);

        guiGraphics.drawString(mc.font, "📦 Active loot vote", x, y, 0xFFFF55, false);
        guiGraphics.drawString(mc.font, "Press [G] to vote", x, y + 10, 0xFFFFFF, false);
    });

}





