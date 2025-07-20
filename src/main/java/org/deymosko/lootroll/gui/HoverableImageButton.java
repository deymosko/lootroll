package org.deymosko.lootroll.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class HoverableImageButton extends AbstractWidget {
    private ResourceLocation normalTexture;
    private ResourceLocation hoverTexture;
    private final int texWidth, texHeight;
    private final Runnable onClick;

    public HoverableImageButton(int x, int y, int width, int height,
                                ResourceLocation normalTexture,
                                ResourceLocation hoverTexture,
                                Runnable onClick,
                                String label) {
        super(x, y, width, height, Component.literal(label));
        this.normalTexture = normalTexture;
        this.hoverTexture = hoverTexture;
        this.texWidth = width;
        this.texHeight = height;
        this.onClick = onClick;
    }

    public void setHoverTexture(ResourceLocation hoverTexture)
    {
        this.hoverTexture = hoverTexture;
    }
    public ResourceLocation getHoverTexture()
    {
        return hoverTexture;
    }

    public void setNormalTexture(ResourceLocation normalTexture)
    {
        this.normalTexture = normalTexture;
    }
    public ResourceLocation getNormalTexture()
    {
        return normalTexture;
    }
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = this.isHoveredOrFocused() ? hoverTexture : normalTexture;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f,     1f, 1f, 1f);
        guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, texWidth, texHeight);
        RenderSystem.disableBlend();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.onClick.run();
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}


