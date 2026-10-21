package org.deymosko.lootroll.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.deymosko.lootroll.enums.VoteType;
import org.deymosko.lootroll.network.Packets;
import org.deymosko.lootroll.network.s2c.VoteEndS2CPacket;
import org.deymosko.lootroll.network.s2c.VoteStartS2CPacket;

import java.util.*;

public class VoteManager {
    private static final Map<UUID, VoteSession> activeVotes = new LinkedHashMap<>();

    private static Component toItemComponent(ItemStack stack) {
        MutableComponent name = Component.literal(stack.getHoverName().getString());
        if (stack.getCount() > 1) {
            name.append(" x" + stack.getCount());
        }
        return name.withStyle(style -> style
                .withColor(stack.getRarity().color)
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

    private static Component itemsComponent(VoteSession session) {
        return joinWithComma(session.getItems().stream()
                .map(VoteManager::toItemComponent)
                .toList());
    }

    private static Component voteTypeComponent(VoteType type) {
        return Component.translatable("lootroll.vote.type." + type.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.RED);
    }

    /** Registers the session and shows the vote to every participant. */
    public static void startSession(VoteSession session, MinecraftServer server) {
        activeVotes.put(session.getId(), session);
        VoteStartS2CPacket packet = new VoteStartS2CPacket(session.getId(), session.getItems(),
                session.getDurationTicks(), session.getRemainingTicks());
        forEachOnline(server, session, p -> Packets.sendToClient(packet, p));
    }

    public static Collection<VoteSession> getActiveSessions() {
        return Collections.unmodifiableCollection(activeVotes.values());
    }

    public static Optional<VoteSession> get(UUID id) {
        return Optional.ofNullable(activeVotes.get(id));
    }

    public static void tick(MinecraftServer server) {
        if (activeVotes.isEmpty()) return;

        List<VoteSession> finished = new ArrayList<>();
        for (VoteSession session : activeVotes.values()) {
            session.tick();
            if (session.isFinished()) {
                finished.add(session);
            }
        }
        for (VoteSession session : finished) {
            activeVotes.remove(session.getId());
            finish(server, session);
        }
    }

    private static void finish(MinecraftServer server, VoteSession session) {
        Optional<UUID> winnerOpt = session.getWinner();

        if (winnerOpt.isPresent()) {
            UUID winner = winnerOpt.get();
            ServerPlayer player = server.getPlayerList().getPlayer(winner);
            if (player != null) {
                giveItems(player, session.getItems());
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.displayClientMessage(Component.translatable("lootroll.vote.won")
                        .withStyle(ChatFormatting.GREEN), false);
            } else {
                PendingLoot.get(server).add(winner, session.getItems());
            }
        } else if (session.getInitiatorId() != null) {
            // Nobody wanted an item offered with /startvote: give it back to whoever offered it.
            ServerPlayer starter = server.getPlayerList().getPlayer(session.getInitiatorId());
            if (starter != null) {
                giveItems(starter, session.getItems());
            } else {
                PendingLoot.get(server).add(session.getInitiatorId(), session.getItems());
            }
        } else {
            dropItemsToWorld(server, session);
        }

        VoteEndS2CPacket endPacket = new VoteEndS2CPacket(session.getId());
        forEachOnline(server, session, p -> {
            Packets.sendToClient(endPacket, p);
            sendResult(p, session, winnerOpt);
        });
    }

    private static void sendResult(ServerPlayer p, VoteSession session, Optional<UUID> winnerOpt) {
        Component itemsComponent = itemsComponent(session);
        p.sendSystemMessage(Component.translatable("lootroll.vote.result", itemsComponent)
                .withStyle(ChatFormatting.GREEN));

        List<UUID> needers = session.getVoters(VoteType.NEED);
        List<UUID> greeders = session.getVoters(VoteType.GREED);
        VoteType displayType = !needers.isEmpty() ? VoteType.NEED : !greeders.isEmpty() ? VoteType.GREED : VoteType.PASS;

        if (displayType == VoteType.PASS) {
            p.sendSystemMessage(Component.translatable("lootroll.vote.unwanted", itemsComponent)
                    .withStyle(ChatFormatting.GREEN));
            return;
        }

        for (UUID id : displayType == VoteType.NEED ? needers : greeders) {
            Component nameComp = Component.literal(session.getName(id)).withStyle(ChatFormatting.YELLOW);
            Component rollComp = Component.literal(String.valueOf(session.getRolls().getOrDefault(id, 0))).withStyle(ChatFormatting.AQUA);
            p.sendSystemMessage(Component.translatable("lootroll.vote.entry", nameComp, voteTypeComponent(displayType), rollComp)
                    .withStyle(ChatFormatting.GREEN));
        }

        for (Map<UUID, Integer> round : session.getTieBreaks()) {
            List<Component> rerolls = round.entrySet().stream()
                    .map(e -> (Component) Component.literal(session.getName(e.getKey())).withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(" (" + e.getValue() + ")").withStyle(ChatFormatting.AQUA)))
                    .toList();
            p.sendSystemMessage(Component.translatable("lootroll.vote.tie", joinWithComma(rerolls))
                    .withStyle(ChatFormatting.GREEN));
        }

        winnerOpt.ifPresent(win -> p.sendSystemMessage(Component.translatable("lootroll.vote.winner",
                        Component.literal(session.getName(win)).withStyle(ChatFormatting.YELLOW))
                .withStyle(ChatFormatting.GREEN)));
    }

    public static void vote(UUID sessionId, ServerPlayer player, VoteType type) {
        VoteSession session = activeVotes.get(sessionId);
        if (session == null) return;

        if (!session.isParticipant(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("lootroll.vote.not_participant")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        if (!session.vote(player.getUUID(), type)) {
            player.sendSystemMessage(Component.translatable("lootroll.vote.already_voted")
                    .withStyle(ChatFormatting.GREEN));
            return;
        }
        announceVote(player.getServer(), session, player.getUUID(), type);
    }

    private static void announceVote(MinecraftServer server, VoteSession session, UUID voter, VoteType type) {
        Component itemsComponent = itemsComponent(session);
        Component name = Component.literal(session.getName(voter));
        Component message;
        if (type == VoteType.PASS) {
            message = Component.translatable("lootroll.vote.pass", name, itemsComponent);
        } else {
            int roll = session.getRolls().getOrDefault(voter, 0);
            message = Component.translatable("lootroll.vote.roll", name, roll, itemsComponent, voteTypeComponent(type));
        }
        Component styled = message.copy().withStyle(ChatFormatting.GREEN);
        forEachOnline(server, session, p -> p.sendSystemMessage(styled));
    }

    /** A participant who leaves before voting passes, so the vote doesn't wait for them. */
    public static void onPlayerQuit(ServerPlayer player) {
        UUID id = player.getUUID();
        for (VoteSession session : activeVotes.values()) {
            if (session.isParticipant(id) && !session.hasVoted(id)) {
                session.vote(id, VoteType.PASS);
                announceVote(player.getServer(), session, id, VoteType.PASS);
            }
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        List<ItemStack> items = PendingLoot.get(player.getServer()).take(player.getUUID());
        if (items.isEmpty()) return;

        giveItems(player, items);
        player.sendSystemMessage(Component.translatable("lootroll.vote.offline_delivered",
                        joinWithComma(items.stream().map(VoteManager::toItemComponent).toList()))
                .withStyle(ChatFormatting.GREEN));
    }

    /** Resolve everything still running so no loot is lost when the server shuts down. */
    public static void onServerStopping(MinecraftServer server) {
        for (VoteSession session : activeVotes.values()) {
            session.expire();
        }
        tick(server);
        activeVotes.clear();
    }

    private static void giveItems(ServerPlayer player, List<ItemStack> items) {
        for (ItemStack stack : items) {
            ItemStack copy = stack.copy();
            // add() shrinks the stack by what fit, so only the remainder is dropped.
            if (!player.getInventory().add(copy) && !copy.isEmpty()) {
                player.drop(copy, false);
            }
        }
    }

    private static void dropItemsToWorld(MinecraftServer server, VoteSession session) {
        ServerLevel level = server.getLevel(session.getDimension());
        if (level == null) level = server.overworld();
        Vec3 pos = session.getSourcePos();
        for (ItemStack stack : session.getItems()) {
            ItemEntity drop = new ItemEntity(level, pos.x, pos.y + 0.5, pos.z, stack.copy());
            level.addFreshEntity(drop);
        }
    }

    private static void forEachOnline(MinecraftServer server, VoteSession session, java.util.function.Consumer<ServerPlayer> action) {
        for (UUID id : session.getParticipants()) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p != null) action.accept(p);
        }
    }
}
