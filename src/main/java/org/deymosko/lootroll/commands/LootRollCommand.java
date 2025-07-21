package org.deymosko.lootroll.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.events.VoteSession;
import org.deymosko.lootroll.network.Packets;
import org.deymosko.lootroll.network.s2c.VoteStartS2CPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LootRollCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("startvote")
                .executes(ctx -> createLootRoll(ctx.getSource(), -1))
                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int count = IntegerArgumentType.getInteger(ctx, "count");
                            return createLootRoll(ctx.getSource(), count);
                        }))
        );
    }

    private static int createLootRoll(CommandSourceStack source, int count) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.translatable("lootroll.command.players_only"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            player.sendSystemMessage(Component.translatable("lootroll.command.no_item_in_hand"));
            return 0;
        }

        int amount = count <= 0 ? stack.getCount() : Math.min(count, stack.getCount());
        ItemStack prize = stack.split(amount);

        ServerLevel level = player.serverLevel();
        TargetingConditions conditions = TargetingConditions.forNonCombat();
        List<Player> nearby = level.getNearbyPlayers(conditions, player, player.getBoundingBox().inflate(100.0d));
        List<ServerPlayer> participants = new ArrayList<>();
        participants.add(player);
        for (Player p : nearby) {
            if (p instanceof ServerPlayer sp) {
                participants.add(sp);
            }
        }

        VoteSession session = new VoteSession(Collections.singletonList(prize), participants, 30);
        VoteManager.addSession(session);
        for (ServerPlayer p : participants) {
            Packets.sendToClient(new VoteStartS2CPacket(session.getId(), session.getItems(), session.getEndTime()), p);
        }

        return Command.SINGLE_SUCCESS;
    }
}
