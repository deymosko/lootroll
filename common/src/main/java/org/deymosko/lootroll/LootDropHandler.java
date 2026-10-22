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
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.events.VoteSession;

import java.util.ArrayList;
import java.util.List;

public class LootDropHandler {

    private static final RegistryMatcher<EntityType<?>> TRACKED_ENTITIES =
            new RegistryMatcher<>(Registries.ENTITY_TYPE, Config.LOOT_ENTITIES::get);
    private static final RegistryMatcher<Item> BLACKLISTED_ITEMS =
            new RegistryMatcher<>(Registries.ITEM, Config.ITEM_BLACKLIST::get);

    // Set while we put items into the world ourselves, so the capture zone doesn't grab them back.
    private static boolean releasing;

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

    public static boolean isReleasing() {
        return releasing;
    }

    /**
     * Called by the loader-specific drop hooks with the entity's death drops.
     * The caller is responsible for removing the drops from the world once this returns.
     */
    public static void onEntityDrops(LivingEntity entity, List<ItemStack> droppedItems) {
        LootCollector.collect(entity, droppedItems);
    }

    /** Entry point for integration mixins: the items join the entity's lot like any other drop. */
    public static void startVoteSession(List<ItemStack> drops, LivingEntity entity)
    {
        LootCollector.collect(entity, drops);
    }

    /** Turns a finished lot into votes according to the config. */
    static void startVotes(ServerLevel level, Vec3 pos, List<ServerPlayer> candidates, List<ItemStack> drops)
    {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : drops) {
            if (isVotable(stack)) items.add(stack);
            else dropInWorld(level, pos, stack);
        }
        if (Config.MERGE_STACKS.get()) items = merge(items);
        if (items.isEmpty()) return;

        // Players who logged out since the kill aren't offered the loot.
        List<ServerPlayer> participants = candidates.stream().filter(p -> !p.hasDisconnected()).toList();
        if (participants.size() < Config.MIN_PLAYERS.get()) {
            items.forEach(stack -> dropInWorld(level, pos, stack));
            return;
        }

        int durationSeconds = Config.VOTE_DURATION.get();
        for (List<ItemStack> lot : splitIntoVotes(items)) {
            VoteSession session = new VoteSession(lot, participants, durationSeconds, pos, level);
            VoteManager.startSession(session, level.getServer());
        }
    }

    private static List<List<ItemStack>> splitIntoVotes(List<ItemStack> items) {
        List<List<ItemStack>> votes = new ArrayList<>();
        switch (Config.VOTE_MODE.get()) {
            case WHOLE_LOT -> votes.add(items);
            case PER_ITEM -> {
                for (ItemStack stack : items) {
                    for (int i = 0; i < stack.getCount(); i++) {
                        votes.add(List.of(stack.copyWithCount(1)));
                    }
                }
            }
            case PER_STACK -> items.forEach(stack -> votes.add(List.of(stack)));
        }
        return votes;
    }

    /** Combines identical items into as few stacks as their max stack size allows. */
    private static List<ItemStack> merge(List<ItemStack> items) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : items) {
            ItemStack rest = stack.copy();
            for (ItemStack target : merged) {
                if (rest.isEmpty()) break;
                if (ItemStack.isSameItemSameComponents(target, rest) && target.getCount() < target.getMaxStackSize()) {
                    int moved = Math.min(rest.getCount(), target.getMaxStackSize() - target.getCount());
                    target.grow(moved);
                    rest.shrink(moved);
                }
            }
            if (!rest.isEmpty()) merged.add(rest);
        }
        return merged;
    }

    public static void dropInWorld(ServerLevel level, Vec3 pos, ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemEntity drop = new ItemEntity(level, pos.x, pos.y + 0.5, pos.z, stack.copy());
        drop.setDefaultPickUpDelay();
        releasing = true;
        try {
            level.addFreshEntity(drop);
        } finally {
            releasing = false;
        }
    }
}
