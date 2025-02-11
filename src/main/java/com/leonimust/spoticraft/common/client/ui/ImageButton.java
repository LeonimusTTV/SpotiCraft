package com.leonimust.spoticraft.common.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import static org.lwjgl.opengl.GL11.*;


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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Set texture filtering to nearest-neighbor
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        guiGraphics.blit(
                texture,
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