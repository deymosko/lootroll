package org.deymosko.lootroll.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** "Loot won!" toast with the item icon, styled like the vanilla advancement toast. */
public class LootWonToast implements Toast {
    private static final long DISPLAY_MS = 5000;
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("toast/advancement");

    private final List<ItemStack> items;

    public LootWonToast(List<ItemStack> items) {
        this.items = items;
    }

    @Override
    public Visibility render(GuiGraphics gui, ToastComponent toastComponent, long timeSinceLastVisible) {
        Font font = toastComponent.getMinecraft().font;
        ItemStack first = items.get(0);

        gui.blitSprite(BACKGROUND, 0, 0, width(), height());
        gui.drawString(font, Component.translatable("lootroll.toast.won").withStyle(ChatFormatting.YELLOW), 30, 7, 0xFFFFFF, false);

        String name = first.getHoverName().getString();
        if (items.size() > 1) {
            name += " +" + (items.size() - 1);
        }
        gui.drawString(font, font.plainSubstrByWidth(name, width() - 36), 30, 18, VoteUIEntry.rarityColor(first), false);

        gui.renderFakeItem(first, 8, 8);
        gui.renderItemDecorations(font, first, 8, 8);

        return timeSinceLastVisible >= DISPLAY_MS * toastComponent.getNotificationDisplayTimeMultiplier()
                ? Visibility.HIDE : Visibility.SHOW;
    }
}
