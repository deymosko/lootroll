package org.deymosko.lootroll;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = Lootroll.MODID)
public class LootDropHandler {

    @SubscribeEvent
    public static void onEntityDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (level.isClientSide) return; // тільки на сервері

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId == null) return;

        List<String> configList = (List<String>) Config.LOOT_ENTITIES.get();

        if (!configList.contains(entityId.toString())) return;

        // ❗ Знайшли підходящу сутність — зупиняємо дроп
        event.getDrops().clear();
        Lootroll.LOGGER.info("[LootRoll] Смерть сутності {} — дроп заблоковано, починаємо голосування.", entityId);

        // (тимчасово) логування гравців поруч
        ServerLevel serverLevel = (ServerLevel) level;
        List<Player> players = serverLevel.getNearbyPlayers(
                TargetingConditions.forNonCombat(),
                entity,
                entity.getBoundingBox().inflate(100.0)
        );

        for (Player p : players) {
            Lootroll.LOGGER.info("→ Поблизу гравець: {}", p.getName().getString());

        }
    }
}
