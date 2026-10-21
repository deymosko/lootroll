package org.deymosko.lootroll.client;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.ClientConfig;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.enums.VoteType;
import org.deymosko.lootroll.events.input.Keybinds;
import org.deymosko.lootroll.gui.LootVoteScreen;
import org.deymosko.lootroll.gui.LootWonToast;
import org.deymosko.lootroll.gui.VoteHUDOverlay;
import org.deymosko.lootroll.network.Packets;
import org.deymosko.lootroll.network.c2s.VoteC2SPacket;

import java.util.List;
import java.util.UUID;

public final class LootrollClient {

    private LootrollClient() {}

    /** Client-only initialisation. Must never be called on a dedicated server. */
    public static void init()
    {
        Keybinds.register();
        Packets.registerClient();

        ClientTickEvent.CLIENT_POST.register(LootrollClient::onClientTick);
        ClientGuiEvent.RENDER_HUD.register(VoteHUDOverlay::render);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> ClientVoteCache.clear());
    }

    private static void onClientTick(Minecraft mc)
    {
        if (mc.player == null || mc.level == null) return;

        ClientVoteCache.removeExpired();

        while (Keybinds.openVoteMenu.consumeClick()) {
            if (ClientVoteCache.hasVote()) {
                mc.setScreen(new LootVoteScreen());
            }
        }
        while (Keybinds.voteNeed.consumeClick()) voteOnTop(VoteType.NEED);
        while (Keybinds.voteGreed.consumeClick()) voteOnTop(VoteType.GREED);
        while (Keybinds.votePass.consumeClick()) voteOnTop(VoteType.PASS);
    }

    /** Hotkey vote on the oldest active roll, i.e. the top HUD frame. */
    private static void voteOnTop(VoteType type)
    {
        UUID id = ClientVoteCache.getFirst();
        if (id == null) return;
        ClientVoteCache.VoteData data = ClientVoteCache.get(id);
        if (type == VoteType.GREED && !data.allowGreed()) return;
        vote(id, type);
    }

    public static void vote(UUID id, VoteType type)
    {
        Packets.sendToServer(new VoteC2SPacket(id, type));
        ClientVoteCache.remove(id);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    public static void onVoteStart(UUID id, List<ItemStack> items, int durationTicks, int remainingTicks, boolean allowGreed)
    {
        if (!ClientVoteCache.add(id, items, durationTicks, remainingTicks, allowGreed)) return;

        Minecraft mc = Minecraft.getInstance();
        if (ClientConfig.PLAY_SOUND.get()) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8f, 0.6f));
        }
        if (ClientConfig.OPEN_SCREEN_ON_VOTE.get() && mc.screen == null) {
            mc.setScreen(new LootVoteScreen());
        }
    }

    /** @param won whether this player received the items */
    public static void onVoteEnd(UUID id, boolean won, List<ItemStack> items)
    {
        ClientVoteCache.remove(id);
        if (won && ClientConfig.WIN_TOAST.get() && !items.isEmpty()) {
            Minecraft.getInstance().getToasts().addToast(new LootWonToast(items));
        }
    }
}
