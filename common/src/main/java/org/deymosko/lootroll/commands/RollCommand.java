package org.deymosko.lootroll.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.deymosko.lootroll.Config;

import java.util.List;

public class RollCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("roll")
                .executes(ctx -> {
                    return roll(ctx.getSource(), 1, 100);
                })
                .then(Commands.argument("limit", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int limit = IntegerArgumentType.getInteger(ctx, "limit");
                            return roll(ctx.getSource(), 1, limit);
                        })
                )
                .then(Commands.argument("min", IntegerArgumentType.integer())
                        .then(Commands.argument("max", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    int min = IntegerArgumentType.getInteger(ctx, "min");
                                    int max = IntegerArgumentType.getInteger(ctx, "max");
                                    return roll(ctx.getSource(), min, max);
                                })
                        )
                )
        );
    }

    private static int roll(CommandSourceStack source, int min, int max) {
        if (min > max) {
            source.sendFailure(Component.translatable("lootroll.roll.invalid_range"));
            return 0;
        }

        int rollRes = source.getLevel().getRandom().nextIntBetweenInclusive(min, max);
        ServerPlayer sender;
        try {
            sender = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.translatable("lootroll.command.players_only"));
            return 0;
        }

        ServerLevel level = sender.serverLevel();
        TargetingConditions conditions = TargetingConditions.forNonCombat().ignoreLineOfSight();
        List<Player> nearbyPlayers = level.getNearbyPlayers(conditions, sender, sender.getBoundingBox().inflate(Config.ROLL_COMMAND_RADIUS.get()));

        Component msg = Component
                .translatable("player.roll.result", sender.getName().getString(), rollRes, min, max)
                .withStyle(style -> style.withColor(ChatFormatting.YELLOW).withBold(false));

        sender.sendSystemMessage(msg);
        for (Player player : nearbyPlayers) {
            player.sendSystemMessage(msg);
        }

        return Command.SINGLE_SUCCESS;
    }
}

