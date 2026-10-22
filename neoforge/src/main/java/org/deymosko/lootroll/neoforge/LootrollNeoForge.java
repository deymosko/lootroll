package org.deymosko.lootroll.neoforge;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.deymosko.lootroll.ClientConfig;
import org.deymosko.lootroll.Config;
import org.deymosko.lootroll.LootDropHandler;
import org.deymosko.lootroll.Lootroll;
import org.deymosko.lootroll.client.LootrollClient;

import java.util.List;

@Mod(Lootroll.MODID)
public class LootrollNeoForge {

    public LootrollNeoForge(IEventBus modBus, ModContainer container)
    {
        container.registerConfig(ModConfig.Type.COMMON, Config.CONFIG);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);

        Lootroll.init();
        EnvExecutor.runInEnv(Env.CLIENT, () -> LootrollClient::init);

        // LOWEST so drops added by other mods are part of the vote too.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, LootrollNeoForge::onEntityDrops);
    }

    private static void onEntityDrops(LivingDropsEvent event)
    {
        LivingEntity entity = event.getEntity();
        if (!LootDropHandler.isTracked(entity)) return;

        List<ItemStack> droppedItems = event.getDrops().stream()
                .map(ItemEntity::getItem)
                .toList();
        event.getDrops().clear();

        LootDropHandler.onEntityDrops(entity, droppedItems);
    }
}
