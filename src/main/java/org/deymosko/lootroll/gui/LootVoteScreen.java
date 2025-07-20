package org.deymosko.lootroll.gui;

import com.mojang.blaze3d.systems.RenderSystem;
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
    private Button needButton, greedButton, passButton;

    public LootVoteScreen(UUID voteId, ItemStack item) {
        super(Component.literal("Loot Vote"));
        this.voteId = voteId;
        this.itemStack = item;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        needButton = Button.builder(Component.literal("Need"), b -> vote("need"))
                .pos(centerX - 60, centerY + 30).size(40, 20).build();
        greedButton = Button.builder(Component.literal("Greed"), b -> vote("greed"))
                .pos(centerX - 10, centerY + 30).size(40, 20).build();
        passButton = Button.builder(Component.literal("Pass"), b -> vote("pass"))
                .pos(centerX + 40, centerY + 30).size(40, 20).build();

        addRenderableWidget(needButton);
        addRenderableWidget(greedButton);
        addRenderableWidget(passButton);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);

        int centerX = width / 2;
        int centerY = height / 2;

        // Фрейм
        RenderSystem.setShaderTexture(0, GuiTextures.LOOTFRAME);
        gui.blit(GuiTextures.LOOTFRAME, centerX - 80, centerY - 40, 0, 0, 160, 80);

        // Предмет
        gui.renderItem(itemStack, centerX - 8, centerY - 20);

        // Таймер
        gui.drawString(this.font, "Time left: " + (timerTicks / 20), centerX - 30, centerY - 35, 0xFFFFFF);

        super.render(gui, mouseX, mouseY, partialTick);
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

