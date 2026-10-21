package org.deymosko.lootroll;

import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import org.deymosko.lootroll.commands.LootRollCommand;
import org.deymosko.lootroll.commands.RollCommand;
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.network.Packets;
import org.slf4j.Logger;

public final class Lootroll {

    public static final String MODID = "lootroll";
    public static final Logger LOGGER = LogUtils.getLogger();

    private Lootroll() {}

    /** Common (both sides) initialisation. Called by each loader entrypoint after the config is registered. */
    public static void init()
    {
        Packets.register();

        TickEvent.SERVER_POST.register(VoteManager::tick);
        LifecycleEvent.SERVER_STOPPING.register(VoteManager::onServerStopping);
        PlayerEvent.PLAYER_JOIN.register(VoteManager::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(VoteManager::onPlayerQuit);

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            RollCommand.register(dispatcher);
            LootRollCommand.register(dispatcher);
        });
    }
}
