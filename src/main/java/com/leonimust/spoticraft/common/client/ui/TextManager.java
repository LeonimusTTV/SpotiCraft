package com.leonimust.spoticraft.common.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TextManager {

    private final Font font; // Reference to the font renderer
    private String key = ""; // Current text to display
    private int centerX = 0;
    private int y = 0;
    private int color = 0xFFFFFF; // Default white color
    private boolean shouldDraw = false; // Flag to control rendering

    public TextManager(Font font) {
        this.font = font;
    }

    // Method to set text
    public void setText(String text, int centerX, int y, int color) {
        this.key = text;
        this.centerX = centerX;
        this.y = y;
        this.color = color;
        this.shouldDraw = true; // Enable drawing
    }

    // Method to clear text
    public void clearText() {
        this.key = "";
        this.shouldDraw = false; // Disable drawing
    }

    // Method to draw the text
    public void drawText(GuiGraphics guiGraphics) {
        if (this.shouldDraw && !this.key.isEmpty()) {
            int textWidth = this.font.width(this.key);
            guiGraphics.drawString(this.font, Component.translatable(this.key), centerX - textWidth / 2, y, color);
        }
    }
}