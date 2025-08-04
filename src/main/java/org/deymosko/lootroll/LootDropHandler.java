package org.deymosko.lootroll;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
        Level level = entity.level();

        if (level.isClientSide) return;

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId == null) return;

        List<String> configList = (List<String>) Config.LOOT_ENTITIES.get();

        if (!configList.contains(entityId.toString())) return;

        List<ItemStack> droppedItems = event.getDrops().stream()
                .map(ItemEntity::getItem)
                .collect(Collectors.toList());
        event.getDrops().clear();

        startVoteSession(droppedItems, entity);
    }


    public static void startVoteSession(List<ItemStack> drops, LivingEntity entity)
    {
        if(entity.level().isClientSide) return;

        int durationSeconds = Config.VOTE_DURATION.get();
        int radius = Config.VOTE_RADIUS.get();
        Vec3 sourcePos = entity.position();
        ServerLevel serverLevel = (ServerLevel) entity.level();

        List<ServerPlayer> serverPlayers = serverLevel.getNearbyPlayers(
                        TargetingConditions.forNonCombat(),
                        entity,
                        entity.getBoundingBox().inflate(radius)
                ).stream()
                .filter(p -> p instanceof ServerPlayer)
                .map(p -> (ServerPlayer) p)
                .toList();
        if (drops.isEmpty()) return;
        for (ItemStack drop : drops) {
            List<ItemStack> items = Collections.singletonList(drop);
            VoteSession session = new VoteSession(items, serverPlayers, durationSeconds, sourcePos, serverLevel);
            VoteManager.addSession(session);
            for (ServerPlayer p : serverPlayers) {
                Packets.sendToClient(new VoteStartS2CPacket(session.getId(), items, session.getEndTime()), p);
            }
        }
    }
}
