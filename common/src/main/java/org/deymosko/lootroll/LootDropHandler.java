package org.deymosko.lootroll;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.deymosko.lootroll.events.Participants;
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.events.VoteSession;

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
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        List<ItemStack> items = drops.stream().filter(stack -> !stack.isEmpty()).toList();
        if (items.isEmpty()) return;

        int durationSeconds = Config.VOTE_DURATION.get();
        Vec3 sourcePos = entity.position();
        List<ServerPlayer> participants = Participants.around(serverLevel, sourcePos, Config.VOTE_RADIUS.get());

        for (ItemStack drop : items) {
            VoteSession session = new VoteSession(Collections.singletonList(drop), participants, durationSeconds, sourcePos, serverLevel);
            VoteManager.startSession(session, serverLevel.getServer());
        }
    }
}
