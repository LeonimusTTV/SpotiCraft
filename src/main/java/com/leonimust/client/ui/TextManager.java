package com.leonimust.client.ui;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class TextManager {

    private final TextRenderer textRenderer; // Reference to the font renderer
    private String text = ""; // Current text to display
    private int centerX = 0;
    private int y = 0;
    private int color = 0xFFFFFF; // Default white color
    private boolean shouldDraw = false; // Flag to control rendering

    public TextManager(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
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
    public void drawText(DrawContext drawContext) {
        if (this.shouldDraw && !this.text.isEmpty()) {
            int textWidth = this.textRenderer.getWidth(this.text);
            drawContext.drawText(this.textRenderer, Text.literal(this.text), centerX - textWidth / 2, y, color, false);
        }
    }
}