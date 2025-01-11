package com.leonimust.spoticraft.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScrollableContainer extends AbstractWidget {
    private final List<String> content;
    private final Font font;
    private int scrollOffset = 0; // Offset in lines
    private final int lineHeight = 12; // Line height in pixels
    private final int visibleLines;
    public final int x, y, width, height;

    public ScrollableContainer(int x, int y, int width, int height, Font font, List<String> content) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.content = content;
        this.font = font;
        this.visibleLines = height / lineHeight;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Draw background
        graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, 0xFF202020);

        // Draw visible content
        for (int i = 0; i < visibleLines; i++) {
            int contentIndex = i + scrollOffset;
            if (contentIndex >= 0 && contentIndex < content.size()) {
                String line = content.get(contentIndex);
                graphics.drawString(font, line, this.x + 5, this.y + i * lineHeight + 5, 0xFFFFFF);
            }
        }

        // Draw scrollbar if necessary
        if (content.size() > visibleLines) {
            int scrollbarHeight = Math.max((int) ((double) visibleLines / content.size() * this.height), 10);
            int scrollbarY = this.y + (int) ((double) scrollOffset / content.size() * this.height);

            graphics.fill(
                    this.x + this.width - 5, scrollbarY,
                    this.x + this.width, scrollbarY + scrollbarHeight,
                    0xFFCCCCCC
            );
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {
        // No narration updates needed
    }

    /**
     * Handles scroll events.
     *
     * @param amount Amount to scroll (-1 for up, +1 for down).
     */
    public void scroll(int amount) {
        int maxScrollOffset = Math.max(0, content.size() - visibleLines);

        // Adjust the scroll offset, clamping within bounds
        this.scrollOffset = Math.max(0, Math.min(scrollOffset + amount , maxScrollOffset));
    }
}