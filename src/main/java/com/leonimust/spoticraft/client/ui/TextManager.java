package com.leonimust.spoticraft.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

public class TextManager {

    private final Font font; // Reference to the font renderer
    private String text = ""; // Current text to display
    private int centerX = 0;
    private int y = 0;
    private int color = 0xFFFFFF; // Default white color
    private boolean shouldDraw = false; // Flag to control rendering

    public TextManager(Font font) {
        this.font = font;
    }

    // Method to set text
    public void setText(String text, int centerX, int y, int color) {
        this.text = text;
        this.centerX = centerX;
        this.y = y;
        this.color = color;
        this.shouldDraw = true; // Enable drawing
    }

    // Method to clear text
    public void clearText() {
        this.text = "";
        this.shouldDraw = false; // Disable drawing
    }

    // Method to draw the text
    public void drawText(GuiGraphics guiGraphics) {
        if (this.shouldDraw && !this.text.isEmpty()) {
            int textWidth = this.font.width(this.text);
            guiGraphics.drawString(this.font, this.text, centerX - textWidth / 2, y, color);
        }
    }
}