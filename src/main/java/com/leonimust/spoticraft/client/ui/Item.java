package com.leonimust.spoticraft.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class Item {

    private final ResourceLocation image;
    private final String name;
    private final String description;
    private final Font font;

    public Item(ResourceLocation image, String playlistName, String description, Font font) {
        this.image = image;
        this.name = playlistName;
        this.font = font;
        this.description = description;
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

        graphics.drawString(font, name, x + imageWidth, y + 10, 16777215);
        graphics.drawString(font, description, x + imageWidth, y + 25, 16777215);
    }
}
