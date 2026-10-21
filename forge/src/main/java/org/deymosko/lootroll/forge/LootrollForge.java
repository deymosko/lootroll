package org.deymosko.lootroll.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.deymosko.lootroll.ClientConfig;
import org.deymosko.lootroll.Config;
import org.deymosko.lootroll.LootDropHandler;
import org.deymosko.lootroll.Lootroll;
import org.deymosko.lootroll.client.LootrollClient;

import java.util.List;

@Mod(Lootroll.MODID)
public class LootrollForge {

    public LootrollForge()
    {
        EventBuses.registerModEventBus(Lootroll.MODID, FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);

        Lootroll.init();
        EnvExecutor.runInEnv(Env.CLIENT, () -> LootrollClient::init);

        // LOWEST so drops added by other mods are part of the vote too.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, LootrollForge::onEntityDrops);
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
