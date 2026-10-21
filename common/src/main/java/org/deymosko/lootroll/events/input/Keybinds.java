package org.deymosko.lootroll.events.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;


public class Keybinds {

    private static final String CATEGORY = "key.categories.lootroll";

    public static final KeyMapping openVoteMenu = key("key.lootroll.open_vote_menu", GLFW.GLFW_KEY_H);
    // Vote on the oldest active roll (the top HUD frame) without opening a screen.
    public static final KeyMapping voteNeed = key("key.lootroll.vote_need", GLFW.GLFW_KEY_J);
    public static final KeyMapping voteGreed = key("key.lootroll.vote_greed", GLFW.GLFW_KEY_K);
    public static final KeyMapping votePass = key("key.lootroll.vote_pass", GLFW.GLFW_KEY_N);

    private static KeyMapping key(String name, int defaultKey) {
        return new KeyMapping(name, InputConstants.Type.KEYSYM, defaultKey, CATEGORY);
    }

    public static void register() {
        KeyMappingRegistry.register(openVoteMenu);
        KeyMappingRegistry.register(voteNeed);
        KeyMappingRegistry.register(voteGreed);
        KeyMappingRegistry.register(votePass);
    }
}
