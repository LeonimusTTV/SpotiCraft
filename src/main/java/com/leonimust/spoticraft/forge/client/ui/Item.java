package com.leonimust.spoticraft.forge.client.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

public class Item {

    private final ResourceLocation image;
    private final String name;
    private final Font font;
    private final String itemId;
    private final String itemUri;
    private final itemType type;
    private final String contextUri;
    public enum itemType {
        PLAYLIST,
        ALBUM,
        PLAY_ALBUM_PLAYLIST,
        TRACK,
        LIKED_TRACK,
        ARTIST,
        CATEGORY,
        EMPTY
    }

    public Item(ResourceLocation image, String name, String uri, String id, itemType type, String contextId, Font font) {
        this.image = image;
        this.name = name;
        this.font = font;
        this.itemUri = uri;
        this.itemId = id;
        this.type = type;
        this.contextUri = contextId;
    }

    public void draw(int x, int y, GuiGraphics graphics) {
        GpuTexture texture = Minecraft.getInstance().getTextureManager().getTexture(image).getTexture();
        RenderSystem.setShaderTexture(0, texture); // Now using GpuTexture instead of ResourceLocation
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

        graphics.drawString(font, name, x + imageWidth + 5, type == itemType.CATEGORY || type == itemType.PLAY_ALBUM_PLAYLIST || type == itemType.LIKED_TRACK ? y + 12 : y + 8, 16777215);

        if (type == itemType.EMPTY || type == itemType.CATEGORY || type == itemType.PLAY_ALBUM_PLAYLIST || type == itemType.LIKED_TRACK) {
            return;
        }

        graphics.drawString(font, String.valueOf(type), x + imageWidth + 5, y + 20, 0x808080);
    }

    public boolean isMouseOver(int mouseX, int mouseY, int x, int y) {
        int imageHeight = 30;
        int imageWidth = 30;

        // Check if the mouse coordinates are within the item's bounds
        return mouseX >= x && mouseX <= x + imageWidth + 5 + font.width(name)
                && mouseY >= y && mouseY <= y + imageHeight;
    }

    public void onClick() throws IOException, ParseException, SpotifyWebApiException, InterruptedException {
        // empty object skip
        if (type == itemType.EMPTY) {
            return;
        }

        System.out.println("Item clicked: " + name);
        System.out.println("Item id: " + itemId);
        System.out.println("Item uri: " + itemUri);
        System.out.println("Context uri: " + contextUri);
        System.out.println("Item type: " + type);

        // play the music
        if (type == itemType.TRACK) {
            try {
                if (!Objects.equals(contextUri, "") && contextUri != null) {
                    SpotifyScreen.spotifyApi.startResumeUsersPlayback().context_uri(contextUri).offset((JsonParser.parseString("{\"uri\":\"" + this.itemUri + "\"}")).getAsJsonObject()).build().execute();
                } else {
                    SpotifyScreen.spotifyApi.startResumeUsersPlayback().uris((JsonArray)JsonParser.parseString("[\"" + this.itemUri + "\"]")).build().execute();
                }
                // add a bit more delay
                Thread.sleep(250);
                //update the ui and wait to make sure the api give update to date info
                SpotifyScreen.getInstance().syncDataWithDelay();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                SpotifyScreen.getInstance().ShowTempMessage(e.getMessage());
            }
        }

        if (type == itemType.ALBUM) {
            SpotifyScreen.getInstance().showAlbum(this.itemId, this.itemUri);
        }

        if (type == itemType.PLAYLIST) {
            SpotifyScreen.getInstance().showPlaylist(this.itemId, this.itemUri);
        }

        if (type == itemType.PLAY_ALBUM_PLAYLIST) {
            try {
                SpotifyScreen.spotifyApi.startResumeUsersPlayback().context_uri(this.contextUri).build().execute();
                Thread.sleep(250);
                //update the ui and wait to make sure the api give update to date info
                SpotifyScreen.getInstance().syncDataWithDelay();
            } catch (IOException | SpotifyWebApiException | ParseException ignored) {

            }
        }

        if (type == itemType.LIKED_TRACK) {
            SpotifyScreen.getInstance().showLikedTracks();
        }

        if (type == itemType.ARTIST) {
            SpotifyScreen.getInstance().showArtist(this.itemId);
        }
    }
}
