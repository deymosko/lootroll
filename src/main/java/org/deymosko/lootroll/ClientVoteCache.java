package org.deymosko.lootroll;

import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ClientVoteCache {

    private static final Set<UUID> pendingVotes = new HashSet<>();
    private static ItemStack activeVote = ItemStack.EMPTY;


    public static void add(UUID id) {
        pendingVotes.add(id);
    }

    public static void remove(UUID id) {
        pendingVotes.remove(id);
    }
    public static Set<UUID> getPendingVotes() {
        return Collections.unmodifiableSet(pendingVotes);
    }

    public static void addVote(ItemStack item) {
        activeVote = item;
    }

    public static boolean hasVote() {
        return !activeVote.isEmpty();
    }

    public static ItemStack getVote() {
        return activeVote;
    }

    public static void clear() {
        activeVote = ItemStack.EMPTY;
    }

}

