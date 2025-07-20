package org.deymosko.lootroll.events.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;


public class Keybinds {

    public static KeyMapping openVoteMenu;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new Keybinds());
    }


    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        openVoteMenu = new KeyMapping(
                "key.lootroll.open_vote_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.categories.misc"
        );
        event.register(openVoteMenu);
    }
}

