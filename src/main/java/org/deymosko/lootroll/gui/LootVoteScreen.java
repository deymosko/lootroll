package org.deymosko.lootroll.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.enums.VoteType;
import org.deymosko.lootroll.network.Packets;
import org.deymosko.lootroll.network.c2s.VoteC2SPacket;

import java.util.UUID;

public class LootVoteScreen extends Screen {
    private final ItemStack itemStack;
    private final UUID voteId;
    private int timerTicks = 600; // 30 секунд при 20 тік/сек
    private HoverableImageButton needButton, greedButton, passButton;

    public LootVoteScreen(UUID voteId, ItemStack item) {
        super(Component.literal("Loot Vote"));
        this.voteId = voteId;
        this.itemStack = item;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        needButton = new HoverableImageButton(centerX + 28, centerY+5, 10, 10, GuiTextures.NEED_BUTTON, GuiTextures.NEED_BUTTON, () ->
        {
            vote(VoteType.NEED.toString());
        }, "");
        greedButton = new HoverableImageButton(centerX + 28, centerY+17, 10, 10, GuiTextures.GREED_BUTTON, GuiTextures.GREED_BUTTON, () ->
        {
            vote(VoteType.GREED.toString());
        }, "");
        passButton = new HoverableImageButton(centerX + 41, centerY, 7, 7, GuiTextures.PASS_BUTTON, GuiTextures.PASS_BUTTON, () ->
        {
            vote(VoteType.PASS.toString());
        }, "");

        addRenderableWidget(needButton);
        addRenderableWidget(greedButton);
        addRenderableWidget(passButton);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        Minecraft mc = Minecraft.getInstance();
        int centerX = width / 2;
        int centerY = height / 2;


        // Координати предмета
        int itemX = centerX - 40;
        int itemY = centerY + 8;

        gui.renderItem(itemStack, itemX, itemY);

// Якщо курсор наведений на предмет — показати tooltip
        if (mouseX >= itemX && mouseX <= itemX + 16 && mouseY >= itemY && mouseY <= itemY + 16) {

            gui.renderTooltip(this.font, itemStack, mouseX, mouseY);
        }



        // Фрейм
        RenderSystem.setShaderTexture(0, GuiTextures.LOOTFRAME);
        gui.blit(GuiTextures.LOOTFRAME, centerX - 48, centerY, 0, 0, 96, 32, 96, 32);

        // Предмет
        gui.renderItem(itemStack, centerX - 40, centerY + 8);

        drawScaledString(gui, this.font, itemStack.getDisplayName().getString(), centerX-21, centerY + 10, 0.4f, 0xFFFFFF);



        // Таймер
        gui.drawString(this.font, "Time left: " + (timerTicks / 20), centerX - 30, centerY - 35, 0xFFFFFF);

        super.render(gui, mouseX, mouseY, partialTick);
    }
    public static void drawScaledString(GuiGraphics guiGraphics, Font font, String text, float screenX, float screenY, float scale, int color) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        // Коригуємо координати під масштаб
        float scaledX = screenX / scale;
        float scaledY = screenY / scale;

        pose.scale(scale, scale, 1.0f);
        guiGraphics.drawString(font, text, scaledX, scaledY, color, false);

        pose.popPose();
    }



    @Override
    public void tick() {
        if (--timerTicks <= 0) {
            onTimeout();
        }
    }

    private void vote(String type) {
        VoteType voteType = switch (type) {
            case "need" -> VoteType.NEED;
            case "greed" -> VoteType.GREED;
            default -> VoteType.PASS;
        };

        Packets.sendToServer(new VoteC2SPacket(voteId, voteType));
        ClientVoteCache.poll();
        minecraft.setScreen(null); // Закриває екран
    }

    private void onTimeout() {
        // Якщо час вийшов — автоматичне "pass"
        vote("pass");
    }
}

