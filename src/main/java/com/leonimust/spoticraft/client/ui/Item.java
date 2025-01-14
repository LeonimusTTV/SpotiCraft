package com.leonimust.spoticraft.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Function;

public class Item {

    private final ResourceLocation image;
    private final String name;
    private final Font font;

    public Item(ResourceLocation image, String playlistName, Font font) {
        this.image = image;
        this.name = playlistName;
        this.font = font;
    }

    public void draw(int x, int y, GuiGraphics graphics) {
        RenderSystem.setShaderTexture(0, image); // Bind the texture
        Function<ResourceLocation, RenderType> renderType = RenderType::guiTextured;
        int imageHeight = 40;
        int imageWidth = 40;

        graphics.blit(
                renderType,
                image,
                x,
                y, // height - imageHeight - 5
                0,
                0,
                imageWidth,
                imageHeight,
                imageWidth,
                imageHeight);

        graphics.drawString(font, name, x + imageWidth + 5, y + 10, 16777215);
    }
}
