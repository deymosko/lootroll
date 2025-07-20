package org.deymosko.lootroll;

import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ClientVoteCache {

    private static final Map<UUID, ItemStack> voteQueue = new LinkedHashMap<>();


    public static void add(UUID id, ItemStack item) {
        voteQueue.putIfAbsent(id, item.copy());
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
        return voteQueue.isEmpty() ? ItemStack.EMPTY : voteQueue.values().iterator().next();
    }

    public static UUID getCurrentId() {
        return voteQueue.isEmpty() ? null : voteQueue.keySet().iterator().next();
    }

    public static void poll() {
        if (!voteQueue.isEmpty()) {
            voteQueue.remove(getCurrentId());
        }
    }

}

