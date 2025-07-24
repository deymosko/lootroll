package org.deymosko.lootroll.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.deymosko.lootroll.Config;
import org.deymosko.lootroll.enums.VoteType;

import java.util.*;

public class VoteSession {
    private final UUID sessionId;
    private final List<ItemStack> items;
    private final List<ServerPlayer> participants;
    private final Map<UUID, VoteType> votes = new HashMap<>();
    private final UUID initiatorId;
    private final Map<UUID, Integer> rolls = new HashMap<>();
    private final long endTime;
    private final Vec3 sourcePos;
    private final ServerLevel world;
    private final int voteRadius = Config.VOTE_RADIUS.get();


    public VoteSession(List<ItemStack> items, List<ServerPlayer> participants, int durationSeconds, Vec3 sourcePos, ServerLevel world) {
        this(items, participants, durationSeconds, null, sourcePos, world);
    }
    public VoteSession(List<ItemStack> items, List<ServerPlayer> participants, int durationSeconds, UUID initiatorId, Vec3 sourcePos, ServerLevel world) {
        this.world = world;
        this.sessionId = UUID.randomUUID();
        this.items = items.stream().map(ItemStack::copy).toList();
        this.participants = new ArrayList<>(participants);
        this.initiatorId = initiatorId;
        this.endTime = System.currentTimeMillis() + durationSeconds * 1000L;
        this.sourcePos = sourcePos;
    }
    public UUID getInitiatorId() {
        return initiatorId;
    }
    public List<ItemStack> getItems() {
        return items;
    }


    public UUID getId() {
        return sessionId;
    }
    public ServerLevel getWorld()
    {
        return world;
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
        return Optional.empty();
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

        List<UUID> contenders = new ArrayList<>(group);
        Random random = new Random();

        while (contenders.size() > 1) {
            int maxRoll = contenders.stream()
                    .mapToInt(rolls::get)
                    .max()
                    .orElse(-1);

            List<UUID> top = new ArrayList<>();
            for (UUID id : contenders) {
                if (rolls.get(id) == maxRoll) {
                    top.add(id);
                }
            }

            if (top.size() == 1) {
                return Optional.of(top.get(0));
            }

            for (UUID id : top) {
                int roll = random.nextInt(100) + 1;
                rolls.put(id, roll);
            }
            contenders = new ArrayList<>(top);
        }

        return contenders.isEmpty() ? Optional.empty() : Optional.of(contenders.get(0));
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

    public Vec3 getSourcePos() {
        return sourcePos;
    }
}

