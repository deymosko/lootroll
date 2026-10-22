package org.deymosko.lootroll.events;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/** Loot won by players who were offline when the vote ended; handed out on their next login. */
public class PendingLoot extends SavedData {
    private static final String NAME = "lootroll_pending";

    private final Map<UUID, List<ItemStack>> pending = new HashMap<>();

    public static PendingLoot get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(PendingLoot::new, PendingLoot::load, null), NAME);
    }

    public void add(UUID playerId, List<ItemStack> items) {
        List<ItemStack> list = pending.computeIfAbsent(playerId, id -> new ArrayList<>());
        for (ItemStack stack : items) {
            list.add(stack.copy());
        }
        setDirty();
    }

    /** Removes and returns everything stored for this player. */
    public List<ItemStack> take(UUID playerId) {
        List<ItemStack> items = pending.remove(playerId);
        if (items == null) return List.of();
        setDirty();
        return items;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag players = new ListTag();
        pending.forEach((id, items) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", id);
            ListTag stacks = new ListTag();
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) stacks.add(stack.save(registries));
            }
            entry.put("Items", stacks);
            players.add(entry);
        });
        tag.put("Players", players);
        return tag;
    }

    private static PendingLoot load(CompoundTag tag, HolderLookup.Provider registries) {
        PendingLoot data = new PendingLoot();
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag entry = players.getCompound(i);
            ListTag stacks = entry.getList("Items", Tag.TAG_COMPOUND);
            List<ItemStack> items = new ArrayList<>();
            for (int j = 0; j < stacks.size(); j++) {
                ItemStack.parse(registries, stacks.getCompound(j)).ifPresent(items::add);
            }
            if (!items.isEmpty()) data.pending.put(entry.getUUID("Player"), items);
        }
        return data;
    }
}
