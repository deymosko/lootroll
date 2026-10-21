package org.deymosko.lootroll.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.Config;
import org.deymosko.lootroll.enums.VoteType;
import org.deymosko.lootroll.events.Participants;
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.events.VoteSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class LootRollCommand {
    // Player -> server tick when /startvote was last used.
    private static final Map<UUID, Integer> lastUse = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("startvote")
                .requires(src -> src.hasPermission(Config.STARTVOTE_PERMISSION_LEVEL.get()))
                .executes(ctx -> createLootRoll(ctx.getSource(), -1))
                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int count = IntegerArgumentType.getInteger(ctx, "count");
                            return createLootRoll(ctx.getSource(), count);
                        }))
        );

        // Target of the clickable chat buttons; also usable by hand.
        var voteId = Commands.argument("id", UuidArgument.uuid());
        for (VoteType type : VoteType.values()) {
            voteId.then(Commands.literal(type.name().toLowerCase(Locale.ROOT))
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        VoteManager.vote(UuidArgument.getUuid(ctx, "id"), player, type);
                        return Command.SINGLE_SUCCESS;
                    }));
        }
        dispatcher.register(Commands.literal("lootroll").then(Commands.literal("vote").then(voteId)));
    }

    private static int createLootRoll(CommandSourceStack source, int count) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("lootroll.command.players_only"));
            return 0;
        }

        int cooldownTicks = Config.STARTVOTE_COOLDOWN.get() * 20;
        int now = source.getServer().getTickCount();
        Integer last = lastUse.get(player.getUUID());
        if (cooldownTicks > 0 && last != null && now - last < cooldownTicks) {
            int secondsLeft = (cooldownTicks - (now - last) + 19) / 20;
            source.sendFailure(Component.translatable("lootroll.command.cooldown", secondsLeft));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            player.sendSystemMessage(Component.translatable("lootroll.command.no_item_in_hand"));
            return 0;
        }

        int amount = count <= 0 ? stack.getCount() : Math.min(count, stack.getCount());
        ItemStack prize = stack.split(amount);

        // Read config on every use so /reload-ed or edited values apply without a restart.
        List<ServerPlayer> participants = Participants.around(player.serverLevel(), player.position(), Config.VOTE_RADIUS.get());
        if (!participants.contains(player)) {
            participants = new ArrayList<>(participants);
            participants.add(0, player);
        }

        VoteSession session = new VoteSession(Collections.singletonList(prize), participants, Config.VOTE_DURATION.get(),
                player.getUUID(), player.position(), player.serverLevel());
        VoteManager.startSession(session, source.getServer());
        lastUse.put(player.getUUID(), now);

        return Command.SINGLE_SUCCESS;
    }
}
