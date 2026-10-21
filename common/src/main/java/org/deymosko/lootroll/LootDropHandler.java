package org.deymosko.lootroll;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.events.VoteSession;
import org.deymosko.lootroll.network.Packets;
import org.deymosko.lootroll.network.s2c.VoteStartS2CPacket;

import java.util.Collections;
import java.util.List;

public class LootDropHandler {

    /** Whether death drops of this entity should go to a loot vote instead of the ground. */
    public static boolean isTracked(LivingEntity entity) {
        if (entity.level().isClientSide) return false;

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return Config.LOOT_ENTITIES.get().contains(entityId.toString());
    }

    /**
     * Called by the loader-specific drop hooks with the entity's death drops.
     * The caller is responsible for removing the drops from the world once this returns.
     */
    public static void onEntityDrops(LivingEntity entity, List<ItemStack> droppedItems) {
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
