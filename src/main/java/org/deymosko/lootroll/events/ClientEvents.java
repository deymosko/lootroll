package org.deymosko.lootroll.events;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.deymosko.lootroll.Lootroll;
import org.deymosko.lootroll.events.input.Keybinds;
import org.deymosko.lootroll.gui.VoteHUDOverlay;

@Mod.EventBusSubscriber(modid = Lootroll.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(Keybinds::init);
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        Keybinds.registerKeys(event);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("loot_vote", (guiGraphics, partialTick, width, height) -> {
            VoteHUDOverlay.render(guiGraphics);
        });
    }
}
