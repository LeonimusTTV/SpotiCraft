package com.leonimust.spoticraft.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;

public class SpotifyScreen extends Screen {

    public SpotifyScreen() {
        super(Component.translatable("gui.spoticraft.spotify_player"));
    }

    @Override
    protected void init() {
        // Add a button that closes the screen
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OK, button -> {
            Minecraft.getInstance().setScreen(null);
        }).bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Draw the background
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000); // Semi-transparent black

        // Draw the title at the top center of the screen
        drawCenteredString(guiGraphics, this.title.getString(), this.width / 2, 20, 0xFFFFFF);

        // Render all buttons and other widgets
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public void drawCenteredString(GuiGraphics guiGraphics, String text, int centerX, int y, int color) {
        // Calculate the text width and draw it centered
        int textWidth = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - textWidth / 2, y, color);
    }

    @Override
    public boolean isPauseScreen() {
        return true; // Pause the game when this screen is open
    }
}