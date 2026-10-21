package org.deymosko.lootroll;

import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class Config
{
    public enum NoWinnerAction { DROP_ON_GROUND, GIVE_RANDOM_PARTICIPANT }
    public enum OfflineWinnerAction { DELIVER_ON_LOGIN, NEXT_HIGHEST_ROLL, DROP_ON_GROUND }
    public enum ChatMessages { ALL, RESULTS_ONLY, NONE }
    public enum VoteMode { PER_STACK, PER_ITEM, WHOLE_LOT }
    public enum ChatButtons { ALWAYS, CLIENTS_WITHOUT_MOD, NEVER }

    public static final ForgeConfigSpec CONFIG;

    // loot
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOOT_ENTITIES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_BLACKLIST;
    public static final ForgeConfigSpec.EnumValue<Rarity> MIN_RARITY;
    public static final ForgeConfigSpec.IntValue MIN_PLAYERS;
    public static final ForgeConfigSpec.EnumValue<VoteMode> VOTE_MODE;
    public static final ForgeConfigSpec.BooleanValue MERGE_STACKS;
    public static final ForgeConfigSpec.BooleanValue CAPTURE_NEARBY_DROPS;
    public static final ForgeConfigSpec.DoubleValue CAPTURE_RADIUS;
    public static final ForgeConfigSpec.IntValue CAPTURE_WINDOW;

    // vote
    public static final ForgeConfigSpec.ConfigValue<Integer> VOTE_DURATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> VOTE_RADIUS;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_LINE_OF_SIGHT;
    public static final ForgeConfigSpec.BooleanValue INCLUDE_KILLER;
    public static final ForgeConfigSpec.BooleanValue ALLOW_GREED;
    public static final ForgeConfigSpec.IntValue ROLL_MIN;
    public static final ForgeConfigSpec.IntValue ROLL_MAX;
    public static final ForgeConfigSpec.BooleanValue END_WHEN_ALL_VOTED;
    public static final ForgeConfigSpec.BooleanValue PASS_ON_DISCONNECT;
    public static final ForgeConfigSpec.EnumValue<NoWinnerAction> NO_WINNER_ACTION;
    public static final ForgeConfigSpec.EnumValue<OfflineWinnerAction> OFFLINE_WINNER_ACTION;
    public static final ForgeConfigSpec.EnumValue<ChatMessages> CHAT_MESSAGES;
    public static final ForgeConfigSpec.EnumValue<ChatButtons> CHAT_VOTE_BUTTONS;

    // commands
    public static final ForgeConfigSpec.IntValue STARTVOTE_PERMISSION_LEVEL;
    public static final ForgeConfigSpec.IntValue STARTVOTE_COOLDOWN;
    public static final ForgeConfigSpec.IntValue ROLL_COMMAND_RADIUS;

    // integrations
    public static final ForgeConfigSpec.ConfigValue<Boolean> ICE_AND_FIRE_INT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ALEXS_CAVES_INT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CHAOS_SPAWNER_INT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("loot");

        LOOT_ENTITIES = builder
                .comment("Entities whose death drops go to a loot vote.",
                        "Accepts entity IDs (minecraft:warden), entity type tags (#forge:bosses) and whole mods (alexscaves:*)")
                .defineList("loot_entities",
                        List.of("minecraft:warden"),
                        Config::isRegistryEntry
                );
        ITEM_BLACKLIST = builder
                .comment("Items that are never voted on and drop normally instead.",
                        "Same format as loot_entities: item IDs, item tags (#minecraft:arrows) or whole mods (somemod:*)")
                .defineList("item_blacklist", List.of(), Config::isRegistryEntry);
        MIN_RARITY = builder
                .comment("Items below this rarity drop normally instead of being voted on (COMMON = vote on everything)")
                .defineEnum("min_rarity", Rarity.COMMON);
        MIN_PLAYERS = builder
                .comment("Minimum number of eligible players for a vote to start. With fewer, loot simply drops on the ground.",
                        "1 = vote even when playing alone")
                .defineInRange("min_players", 1, 1, 100);
        VOTE_MODE = builder
                .comment("How a mob's loot is split into votes",
                        "PER_STACK - one vote per item stack (5 diamonds = one vote)",
                        "PER_ITEM - one vote per single item (5 diamonds = five votes)",
                        "WHOLE_LOT - one vote for everything the mob dropped")
                .defineEnum("vote_mode", VoteMode.PER_STACK);
        MERGE_STACKS = builder
                .comment("Merge identical items from the same mob into one stack before voting")
                .define("merge_identical_stacks", true);
        CAPTURE_NEARBY_DROPS = builder
                .comment("Also capture items that appear around a tracked mob while it is dying.",
                        "Catches bosses that drop loot their own way (after a death animation, one by one, etc.)",
                        "instead of through the normal death drops. Items thrown by players are never captured.")
                .define("capture_nearby_drops", true);
        CAPTURE_RADIUS = builder
                .comment("Distance in blocks around the dying mob's hitbox in which spawned items are captured")
                .defineInRange("capture_radius", 4.0, 0.0, 64.0);
        CAPTURE_WINDOW = builder
                .comment("Longest time in seconds to keep capturing drops after a tracked mob dies",
                        "(capturing also stops shortly after the mob's body disappears)")
                .defineInRange("capture_window", 30, 1, 600);
        builder.pop();


        builder.push("vote");
        VOTE_DURATION = builder
                .comment("Duration of the vote session in seconds (default: 30)")
                .define("vote_duration", 30, v -> v instanceof Integer i && i > 0 && i <= 3600);

        VOTE_RADIUS = builder
                .comment("Radius (in blocks) around the entity in which players can vote (default: 100)")
                .define("vote_radius", 100, v -> v instanceof Integer i && i > 0);
        REQUIRE_LINE_OF_SIGHT = builder
                .comment("Only players who can see the entity when it dies may vote")
                .define("require_line_of_sight", false);
        INCLUDE_KILLER = builder
                .comment("Always include the player who got the kill, even outside vote_radius (e.g. a long bow shot)")
                .define("include_killer", true);
        ALLOW_GREED = builder
                .comment("Allow the Greed option. When false players can only choose Need or Pass")
                .define("allow_greed", true);
        ROLL_MIN = builder
                .comment("Lowest possible roll")
                .defineInRange("roll_min", 1, 0, 1_000_000);
        ROLL_MAX = builder
                .comment("Highest possible roll (must be greater than roll_min)")
                .defineInRange("roll_max", 100, 1, 1_000_000);
        END_WHEN_ALL_VOTED = builder
                .comment("End the vote as soon as every participant has voted instead of waiting for the timer")
                .define("end_when_all_voted", true);
        PASS_ON_DISCONNECT = builder
                .comment("A participant who disconnects before voting automatically passes")
                .define("pass_on_disconnect", true);
        NO_WINNER_ACTION = builder
                .comment("What happens to an item everyone passed on (items from /startvote always go back to whoever offered them)",
                        "DROP_ON_GROUND - drop where the entity died",
                        "GIVE_RANDOM_PARTICIPANT - give it to a random participant")
                .defineEnum("no_winner_action", NoWinnerAction.DROP_ON_GROUND);
        OFFLINE_WINNER_ACTION = builder
                .comment("What happens when the winner is offline when the vote ends",
                        "DELIVER_ON_LOGIN - keep it and give it on their next login",
                        "NEXT_HIGHEST_ROLL - give it to the online player with the next highest roll in the same group",
                        "DROP_ON_GROUND - drop where the entity died")
                .defineEnum("offline_winner_action", OfflineWinnerAction.DELIVER_ON_LOGIN);
        CHAT_MESSAGES = builder
                .comment("Chat output: ALL (every vote and the results), RESULTS_ONLY, NONE")
                .defineEnum("chat_messages", ChatMessages.ALL);
        CHAT_VOTE_BUTTONS = builder
                .comment("Post clickable [Need] [Greed] [Pass] buttons in chat when a roll starts",
                        "ALWAYS - for everyone",
                        "CLIENTS_WITHOUT_MOD - only for players who don't have LootRoll installed (lets vanilla clients vote)",
                        "NEVER - GUI only")
                .defineEnum("chat_vote_buttons", ChatButtons.CLIENTS_WITHOUT_MOD);
        builder.pop();


        builder.push("commands");
        STARTVOTE_PERMISSION_LEVEL = builder
                .comment("Permission level needed for /startvote (0 = everyone, 2 = operators)")
                .defineInRange("startvote_permission_level", 0, 0, 4);
        STARTVOTE_COOLDOWN = builder
                .comment("Cooldown in seconds between /startvote uses per player (0 = none)")
                .defineInRange("startvote_cooldown", 0, 0, 86400);
        ROLL_COMMAND_RADIUS = builder
                .comment("Radius in blocks in which other players see /roll results")
                .defineInRange("roll_radius", 100, 1, 10_000);
        builder.pop();


        builder.push("integrations");

        ICE_AND_FIRE_INT = builder
                .comment("Enable loot voting for Ice and Fire dragons")
                .define("dragon_loot_voting", true);
        ALEXS_CAVES_INT = builder
                .comment("Enable loot voting for Luxtructosaurus(AlexsCaves)")
                .define("luxtructosaurus", true);
        CHAOS_SPAWNER_INT = builder
                .comment("Enable loot voting for ChaosSpawner(DungeonNowLoading)")
                .define("chaos_spawner", true);
        builder.pop();


        CONFIG = builder.build();
    }

    private static boolean isRegistryEntry(Object entry) {
        return entry instanceof String s && s.contains(":");
    }
}
