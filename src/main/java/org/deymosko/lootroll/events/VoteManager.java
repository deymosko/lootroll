package org.deymosko.lootroll.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

                // Віддаємо весь лот переможцю
                winnerOpt.ifPresent(winner -> {
                    ServerPlayer player = session.getParticipants().stream()
                            .filter(p -> p.getUUID().equals(winner))
                            .findFirst().orElse(null);
                    if (player != null) {
                        for (ItemStack stack : session.getItems()) {
                            boolean success = player.getInventory().add(stack.copy());
                            if (!success) {
                                player.drop(stack.copy(), false);
                            }
                        }
                        player.displayClientMessage(Component.literal("🎉 Ви виграли лот!"), false);
                    }
                });

                // Повідомляємо результати
                String itemsStr = session.getItems().stream()
                        .map(i -> i.getHoverName().getString())
                        .reduce((a, b) -> a + ", " + b).orElse("порожній лот");

                for (ServerPlayer p : session.getParticipants()) {
                    p.sendSystemMessage(Component.literal("Результати голосування за лот: " + itemsStr));
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

            String itemsStr = session.getItems().stream()
                    .map(i -> i.getHoverName().getString())
                    .reduce((a, b) -> a + ", " + b).orElse("порожній лот");

            int roll = session.getRolls().getOrDefault(player.getUUID(), 0);
            for (ServerPlayer p : session.getParticipants()) {
                if (type == VoteType.PASS) {
                    p.sendSystemMessage(Component.literal(player.getName().getString() + " пасує за " + itemsStr));
                } else {
                    p.sendSystemMessage(Component.literal(player.getName().getString() + " кидає " + roll + " за " + itemsStr + " (" + type + ")"));
                }
            }
        }
    }
}

