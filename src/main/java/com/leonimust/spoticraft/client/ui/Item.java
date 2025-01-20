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
        int imageHeight = 30;
        int imageWidth = 30;

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

    public boolean isMouseOver(int mouseX, int mouseY, int x, int y) {
        int imageHeight = 30;
        int imageWidth = 30;

        // Check if the mouse coordinates are within the item's bounds
        return mouseX >= x && mouseX <= x + imageWidth + 5 + font.width(name)
                && mouseY >= y && mouseY <= y + imageHeight;
    }

    public void onClick() {
        // means it's the empty item and we should skip it
        if (Objects.equals(this.name, "")) {
            return;
        }

        System.out.println("Item clicked: " + name);
    }
}
