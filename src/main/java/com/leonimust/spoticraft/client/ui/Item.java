package com.leonimust.spoticraft.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.function.Function;

public class Item {

    private final ResourceLocation image;
    private final String name;
    private final Font font;
    private final String itemId;
    private final itemType type;
    public enum itemType {
        PLAYLIST,
        ALBUM,
        TRACK,
        EMPTY
    }

    public Item(ResourceLocation image, String playlistName, String id, itemType type, Font font) {
        this.image = image;
        this.name = playlistName;
        this.font = font;
        this.itemId = id;
        this.type = type;
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

    public void onClick() throws IOException, ParseException, SpotifyWebApiException {
        // empty object skip
        if (type == itemType.EMPTY) {
            return;
        }

        System.out.println("Item clicked: " + name);
        System.out.println("Item id: " + itemId);
        System.out.println("Item type: " + type);

        // play the music
        if (type == itemType.TRACK) {
            // probably a better way to do this tbh
            SpotifyScreen.spotifyApi.addItemToUsersPlaybackQueue(this.itemId).build().execute();
            SpotifyScreen.spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
            //update the ui
            //SpotifyScreen.syncData();
        }
    }
}
