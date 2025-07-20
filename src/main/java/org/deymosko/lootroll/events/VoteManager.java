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
                Optional<UUID> winnerOpt = session.getWinner();
                winnerOpt.ifPresent(winner ->
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

                // Повідомляємо результати всім учасникам
                String itemName = session.getItem().getHoverName().getString();
                for (ServerPlayer p : session.getParticipants()) {
                    p.sendSystemMessage(Component.literal("Результати голосування за " + itemName + ":"));
                    session.getVotes().forEach((id, vote) -> {
                        int roll = session.getRolls().getOrDefault(id, 0);
                        String name = session.getParticipants().stream()
                                .filter(sp -> sp.getUUID().equals(id))
                                .findFirst()
                                .map(sp -> sp.getName().getString())
                                .orElse(id.toString());
                        p.sendSystemMessage(Component.literal(" - " + name + " " + vote + " " + roll));
                    });
                    winnerOpt.ifPresent(win -> {
                        String winnerName = session.getParticipants().stream()
                                .filter(sp -> sp.getUUID().equals(win))
                                .findFirst()
                                .map(sp -> sp.getName().getString())
                                .orElse(win.toString());
                        p.sendSystemMessage(Component.literal("Переможець: " + winnerName));
                    });
                }
            }
        }
        finished.forEach(activeVotes::remove);
    }

    public static Optional<VoteSession> get(UUID id) {
        return Optional.ofNullable(activeVotes.get(id));
    }

    public static void vote(UUID sessionId, ServerPlayer player, VoteType type) {
        VoteSession session = activeVotes.get(sessionId);
        if (session != null) {
            if (session.getVotes().containsKey(player.getUUID())) {
                player.sendSystemMessage(Component.literal("Ви вже проголосували"));
                return;
            }
            session.vote(player.getUUID(), type);

            String itemName = session.getItem().getHoverName().getString();
            int roll = session.getRolls().getOrDefault(player.getUUID(), 0);
            for (ServerPlayer p : session.getParticipants()) {
                if (type == VoteType.PASS) {
                    p.sendSystemMessage(Component.literal(player.getName().getString() + " пасує за " + itemName));
                } else {
                    p.sendSystemMessage(Component.literal(player.getName().getString() + " кидає " + roll + " за " + itemName + " (" + type + ")"));
                }
            }
        }
    }
}
