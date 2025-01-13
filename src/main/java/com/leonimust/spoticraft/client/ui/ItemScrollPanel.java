package com.leonimust.spoticraft.client.ui;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ItemScrollPanel extends ScrollPanel {

    private List<Item> items;
    private final int itemHeight = 40;

    public ItemScrollPanel(Minecraft mc, int width, int height, int top, int left) {
        super(mc, width, height, top, left);
    }

    public void setInfo(List<Item> content) {
        this.items = content;
    }

    void clearInfo() {
        this.items = Collections.emptyList();
    }

    @Override
    public int getContentHeight() {
        int height = items.size() * itemHeight;
        if (height < this.bottom - this.top - 15) {
            height = this.bottom - this.top - 15;
        }
        return height;
    }

    @Override
    protected int getScrollAmount() {
        return itemHeight;
    }

    @Override
    protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
        int baseY = relativeY;
        for (Item item : items) {
            if (item != null) {
                item.draw(left, baseY, guiGraphics);
            }
            baseY += itemHeight;
        }
    }
    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {

    }
}