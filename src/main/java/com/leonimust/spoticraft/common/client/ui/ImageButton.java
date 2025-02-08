package com.leonimust.spoticraft.common.client.ui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class ImageButton extends Button {

    private ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;

    public ImageButton(int x, int y, int width, int height, ResourceLocation texture, int textureWidth, int textureHeight, String tooltipKey, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        guiGraphics.blit(
                texture, // RenderType
                this.getX(),               // X position on screen
                this.getY(),               // Y position on screen
                0,                         // Start of texture U
                0,                         // Start of texture V
                this.getWidth(),           // Rendered width
                this.getHeight(),          // Rendered height
                textureWidth,              // Full texture width
                textureHeight              // Full texture height
        );
    }

    public void setTexture(ResourceLocation newTexture) {
        this.texture = newTexture;
    }

    public void setTooltip(String tooltipKey) {
        this.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}