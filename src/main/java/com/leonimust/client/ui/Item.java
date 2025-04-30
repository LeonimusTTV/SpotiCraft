package com.leonimust.client.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.Objects;

public class Item {

    private final Identifier image;
    private final String name;
    private final TextRenderer font;
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

    public Item(Identifier image, String name, String uri, String id, itemType type, String contextId, TextRenderer font) {
        this.image = image;
        this.name = name;
        this.font = font;
        this.itemUri = uri;
        this.itemId = id;
        this.type = type;
        this.contextUri = contextId;
    }

    public void draw(int x, int y, DrawContext graphics) {
        int imageHeight = 30;
        int imageWidth = 30;

        GpuTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(image).getGlTexture();
        RenderSystem.setShaderTexture(0, texture);

        graphics.drawTexture(RenderLayer::getGuiTextured, image, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        graphics.drawText(font, name, x + imageWidth + 5, type == itemType.CATEGORY || type == itemType.PLAY_ALBUM_PLAYLIST || type == itemType.LIKED_TRACK ? y + 12 : y + 8, 16777215, false);

        if (type == itemType.EMPTY || type == itemType.CATEGORY || type == itemType.PLAY_ALBUM_PLAYLIST || type == itemType.LIKED_TRACK) {
            return;
        }

        graphics.drawText(font, String.valueOf(type), x + imageWidth + 5, y + 20, 0x808080, false);
    }

    public boolean isMouseOver(int mouseX, int mouseY, int x, int y) {
        int imageHeight = 30;
        int imageWidth = 30;
        return mouseX >= x && mouseX <= x + imageWidth + 5 + font.getWidth(name)
                && mouseY >= y && mouseY <= y + imageHeight;
    }

    public void onClick() throws IOException, ParseException, SpotifyWebApiException, InterruptedException {
        if (type == itemType.EMPTY) {
            return;
        }

        System.out.println("Item clicked: " + name);
        System.out.println("Item id: " + itemId);
        System.out.println("Item uri: " + itemUri);
        System.out.println("Context uri: " + contextUri);
        System.out.println("Item type: " + type);

        if (type == itemType.TRACK) {
            try {
                if (!Objects.equals(contextUri, "") && contextUri != null) {
                    SpotifyScreen.spotifyApi.startResumeUsersPlayback().context_uri(contextUri).offset(JsonParser.parseString("{\"uri\":\"" + this.itemUri + "\"}").getAsJsonObject()).build().execute();
                } else {
                    SpotifyScreen.spotifyApi.startResumeUsersPlayback().uris((JsonArray) JsonParser.parseString("[\"" + this.itemUri + "\"]")).build().execute();
                }
                Thread.sleep(250);
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