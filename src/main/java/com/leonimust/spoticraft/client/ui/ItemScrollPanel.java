package com.leonimust.spoticraft.client.ui;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemScrollPanel extends ScrollPanel {

    private List<Item> items = new ArrayList<>();
    private final int itemHeight = 35;

    public ItemScrollPanel(Minecraft mc, int width, int height, int top, int left) {
        super(mc, width, height, top, left);
    }

    public void setInfo(List<Item> content) {
        this.items = content;
    }

    @Override
    public int getContentHeight() {
        // -1 because of the empty item
        int height = (items.size()-1) * itemHeight;
        if (height < this.bottom - this.top - 8) {
            height = this.bottom - this.top - 8;
        }
        return height;
    }

    @Override
    protected int getScrollAmount() {
        return itemHeight*3;
    }

    @Override
    protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
        if (items == null || items.isEmpty()) {
            return;
        }
        //System.out.println(items.size());
        for (Item item : items) {
            if (item != null) {
                item.draw(left, relativeY, guiGraphics);
            }
            relativeY += itemHeight;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left mouse button
            int relativeY = top + border - (int) scrollDistance; // Starting Y position, accounting for scrolling

            for (Item item : items) {
                if (item != null) {
                    if (item.isMouseOver((int) mouseX, (int) mouseY, left, relativeY)) {
                        try {
                            item.onClick(); // Trigger the item's click action
                        } catch (IOException | ParseException | SpotifyWebApiException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        return true;
                    }
                }
                relativeY += itemHeight;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {

    }
}