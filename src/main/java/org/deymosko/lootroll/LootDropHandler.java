package org.deymosko.lootroll;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.events.VoteSession;
import org.deymosko.lootroll.network.Packets;
import org.deymosko.lootroll.network.s2c.VoteStartS2CPacket;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Lootroll.MODID)
public class LootDropHandler {

    @SubscribeEvent
    public static void onEntityDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        int durationSeconds = Config.VOTE_DURATION.get();
        Level level = entity.level();
        int radius = Config.VOTE_RADIUS.get();

        if (level.isClientSide) return;

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId == null) return;

        List<String> configList = (List<String>) Config.LOOT_ENTITIES.get();

        if (!configList.contains(entityId.toString())) return;

        Vec3 sourcePos = event.getEntity().position();
        List<ItemStack> droppedItems = event.getDrops().stream()
                .map(ItemEntity::getItem)
                .collect(Collectors.toList());
        event.getDrops().clear();



        ServerLevel serverLevel = (ServerLevel) level;

        List<ServerPlayer> serverPlayers = serverLevel.getNearbyPlayers(
                        TargetingConditions.forNonCombat(),
                        entity,
                        entity.getBoundingBox().inflate(radius)
                ).stream()
                .filter(p -> p instanceof ServerPlayer)
                .map(p -> (ServerPlayer) p)
                .toList();



        if (droppedItems.isEmpty()) return;
        for(int i = 0; i < droppedItems.size(); i++)
        {
            List<ItemStack> items = Collections.singletonList(droppedItems.get(i));
            VoteSession session = new VoteSession(items, serverPlayers, durationSeconds, sourcePos, serverLevel);
            VoteManager.addSession(session);
            for (ServerPlayer p : serverPlayers) {
                Packets.sendToClient(new VoteStartS2CPacket(session.getId(), items, session.getEndTime()), p);
            }
        }
    }
}
