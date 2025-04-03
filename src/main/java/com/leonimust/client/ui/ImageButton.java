package com.leonimust.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_NEAREST;

public class ImageButton extends ButtonWidget {
    private Identifier texture;
    private final int textureWidth;
    private final int textureHeight;

    public ImageButton(int x, int y, int width, int height, Identifier texture, int textureWidth, int textureHeight, String tooltipKey, PressAction onPress) {
        super(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.setTooltip(Tooltip.of(Text.translatable(tooltipKey)));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Set texture filtering to nearest-neighbor
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        context.drawTexture(
                texture,
                this.getX(),
                this.getY(),
                0,
                0,
                this.getWidth(),
                this.getHeight(),
                textureWidth,
                textureHeight
        );
    }

    public void setTexture(Identifier newTexture) {
        this.texture = newTexture;
    }

    public void setTooltip(String tooltipKey) {
        this.setTooltip(Tooltip.of(Text.translatable(tooltipKey)));
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
