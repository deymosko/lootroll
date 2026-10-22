package org.deymosko.lootroll;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Per-player UI settings (lootroll-client.toml). */
public class ClientConfig
{
    public enum Anchor { TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    public static final ModConfigSpec CONFIG;

    public static final ModConfigSpec.BooleanValue HUD_ENABLED;
    public static final ModConfigSpec.EnumValue<Anchor> HUD_ANCHOR;
    public static final ModConfigSpec.IntValue HUD_OFFSET_X;
    public static final ModConfigSpec.IntValue HUD_OFFSET_Y;
    public static final ModConfigSpec.IntValue HUD_MAX_FRAMES;

    public static final ModConfigSpec.BooleanValue PLAY_SOUND;
    public static final ModConfigSpec.BooleanValue OPEN_SCREEN_ON_VOTE;
    public static final ModConfigSpec.BooleanValue WIN_TOAST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("hud");
        HUD_ENABLED = builder
                .comment("Show active loot rolls on the HUD")
                .define("enabled", true);
        HUD_ANCHOR = builder
                .comment("Screen corner/edge the roll frames are attached to")
                .defineEnum("anchor", Anchor.CENTER_RIGHT);
        HUD_OFFSET_X = builder
                .comment("Horizontal offset in GUI pixels (positive = right)")
                .defineInRange("offset_x", 0, -2000, 2000);
        HUD_OFFSET_Y = builder
                .comment("Vertical offset in GUI pixels (positive = down)")
                .defineInRange("offset_y", 0, -2000, 2000);
        HUD_MAX_FRAMES = builder
                .comment("How many rolls are shown at once; the rest are summarised as \"+N more\"")
                .defineInRange("max_frames", 3, 1, 10);
        builder.pop();

        builder.push("notifications");
        PLAY_SOUND = builder
                .comment("Play a sound when a new loot roll starts")
                .define("play_sound", true);
        OPEN_SCREEN_ON_VOTE = builder
                .comment("Open the vote screen automatically when a roll starts (only if no other screen is open)")
                .define("open_screen_on_vote", false);
        WIN_TOAST = builder
                .comment("Show a toast when you win an item")
                .define("win_toast", true);
        builder.pop();

        CONFIG = builder.build();
    }
}
