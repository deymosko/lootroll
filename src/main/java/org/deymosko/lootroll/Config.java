package org.deymosko.lootroll;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class Config
{
    public static final ForgeConfigSpec CONFIG;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOOT_ENTITIES;

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
        CONFIG = builder.build();
    }
}
