package org.deymosko.lootroll;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class Config
{
    public static final ForgeConfigSpec CONFIG;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOOT_ENTITIES;
    public static final ForgeConfigSpec.ConfigValue<Integer> VOTE_DURATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> VOTE_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ICE_AND_FIRE_INT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("loot");

        LOOT_ENTITIES = builder
                .comment("List of entity IDs that should trigger loot voting (example: minecraft:warden)")
                .defineList("loot_entities",
                        List.of("minecraft:warden"),
                        entry -> entry instanceof String && ((String) entry).contains(":")
                );
        builder.pop();


        builder.push("vote");
        VOTE_DURATION = builder
                .comment("Duration of the vote session in seconds (default: 30)")
                .define("vote_duration", 30);

        VOTE_RADIUS = builder
                .comment("Radius (in blocks) around the entity in which players can vote (default: 100)")
                .define("vote_radius", 100);
        builder.pop();



        builder.push("integrations");

        ICE_AND_FIRE_INT = builder
                .comment("Enable loot voting for Ice and Fire dragons")
                .define("dragon_loot_voting", true);

        builder.pop();


        CONFIG = builder.build();
    }
}
