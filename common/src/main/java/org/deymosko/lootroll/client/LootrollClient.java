package org.deymosko.lootroll.client;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.events.input.Keybinds;
import org.deymosko.lootroll.gui.LootVoteScreen;
import org.deymosko.lootroll.gui.VoteHUDOverlay;
import org.deymosko.lootroll.network.Packets;

public final class LootrollClient {

    private LootrollClient() {}

    /** Client-only initialisation. Must never be called on a dedicated server. */
    public static void init()
    {
        Keybinds.register();
        Packets.registerClient();

        ClientTickEvent.CLIENT_POST.register(LootrollClient::onClientTick);
        ClientGuiEvent.RENDER_HUD.register(VoteHUDOverlay::render);
    }

    private static void onClientTick(Minecraft mc)
    {
        if (mc.player == null || mc.level == null) return;

        while (Keybinds.openVoteMenu.consumeClick()) {
            if (ClientVoteCache.hasVote()) {
                mc.setScreen(new LootVoteScreen());
            }
        }
    }
}
