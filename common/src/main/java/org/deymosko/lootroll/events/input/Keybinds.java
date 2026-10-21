package org.deymosko.lootroll.events.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;


public class Keybinds {

    public static final KeyMapping openVoteMenu = new KeyMapping(
            "key.lootroll.open_vote_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.misc"
    );

    public static void register() {
        KeyMappingRegistry.register(openVoteMenu);
    }
}
