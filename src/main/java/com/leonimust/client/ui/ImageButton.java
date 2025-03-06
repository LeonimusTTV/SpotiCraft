package com.leonimust.client.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(
                RenderLayer::getGuiTextured,
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
