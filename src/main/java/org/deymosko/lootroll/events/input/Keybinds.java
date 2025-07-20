package org.deymosko.lootroll.events.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.InputEvent;
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
    public void onKeyPress(InputEvent.Key event) {
        if (openVoteMenu.isActiveAndMatches(InputConstants.getKey(event.getKey(), event.getScanCode()))) {
            // TODO: Відкрити меню голосування
            System.out.println("[LootRoll] Відкриваємо меню голосування!");
        }
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        openVoteMenu = new KeyMapping(
                "key.lootroll.open_vote_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key.categories.misc"
        );
        event.register(openVoteMenu);
    }
}

