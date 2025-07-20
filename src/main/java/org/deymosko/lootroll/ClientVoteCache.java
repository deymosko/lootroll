package org.deymosko.lootroll;

import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ClientVoteCache {

    public record VoteData(ItemStack item, long endTime) {}

    private static final Map<UUID, VoteData> voteQueue = new LinkedHashMap<>();


    public static void add(UUID id, ItemStack item, long endTime) {
        voteQueue.putIfAbsent(id, new VoteData(item.copy(), endTime));
    }

    public static void remove(UUID id) {
        voteQueue.remove(id);
    }
    public static Set<UUID> getPendingVotes() {
        return Collections.unmodifiableSet(voteQueue.keySet());
    }

    public static boolean hasVote() {
        return !voteQueue.isEmpty();
    }

    public static ItemStack getCurrentItem() {
        return voteQueue.isEmpty() ? ItemStack.EMPTY : voteQueue.values().iterator().next().item();
    }

    public static UUID getCurrentId() {
        return voteQueue.isEmpty() ? null : voteQueue.keySet().iterator().next();
    }

    public static long getCurrentEndTime() {
        return voteQueue.isEmpty() ? System.currentTimeMillis() : voteQueue.values().iterator().next().endTime();
    }

    public static void poll() {
        if (!voteQueue.isEmpty()) {
            voteQueue.remove(getCurrentId());
        }
    }

}

