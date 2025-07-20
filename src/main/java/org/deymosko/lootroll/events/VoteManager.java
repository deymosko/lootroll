package org.deymosko.lootroll.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.deymosko.lootroll.enums.VoteType;

import java.util.*;

public class VoteManager {
    private static final Map<UUID, VoteSession> activeVotes = new HashMap<>();

    public static void addSession(VoteSession session) {
        activeVotes.put(session.getId(), session);
    }

    public static Collection<VoteSession> getActiveSessions() {
        return activeVotes.values();
    }

    public static void tick() {
        List<UUID> finished = new ArrayList<>();
        for (VoteSession session : activeVotes.values()) {
            if (session.isFinished()) {
                finished.add(session.getId());
                session.getWinner().ifPresent(winner ->
                {
                    System.out.println("[VoteManager] Переможець голосування " + session.getId() + ": " + winner);

                    for (ServerPlayer p : session.getParticipants()) {
                        if (p.getUUID().equals(winner)) {
                            boolean success = p.getInventory().add(session.getItem().copy());
                            if (!success) {
                                p.drop(session.getItem().copy(), false);
                            }
                            p.displayClientMessage(Component.literal("🎉 Ви виграли: " + session.getItem().getHoverName().getString()), false);
                            break;
                        }
                    }


                });
            }
        }
    }

    public static Optional<VoteSession> get(UUID id) {
        return Optional.ofNullable(activeVotes.get(id));
    }

    public static void vote(UUID sessionId, ServerPlayer player, VoteType type) {
        VoteSession session = activeVotes.get(sessionId);
        if (session != null) {
            System.out.println("[VoteManager] " + player.getName() + " is already voted");
            session.vote(player.getUUID(), type);
        }
    }
}
