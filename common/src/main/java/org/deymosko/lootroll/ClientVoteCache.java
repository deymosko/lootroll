package org.deymosko.lootroll;

import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ClientVoteCache {

    /** endTime is in the client's own clock (System.currentTimeMillis). */
    public record VoteData(List<ItemStack> items, long endTime, long durationMs) {
        public float progress() {
            if (durationMs <= 0) return 0.0f;
            return Math.max(0.0f, Math.min(1.0f, (endTime - System.currentTimeMillis()) / (float) durationMs));
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= endTime;
        }
    }

    // Kept around a little past the deadline in case the server's end packet is late.
    private static final long EXPIRY_GRACE_MS = 2000;

    private static final Map<UUID, VoteData> voteQueue = new LinkedHashMap<>();

    public static void add(UUID id, List<ItemStack> items, int durationTicks, int remainingTicks) {
        long now = System.currentTimeMillis();
        voteQueue.putIfAbsent(id, new VoteData(items, now + remainingTicks * 50L, durationTicks * 50L));
    }

    public static void remove(UUID id) {
        voteQueue.remove(id);
    }

    public static void clear() {
        voteQueue.clear();
    }

    /** Drops votes whose deadline passed long ago; called every client tick. */
    public static void removeExpired() {
        long cutoff = System.currentTimeMillis() - EXPIRY_GRACE_MS;
        voteQueue.values().removeIf(data -> data.endTime() < cutoff);
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

    public static int size() {
        return voteQueue.size();
    }
}
