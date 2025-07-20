package org.deymosko.lootroll.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.Lootroll;
import org.deymosko.lootroll.events.input.Keybinds;
import org.deymosko.lootroll.gui.LootVoteScreen;

@Mod.EventBusSubscriber(modid = Lootroll.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        while (Keybinds.openVoteMenu.consumeClick()) {
            if (ClientVoteCache.hasVote()) {
                mc.setScreen(new LootVoteScreen(ClientVoteCache.getCurrentId(), ClientVoteCache.getCurrentItem()));
            }
        }
    }
}
