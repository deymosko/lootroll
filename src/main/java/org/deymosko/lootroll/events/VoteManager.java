package org.deymosko.lootroll.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.enums.VoteType;

import java.util.*;

public class VoteManager {
    private static final Map<UUID, VoteSession> activeVotes = new HashMap<>();

    private static Component toItemComponent(ItemStack stack) {
        return Component.literal(stack.getHoverName().getString())
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack.copy()))));
    }

    private static Component joinWithComma(List<Component> components) {
        MutableComponent result = Component.empty();
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                result.append(Component.literal(", ").withStyle(ChatFormatting.GREEN));
            }
            result.append(components.get(i));
        }
        return result;
    }

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
                        player.displayClientMessage(Component.translatable("lootroll.vote.won")
                                .withStyle(ChatFormatting.GREEN), false);
                    }
                });

                // Повідомляємо результати
                Component itemsComponent = joinWithComma(session.getItems().stream()
                        .map(VoteManager::toItemComponent)
                        .toList());

                for (ServerPlayer p : session.getParticipants()) {
                    p.sendSystemMessage(Component.translatable("lootroll.vote.result", itemsComponent)
                            .withStyle(ChatFormatting.GREEN));
                    session.getVotes().forEach((id, vote) -> {
                        int roll = session.getRolls().getOrDefault(id, 0);
                        String name = session.getParticipants().stream()
                                .filter(sp -> sp.getUUID().equals(id))
                                .findFirst()
                                .map(sp -> sp.getName().getString())
                                .orElse(id.toString());
                        p.sendSystemMessage(Component.translatable("lootroll.vote.entry", name, vote, roll)
                                .withStyle(ChatFormatting.GREEN));
                    });
                    winnerOpt.ifPresent(win -> {
                        String winnerName = session.getParticipants().stream()
                                .filter(sp -> sp.getUUID().equals(win))
                                .findFirst()
                                .map(sp -> sp.getName().getString())
                                .orElse(win.toString());
                        p.sendSystemMessage(Component.translatable("lootroll.vote.winner", winnerName)
                                .withStyle(ChatFormatting.GREEN));
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
                player.sendSystemMessage(Component.translatable("lootroll.vote.already_voted")
                        .withStyle(ChatFormatting.GREEN));
                return;
            }
            session.vote(player.getUUID(), type);

            Component itemsComponent = joinWithComma(session.getItems().stream()
                    .map(VoteManager::toItemComponent)
                    .toList());

            int roll = session.getRolls().getOrDefault(player.getUUID(), 0);
            for (ServerPlayer p : session.getParticipants()) {
                if (type == VoteType.PASS) {
                    p.sendSystemMessage(Component.translatable("lootroll.vote.pass", player.getName(), itemsComponent)
                            .withStyle(ChatFormatting.GREEN));
                } else {
                    p.sendSystemMessage(Component.translatable("lootroll.vote.roll", player.getName(), roll, itemsComponent, type)
                            .withStyle(ChatFormatting.GREEN));
                }
            }
        }
    }
}

