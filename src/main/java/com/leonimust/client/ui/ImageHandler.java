package com.leonimust.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.*;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.UUID;

import static com.leonimust.SpotiCraft.LOGGER;
import static com.leonimust.SpotiCraft.MOD_ID;

public class ImageHandler {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final TextureManager TEXTURE_MANAGER = MC.getTextureManager();
    private static final HashMap<String, Identifier> CACHE = new HashMap<>();
    private static final File CACHE_DIR = new File(MC.runDirectory, "spoticraft/cache");
    private static final Identifier EMPTY = Identifier.of(MOD_ID, "textures/gui/empty.png");

    static {
        if (!CACHE_DIR.exists() && !CACHE_DIR.mkdirs()) {
            throw new RuntimeException("Unable to create directory " + CACHE_DIR);
        }
    }

    public static void drawImage(DrawContext context, Identifier musicImage, int height, int imageHeight, int imageWidth) {
        context.drawTexture(RenderLayer::getGuiTextured, musicImage, 5, height - imageHeight - 5, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    public static Identifier downloadImage(String url) {
        try {
            LOGGER.info("downloadImage called for {}", url);

            if (CACHE.containsKey(url)) {
                LOGGER.info("Image found in cache");
                return CACHE.get(url);
            }

            String fileName = UUID.nameUUIDFromBytes(url.getBytes()) + ".png";
            File cachedFile = new File(CACHE_DIR, fileName);

            if (cachedFile.exists()) {
                LOGGER.info("Image found in cache: {}", cachedFile.getAbsolutePath());
                return loadFromDisk(cachedFile, url);
            }

            URL imageUrl = new URI(url).toURL();
            try (InputStream inputStream = imageUrl.openStream()) {
                Files.copy(inputStream, cachedFile.toPath());
            }

            LOGGER.info("Downloaded image to {}", cachedFile.getAbsolutePath());

            return loadFromDisk(cachedFile, url);
        } catch (Exception e) {
            LOGGER.error("Failed to load image from {}: {}", url, e.getMessage(), e);
            return EMPTY;
        }
    }

    private static Identifier loadFromDisk(File file, String url) throws Exception {
        LOGGER.info("Loading cached image: {}", file.getAbsolutePath());
        BufferedImage bufferedImage = ImageIO.read(file);
        if (bufferedImage == null) {
            return EMPTY;
        }

        NativeImage nativeImage = convertToNativeImage(bufferedImage);
        NativeImageBackedTexture dynamicTexture = new NativeImageBackedTexture(nativeImage);
        Identifier textureLocation = Identifier.of(MOD_ID, "textures/gui/spotify_cover_" + UUID.randomUUID());
        TEXTURE_MANAGER.registerTexture(textureLocation, dynamicTexture);
        CACHE.put(url, textureLocation);
        return textureLocation;
    }

    private static NativeImage convertToNativeImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        NativeImage nativeImage = new NativeImage(width, height, true);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = bufferedImage.getRGB(x, y);
                nativeImage.setColorArgb(x, y, argb);
            }
        }
        return nativeImage;
    }
}
