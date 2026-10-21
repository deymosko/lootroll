package org.deymosko.lootroll;

import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ClientVoteCache {

    public record VoteData(List<ItemStack> items, long endTime) {}

    private static final Map<UUID, VoteData> voteQueue = new LinkedHashMap<>();

    public static void add(UUID id, List<ItemStack> items, long endTime) {
        voteQueue.putIfAbsent(id, new VoteData(items, endTime));
    }

    public static void remove(UUID id) {
        voteQueue.remove(id);
    }

    public static VoteData get(UUID id) {
        return voteQueue.get(id);
    }

    public static Set<UUID> getPendingVotes() {
        return Collections.unmodifiableSet(voteQueue.keySet());
    }

    public static boolean hasVote() {
        return !voteQueue.isEmpty();
    }

    public static List<ItemStack> getCurrentItems() {
        return voteQueue.isEmpty() ? List.of() : voteQueue.values().iterator().next().items();
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


