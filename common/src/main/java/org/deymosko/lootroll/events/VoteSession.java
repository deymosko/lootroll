package org.deymosko.lootroll.events;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.deymosko.lootroll.Config;
import org.deymosko.lootroll.enums.VoteType;

import java.util.*;

public class VoteSession {
    private final UUID sessionId = UUID.randomUUID();
    private final List<ItemStack> items;
    // Stored by UUID so a player who relogs mid-vote is still the same participant.
    private final Map<UUID, String> participants = new LinkedHashMap<>();
    private final Map<UUID, VoteType> votes = new LinkedHashMap<>();
    private final Map<UUID, Integer> rolls = new HashMap<>();
    // One map per extra round rolled to break a tie between the top rollers.
    private final List<Map<UUID, Integer>> tieBreaks = new ArrayList<>();
    private final UUID initiatorId;
    private final int durationTicks;
    private int remainingTicks;
    private final Vec3 sourcePos;
    private final ResourceKey<Level> dimension;
    private final RandomSource random;
    // Snapshot of the config when the vote started, so a reload mid-vote can't change the rules.
    private final int rollMin;
    private final int rollMax;
    private final boolean allowGreed;
    private final boolean endWhenAllVoted;
    private Optional<UUID> winner;

    public VoteSession(List<ItemStack> items, List<ServerPlayer> participants, int durationSeconds, Vec3 sourcePos, Level world) {
        this(items, participants, durationSeconds, null, sourcePos, world);
    }

    public VoteSession(List<ItemStack> items, List<ServerPlayer> participants, int durationSeconds, UUID initiatorId, Vec3 sourcePos, Level world) {
        this.items = items.stream().map(ItemStack::copy).toList();
        for (ServerPlayer p : participants) {
            this.participants.put(p.getUUID(), p.getGameProfile().getName());
        }
        this.initiatorId = initiatorId;
        this.durationTicks = Math.max(1, durationSeconds * 20);
        this.remainingTicks = this.durationTicks;
        this.sourcePos = sourcePos;
        this.dimension = world.dimension();
        this.random = world.getRandom();
        this.rollMin = Config.ROLL_MIN.get();
        this.rollMax = Math.max(rollMin, Config.ROLL_MAX.get());
        this.allowGreed = Config.ALLOW_GREED.get();
        this.endWhenAllVoted = Config.END_WHEN_ALL_VOTED.get();
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

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public Set<UUID> getParticipants() {
        return Collections.unmodifiableSet(participants.keySet());
    }

    public boolean isParticipant(UUID playerId) {
        return participants.containsKey(playerId);
    }

    public String getName(UUID playerId) {
        return participants.getOrDefault(playerId, playerId.toString());
    }

    public boolean hasVoted(UUID playerId) {
        return votes.containsKey(playerId);
    }

    public boolean isGreedAllowed() {
        return allowGreed;
    }

    /** @return false if the player isn't a participant or has already voted. */
    public boolean vote(UUID playerId, VoteType vote) {
        if (!isParticipant(playerId) || votes.containsKey(playerId) || winner != null) return false;
        if (vote == VoteType.GREED && !allowGreed) return false;

        votes.put(playerId, vote);
        if (vote == VoteType.NEED || vote == VoteType.GREED) {
            rolls.put(playerId, roll());
        }
        return true;
    }

    private int roll() {
        return random.nextIntBetweenInclusive(rollMin, rollMax);
    }

    public void tick() {
        if (remainingTicks > 0) remainingTicks--;
    }

    /** Ends the vote early, e.g. when the server is stopping. */
    public void expire() {
        remainingTicks = 0;
    }

    public boolean isFinished() {
        return remainingTicks <= 0 || (endWhenAllVoted && votes.size() >= participants.size());
    }

    /** Resolved once; later calls return the same winner. */
    public Optional<UUID> getWinner() {
        if (winner == null) {
            List<UUID> needers = getVoters(VoteType.NEED);
            List<UUID> greeders = getVoters(VoteType.GREED);

            if (!needers.isEmpty()) {
                winner = getTopRoll(needers);
            } else if (!greeders.isEmpty()) {
                winner = getTopRoll(greeders);
            } else {
                winner = Optional.empty();
            }
        }
        return winner;
    }

    public List<UUID> getVoters(VoteType type) {
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, VoteType> entry : votes.entrySet()) {
            if (entry.getValue() == type) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private Optional<UUID> getTopRoll(List<UUID> group) {
        List<UUID> contenders = topOf(group, rolls);

        while (contenders.size() > 1) {
            Map<UUID, Integer> round = new LinkedHashMap<>();
            for (UUID id : contenders) {
                round.put(id, roll());
            }
            tieBreaks.add(round);
            contenders = topOf(contenders, round);
        }

        return contenders.isEmpty() ? Optional.empty() : Optional.of(contenders.get(0));
    }

    /** Voters of the winning group (Need, else Greed), best roll first, ignoring tie-break rounds. */
    public List<UUID> getRankedCandidates() {
        List<UUID> group = getVoters(VoteType.NEED);
        if (group.isEmpty()) group = getVoters(VoteType.GREED);
        List<UUID> ranked = new ArrayList<>(group);
        ranked.sort(Comparator.comparingInt((UUID id) -> rolls.getOrDefault(id, 0)).reversed());
        getWinner().ifPresent(w -> {
            ranked.remove(w);
            ranked.add(0, w);
        });
        return ranked;
    }

    public RandomSource getRandom() {
        return random;
    }

    private static List<UUID> topOf(List<UUID> group, Map<UUID, Integer> scores) {
        int max = group.stream().mapToInt(id -> scores.getOrDefault(id, 0)).max().orElse(-1);
        return group.stream().filter(id -> scores.getOrDefault(id, 0) == max).toList();
    }

    public Map<UUID, VoteType> getVotes() {
        return votes;
    }

    public Map<UUID, Integer> getRolls() {
        return rolls;
    }

    public List<Map<UUID, Integer>> getTieBreaks() {
        return tieBreaks;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public Vec3 getSourcePos() {
        return sourcePos;
    }
}
