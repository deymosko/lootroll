package org.deymosko.lootroll.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.ClientVoteCache;
import org.deymosko.lootroll.enums.VoteType;
import org.deymosko.lootroll.events.VoteSession;
import org.deymosko.lootroll.network.Packets;
import org.deymosko.lootroll.network.c2s.VoteC2SPacket;

import java.util.List;
import java.util.UUID;

public class LootVoteScreen extends Screen {
    private final List<ItemStack> itemStack;
    private final UUID voteId;
    private final long endTime;
    private int timerTicks;
    private HoverableImageButton needButton, greedButton, passButton;
    private VoteSession session;
    private int overlay = 0;


    public LootVoteScreen(UUID voteId, List<ItemStack> item, long endTime) {
        super(Component.literal("Loot Vote"));
        this.voteId = voteId;
        this.itemStack = item;
        this.endTime = endTime;
        this.timerTicks = (int) Math.ceil(Math.max(0, endTime - System.currentTimeMillis()) / 50.0);
    }


    private int guiElementsX(int x)
    {
        overlay++;
        return x * overlay;
    }
    private int guiElementsY(int y)
    {
        overlay++;
        return y * overlay;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        needButton = new HoverableImageButton(centerX - 80 + 117, centerY+80+6, 16, 16, GuiTextures.NEED_BUTTON, GuiTextures.NEED_BUTTON_HOVER, () ->
        {
            vote(VoteType.NEED.toString());
        }, "");
        greedButton = new HoverableImageButton(centerX -80 + 116, centerY+80+25, 17, 10, GuiTextures.GREED_BUTTON, GuiTextures.GREED_BUTTON_HOVER, () ->
        {
            vote(VoteType.GREED.toString());
        }, "");
        passButton = new HoverableImageButton(centerX + -80 + 139, centerY + 80 + 6, 14, 14, GuiTextures.PASS_BUTTON, GuiTextures.PASS_BUTTON_HOVER, () ->
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
        ItemStack stack = itemStack.get(0);


        // Координати предмета
        int itemX = centerX - 80 + 8;
        int itemY = centerY + 80 + 10;

        gui.renderItem(itemStack.get(0), itemX, itemY);

// Якщо курсор наведений на предмет — показати tooltip
        if (mouseX >= itemX && mouseX <= itemX + 16 && mouseY >= itemY && mouseY <= itemY + 16) {

            gui.renderTooltip(this.font, itemStack.get(0), mouseX, mouseY);
        }


        // Фрейм
        RenderSystem.setShaderTexture(0, GuiTextures.LOOTFRAME);
        gui.blit(GuiTextures.LOOTFRAME, centerX - 80, centerY+80, 0, 0, 160, 40, 160, 40);

        // Предмет
        gui.renderItem(itemStack.get(0), itemX, itemY);

        drawScaledString(gui, this.font, itemStack.get(0).getDisplayName().getString(), centerX-80+33, centerY + 80+9, 0.4f, 0xFFFFFF);
        float progress = timerTicks / 600.0f;
        drawProgressBar(gui, progress, centerX-80+6, centerY+80+33, 104, 5, 0xFFB2CA5D);


        // Таймер
        gui.drawString(this.font, "Time left: " + (int)Math.ceil(Math.max(0, endTime - System.currentTimeMillis()) / 1000.0), centerX - 30, centerY - 35, 0xFFFFFF);
        if (stack.getCount() > 1) {
            String countText = String.valueOf(stack.getCount());
            gui.drawString(
                    this.font,
                    countText,
                    itemX + 17 - this.font.width(countText),
                    itemY + 9,
                    0xFFFFFF,
                    true
            );
        }

        super.render(gui, mouseX, mouseY, partialTick);
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        // Не малюємо затемнення
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

    private void drawProgressBar(GuiGraphics graphics, float progress, int x, int y, int width, int height, int color)
    {
        progress = Math.min(Math.max(progress, 0.0f), 1.0f);
        int filledWidth = (int)(width * progress);
        int filledHeight = (int)(height * progress);
        graphics.fill(x, y, x + filledWidth, y + height, color);

    }



    @Override
    public void tick() {
        timerTicks = (int) Math.ceil(Math.max(0, endTime - System.currentTimeMillis()) / 50.0);
        if (timerTicks <= 0) {
            onTimeout();
        }
    }

    private void vote(String type) {
        VoteType voteType = switch (type) {
            case "NEED" -> VoteType.NEED;
            case "GREED" -> VoteType.GREED;
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

