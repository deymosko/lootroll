package org.deymosko.lootroll.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.enums.VoteType;

import java.util.*;

public class VoteSession {
    private final UUID sessionId;
    private final List<ItemStack> items;
    private final List<ServerPlayer> participants;
    private final Map<UUID, VoteType> votes = new HashMap<>();
    private final Map<UUID, Integer> rolls = new HashMap<>();
    private final long endTime;


    public VoteSession(List<ItemStack> items, List<ServerPlayer> participants, int durationSeconds) {
        this.sessionId = UUID.randomUUID();
        this.items = items.stream().map(ItemStack::copy).toList();
        this.participants = new ArrayList<>(participants);
        this.endTime = System.currentTimeMillis() + durationSeconds * 1000L;
    }

    public List<ItemStack> getItems() {
        return items;
    }


    public UUID getId() {
        return sessionId;
    }


    public List<ServerPlayer> getParticipants() {
        return participants;
    }

    public void vote(UUID playerId, VoteType vote) {
        if (!votes.containsKey(playerId)) {
            votes.put(playerId, vote);
            if (vote == VoteType.NEED || vote == VoteType.GREED) {
                int roll = new Random().nextInt(100) + 1;
                System.out.println(playerId + ": " + roll);
                rolls.put(playerId, roll);
            }
        }
    }

    public boolean isFinished() {
        return System.currentTimeMillis() >= endTime || votes.size() >= participants.size();
    }

    public Optional<UUID> getWinner() {
        List<UUID> needers = getVoters(VoteType.NEED);
        List<UUID> greeders = getVoters(VoteType.GREED);

        if (!needers.isEmpty()) {
            return getTopRoll(needers);
        } else if (!greeders.isEmpty()) {
            return getTopRoll(greeders);
        }
        return Optional.empty(); // всі натиснули PASS
    }

    private List<UUID> getVoters(VoteType type) {
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, VoteType> entry : votes.entrySet()) {
            if (entry.getValue() == type) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private Optional<UUID> getTopRoll(List<UUID> group) {
        return group.stream()
                .max(Comparator.comparingInt(rolls::get));
    }

    public Map<UUID, VoteType> getVotes() {
        return votes;
    }

    public Map<UUID, Integer> getRolls() {
        return rolls;
    }

    public long getTimeLeftMs() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public long getEndTime() {
        return endTime;
    }
}

