package com.leonimust.spoticraft.client.ui;

import com.leonimust.spoticraft.SpotiCraft;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

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
import java.util.function.Function;

import static com.leonimust.spoticraft.SpotiCraft.LOGGER;

public class ImageHandler {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final HashMap<String, ResourceLocation> CACHE = new HashMap<>();
    private static final File CACHE_DIR = new File(MC.gameDirectory, "spoticraft/cache");

    static {
        if (!CACHE_DIR.exists()) {
            boolean result = CACHE_DIR.mkdirs();
            if (!result) {
                throw new RuntimeException("Unable to create directory " + CACHE_DIR);
            }
        }
    }

    public static void drawImage(GuiGraphics graphics, ResourceLocation musicImage, int height, int imageHeight, int imageWidth) {
        RenderSystem.setShaderTexture(0, musicImage); // Bind the texture
        Function<ResourceLocation, RenderType> renderType = RenderType::guiTextured;
        graphics.blit(
                renderType,
                musicImage,
                5,
                height - imageHeight - 5,
                0,
                0,
                imageWidth,
                imageHeight,
                imageWidth,
                imageHeight);
    }

    public static ResourceLocation downloadImage(String url) {
        try {
            LOGGER.info("downloadImage called for {}", url);

            // Check cache first
            if (CACHE.containsKey(url)) {
                LOGGER.info("cache");
                return CACHE.get(url);
            }

            // Generate a hash for the URL to use as a filename
            String fileName = UUID.nameUUIDFromBytes(url.getBytes()) + ".png";
            File cachedFile = new File(CACHE_DIR, fileName);

            // Check if the image is already downloaded
            if (cachedFile.exists()) {
                LOGGER.info("Image found in cache: {}", cachedFile.getAbsolutePath());
                return loadFromDisk(cachedFile, url);
            }

            // Download the image
            URL imageUrl = new URI(url).toURL();
            try (InputStream inputStream = imageUrl.openStream()) {
                Files.copy(inputStream, cachedFile.toPath());
            }

            LOGGER.info("Downloaded image to {}", cachedFile.getAbsolutePath());

            // Check if the file is a WebP image
            if (isWebP(cachedFile)) {
                LOGGER.info("Image is a WebP file, converting to PNG...");
                File pngFile = convertWebPToPng(cachedFile);
                return loadFromDisk(pngFile, url);
            }

            // If not WebP, load the file directly
            return loadFromDisk(cachedFile, url);

        } catch (Exception e) {
            LOGGER.error("Failed to load image from {}: {}", url, e.getMessage(), e);
            return null; // Return null if something goes wrong
        }
    }

    private static ResourceLocation loadFromDisk(File file, String url) throws Exception {
        LOGGER.info("Loading cached image : {}", file.getAbsolutePath());
        BufferedImage bufferedImage = ImageIO.read(file);

        // Convert BufferedImage to NativeImage
        NativeImage nativeImage = convertToNativeImage(bufferedImage);

        // Create a DynamicTexture from the NativeImage
        DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);

        // Register the texture in Minecraft's TextureManager
        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/spotify_cover_" + UUID.randomUUID());
        MC.getTextureManager().register(textureLocation, dynamicTexture);

        // Cache the texture
        CACHE.put(url, textureLocation);
        return textureLocation;
    }

    private static NativeImage convertToNativeImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, true);

        // Transfer pixels from BufferedImage to NativeImage
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = bufferedImage.getRGB(x, y); // Get pixel in ARGB format
                nativeImage.setPixel(x, y, argb);
            }
        }

        return nativeImage;
    }

    /**
     * Checks if the file is a WebP image by inspecting its magic bytes.
     */
    private static boolean isWebP(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] magicBytes = inputStream.readNBytes(12); // Properly read the first 12 bytes
            String header = new String(magicBytes, StandardCharsets.US_ASCII);
            return header.contains("WEBP");
        } catch (Exception e) {
            LOGGER.error("Error checking WebP format: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Converts a WebP file to PNG using the dwebp command-line tool.
     */
    private static File convertWebPToPng(File webpFile) throws Exception {
        String pngFileName = webpFile.getName().replace(".webp", ".png");
        File pngFile = new File(webpFile.getParent(), pngFileName);

        Process process = new ProcessBuilder("dwebp", webpFile.getAbsolutePath(), "-o", pngFile.getAbsolutePath())
                .redirectErrorStream(true)
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            try (InputStream errorStream = process.getInputStream()) {
                String errorMessage = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                LOGGER.error("Failed to convert WebP to PNG: {}", errorMessage);
            }
            throw new RuntimeException("Failed to convert WebP image to PNG. Exit code: " + exitCode);
        }

        LOGGER.info("Converted WebP to PNG: {}", pngFile.getAbsolutePath());
        return pngFile;
    }
}
