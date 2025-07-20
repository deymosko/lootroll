package org.deymosko.lootroll;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.events.VoteSession;
import org.deymosko.lootroll.network.Packets;
import org.deymosko.lootroll.network.s2c.VoteStartS2CPacket;

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
        List<ServerPlayer> serverPlayers = serverLevel.getNearbyPlayers(
                        TargetingConditions.forNonCombat(),
                        entity,
                        entity.getBoundingBox().inflate(100.0)
                ).stream()
                .filter(p -> p instanceof ServerPlayer)
                .map(p -> (ServerPlayer) p)
                .toList();
        ItemStack testItem = new ItemStack(Items.DIAMOND_SWORD); // Потім замінимо на лут із loot table

        VoteSession session = new VoteSession(testItem, serverPlayers, 30); // 30 секунд
        VoteManager.addSession(session);

        Lootroll.LOGGER.info("Почато голосування за {}, учасників: {}", testItem.getDisplayName().getString(), serverPlayers.size());

        for (ServerPlayer p : serverPlayers) {
            Lootroll.LOGGER.info("→ Поблизу гравець: {}", p.getName().getString());
            Packets.sendToClient(new VoteStartS2CPacket(session.getId(), testItem), p);
        }
    }
}
