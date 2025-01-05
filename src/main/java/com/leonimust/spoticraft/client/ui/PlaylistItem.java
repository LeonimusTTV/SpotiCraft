package com.leonimust.spoticraft.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class PlaylistItem {

    private final ResourceLocation image;
    private final String name;
    private final String description;
    private final Font font;
    private final GuiGraphics graphics;

    public PlaylistItem(ResourceLocation image, String playlistName, String description, Font font, GuiGraphics graphics) {
        this.image = image;
        this.name = playlistName;
        this.font = font;
        this.description = description;
        this.graphics = graphics;
    }

    public void draw(int x, int y) {

        RenderSystem.setShaderTexture(0, image); // Bind the texture
        Function<ResourceLocation, RenderType> renderType = RenderType::guiTextured;
        int imageHeight = 50;
        int imageWidth = 50;

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

        graphics.drawString(font, name, x + imageWidth, y + 13, 16777215);
        graphics.drawString(font, description, x + imageWidth, y + 27, 16777215);
    }

    public ResourceLocation getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
