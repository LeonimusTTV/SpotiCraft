package com.leonimust.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemScrollPanel extends ElementListWidget<ItemScrollPanel.ItemEntry> {

    private List<Item> items = new ArrayList<>();

    public ItemScrollPanel(MinecraftClient client, int width, int height, int top, int left) {
        super(client, width, height, top, left);
    }

    public void setInfo(List<Item> content) {
        this.items = content;
        this.clearEntries();
        for (Item item : items) {
            this.addEntry(new ItemEntry(item));
        }
    }

    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    public class ItemEntry extends ElementListWidget.Entry<ItemEntry> {
        private final Item item;

        public ItemEntry(Item item) {
            this.item = item;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (item != null) {
                item.draw(x, y, context);
            }
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                int y = ItemScrollPanel.this.getRowLeft();
                for (Item item : items) {
                    if (item != null && item.isMouseOver((int) mouseX, (int) mouseY, y, y)) {
                        try {
                            item.onClick();
                        } catch (IOException | ParseException | SpotifyWebApiException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        return true;
                    }
                    int itemHeight = 35;
                    y += itemHeight;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }
    }
}
