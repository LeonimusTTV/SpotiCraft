package com.leonimust.client.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ItemScrollPanel extends ScrollableWidget {

    private List<Item> items = new ArrayList<>();
    private final int itemHeight = 35;

    // Scrollbar dragging state
    private boolean isScrolling;
    private double startMouseY;
    private double startScrollY;

    public ItemScrollPanel(int width, int height, int x, int y) {
        super(x, y, width, height, Text.of(""));
    }

    public void setInfo(List<Item> content) {
        this.items = content;
        this.setScrollY(0);
    }

    @Override
    protected int getContentsHeightWithPadding() {
        return (items.size()-1) * itemHeight;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return itemHeight * 3;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (items == null || items.isEmpty()) return;

        // Enable scissor to clip content
        context.enableScissor(
                this.getX(),
                this.getY(),
                this.getX() + this.width,
                this.getY() + this.height
        );

        // Render items with scroll offset
        int relativeY = (int) (this.getY() - this.getScrollY());
        for (Item item : items) {
            if (item != null) {
                item.draw(this.getX(), relativeY, context);
            }
            relativeY += itemHeight;
        }

        // Disable scissor
        context.disableScissor();

        // Draw scrollbar if needed
        if (overflows()) {
            drawScrollbar(context);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && overflows()) {
            if (isMouseOverScrollbar(mouseX, mouseY)) {
                isScrolling = true;
                startMouseY = mouseY;
                startScrollY = getScrollY();
                return true;
            }
        }

        // Existing item click handling
        int relativeY = (int) (this.getY() - this.getScrollY());
        for (Item item : items) {
            if (item != null && item.isMouseOver((int) mouseX, (int) mouseY, this.getX(), relativeY)) {
                try {
                    item.onClick();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
            relativeY += itemHeight;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isScrolling) {
            double maxScrollY = getMaxScrollY();
            double scrollDelta = (mouseY - startMouseY) * (maxScrollY / (this.height - this.getScrollbarThumbHeight()));
            setScrollY(MathHelper.clamp(startScrollY + scrollDelta, 0, maxScrollY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isScrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        int scrollbarX = this.getX() + this.width - 6; // Match scrollbarWidth
        return mouseX >= scrollbarX &&
                mouseX <= scrollbarX + 6 && // Match scrollbarWidth
                mouseY >= this.getScrollbarThumbY() &&
                mouseY <= this.getScrollbarThumbY() + this.getScrollbarThumbHeight();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // Narration implementation
    }
}