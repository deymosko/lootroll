package org.deymosko.lootroll;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.deymosko.lootroll.events.Participants;
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.events.VoteSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LootDropHandler {

    private static final RegistryMatcher<EntityType<?>> TRACKED_ENTITIES =
            new RegistryMatcher<>(Registries.ENTITY_TYPE, Config.LOOT_ENTITIES::get);
    private static final RegistryMatcher<Item> BLACKLISTED_ITEMS =
            new RegistryMatcher<>(Registries.ITEM, Config.ITEM_BLACKLIST::get);

    /** Whether death drops of this entity should go to a loot vote instead of the ground. */
    public static boolean isTracked(LivingEntity entity) {
        if (entity.level().isClientSide) return false;
        return TRACKED_ENTITIES.matches(entity.getType().builtInRegistryHolder());
    }

    /** Whether this item may be voted on at all; others drop normally. */
    public static boolean isVotable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getRarity().ordinal() < Config.MIN_RARITY.get().ordinal()) return false;
        return !BLACKLISTED_ITEMS.matches(stack.getItem().builtInRegistryHolder());
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

        Vec3 sourcePos = entity.position();
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : drops) {
            if (isVotable(stack)) items.add(stack);
            else if (!stack.isEmpty()) dropInWorld(serverLevel, sourcePos, stack);
        }
        if (items.isEmpty()) return;

        List<ServerPlayer> participants = Participants.forDeath(entity);
        if (participants.size() < Config.MIN_PLAYERS.get()) {
            items.forEach(stack -> dropInWorld(serverLevel, sourcePos, stack));
            return;
        }

        int durationSeconds = Config.VOTE_DURATION.get();
        for (ItemStack drop : items) {
            VoteSession session = new VoteSession(Collections.singletonList(drop), participants, durationSeconds, sourcePos, serverLevel);
            VoteManager.startSession(session, serverLevel.getServer());
        }
    }

    public static void dropInWorld(ServerLevel level, Vec3 pos, ItemStack stack) {
        ItemEntity drop = new ItemEntity(level, pos.x, pos.y + 0.5, pos.z, stack.copy());
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
    }
}
