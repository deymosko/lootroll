package org.deymosko.lootroll;

import com.mojang.logging.LogUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.world.entity.item.ItemEntity;
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

        TickEvent.SERVER_POST.register(server -> {
            LootCollector.tick(server);
            VoteManager.tick(server);
        });
        LifecycleEvent.SERVER_STOPPING.register(server -> {
            LootCollector.onServerStopping(server);
            VoteManager.onServerStopping(server);
        });

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            LootCollector.onLivingDeath(entity, source);
            return EventResult.pass();
        });
        EntityEvent.ADD.register((entity, level) ->
                entity instanceof ItemEntity item && LootCollector.onItemSpawn(item, level)
                        ? EventResult.interruptFalse()
                        : EventResult.pass());
        PlayerEvent.PLAYER_JOIN.register(VoteManager::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(VoteManager::onPlayerQuit);

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            RollCommand.register(dispatcher);
            LootRollCommand.register(dispatcher);
        });
    }
}
