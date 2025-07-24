package org.deymosko.lootroll.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
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
                        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
                        player.displayClientMessage(Component.translatable("lootroll.vote.won")
                                .withStyle(ChatFormatting.GREEN), false);
                    }
                });
                if (winnerOpt.isEmpty()) {
                    UUID starterId = session.getInitiatorId();
                    ServerPlayer starter = session.getParticipants().stream()
                            .filter(p -> p.getUUID().equals(starterId))
                            .findFirst().orElse(null);

                    if (starter != null && session.getInitiatorId() != null) {
                        for (ItemStack stack : session.getItems()) {
                            boolean success = starter.getInventory().add(stack.copy());
                            if (!success) {
                                starter.drop(stack.copy(), false);
                            }
                        }
                    }
                    else if(session.getVotes().isEmpty())
                    {
                        dropItemsToWorld(session.getItems(), session.getSourcePos(), session.getWorld());
                    }
                    else
                    {
                        dropItemsToWorld(session.getItems(), session.getSourcePos(), session.getWorld());
                    }
                }
                Component itemsComponent = joinWithComma(session.getItems().stream()
                        .map(VoteManager::toItemComponent)
                        .toList());

                List<UUID> needers = session.getVotes().entrySet().stream()
                        .filter(e -> e.getValue() == VoteType.NEED)
                        .map(Map.Entry::getKey)
                        .toList();
                List<UUID> greeders = session.getVotes().entrySet().stream()
                        .filter(e -> e.getValue() == VoteType.GREED)
                        .map(Map.Entry::getKey)
                        .toList();
                VoteType displayType;
                List<UUID> displayGroup;
                if (!needers.isEmpty()) {
                    displayType = VoteType.NEED;
                    displayGroup = needers;
                } else if (!greeders.isEmpty()) {
                    displayType = VoteType.GREED;
                    displayGroup = greeders;
                } else {
                    displayType = VoteType.PASS;
                    displayGroup = session.getVotes().entrySet().stream()
                            .filter(e -> e.getValue() == VoteType.PASS)
                            .map(Map.Entry::getKey)
                            .toList();
                }

                for (ServerPlayer p : session.getParticipants()) {
                    Component countComponent = session.getItems().get(0).getCount() > 1
                            ? Component.literal("x" + session.getItems().get(0).getCount())
                            : Component.empty();

                    Component resultMessage = Component.translatable("lootroll.vote.result", itemsComponent, countComponent)
                            .withStyle(ChatFormatting.GREEN);

                    p.sendSystemMessage(resultMessage);

                    if (displayType == VoteType.PASS) {
                        p.sendSystemMessage(Component.translatable("lootroll.vote.unwanted", itemsComponent)
                                .withStyle(ChatFormatting.GREEN));
                    } else {
                        for (UUID id : displayGroup) {
                            int roll = session.getRolls().getOrDefault(id, 0);
                            String name = session.getParticipants().stream()
                                    .filter(sp -> sp.getUUID().equals(id))
                                    .findFirst()
                                    .map(sp -> sp.getName().getString())
                                    .orElse(id.toString());

                            Component nameComp = Component.literal(name).withStyle(ChatFormatting.YELLOW);
                            Component voteComp = Component.translatable("lootroll.vote.type." + displayType.name().toLowerCase()).withStyle(ChatFormatting.RED);
                            Component rollComp = Component.literal(String.valueOf(roll)).withStyle(ChatFormatting.AQUA);

                            p.sendSystemMessage(Component.translatable("lootroll.vote.entry", nameComp, voteComp, rollComp)
                                    .withStyle(ChatFormatting.GREEN));
                        }
                        winnerOpt.ifPresent(win -> {
                            String winnerName = session.getParticipants().stream()
                                    .filter(sp -> sp.getUUID().equals(win))
                                    .findFirst()
                                    .map(sp -> sp.getName().getString())
                                    .orElse(win.toString());
                            p.sendSystemMessage(Component.translatable("lootroll.vote.winner", Component.literal(winnerName).withStyle(ChatFormatting.YELLOW))
                                    .withStyle(ChatFormatting.GREEN));
                        });
                    }
                }
            }
        }
        finished.forEach(activeVotes::remove);
    }
    private static void dropItemsToWorld(List<ItemStack> items, Vec3 pos, ServerLevel level) {
        for (ItemStack stack : items) {
            ItemEntity drop = new ItemEntity(level, pos.x, pos.y + 0.5, pos.z, stack.copy());
            level.addFreshEntity(drop);
        }
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

