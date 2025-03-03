package com.leonimust.spoticraft.neoforge.client;

import com.leonimust.spoticraft.neoforge.client.ui.SpotifyScreen;
import com.leonimust.spoticraft.neoforge.server.SpotifyAuthHandler;
import net.minecraft.client.Minecraft;
import org.json.JSONObject;
import se.michaelthelin.spotify.SpotifyApi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import static com.leonimust.spoticraft.Main.LOGGER;

public class TokenStorage {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final File tokenFile = new File(MC.gameDirectory, "spoticraft/spotify_tokens.json");
    private static final File DIR = new File(MC.gameDirectory, "spoticraft");

    static {
        if (!DIR.exists()) {
            boolean result = DIR.mkdirs();
            if (!result) {
                throw new RuntimeException("Unable to create directory " + DIR);
            }
        }
    }

    public static JSONObject token;

    // Save the tokens in JSON format
    public static void saveToken(String accessToken, String refreshToken, int expiresIn) {
        System.out.println("Saving access token: " + accessToken);
        LOGGER.info("Saving access token: {}", accessToken);
        JSONObject tokenJson = new JSONObject();
        tokenJson.put("access_token", accessToken);
        tokenJson.put("refresh_token", refreshToken);
        tokenJson.put("expires_in", expiresIn);
        // removed 250 out of 3600 from expire so we avoid having an expired token
        tokenJson.put("timestamp", System.currentTimeMillis() + (expiresIn - 250) * 1000L);

        try (FileWriter writer = new FileWriter(tokenFile)) {
            writer.write(tokenJson.toString());
            token = tokenJson;
            System.out.println("Saved token to " + tokenFile.getAbsolutePath());
            LOGGER.info("Saved token to {}", tokenFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to write token to file : " + e.getMessage());
            throw new RuntimeException("Failed to write token to file : " + e.getMessage());
        }
    }

    // Load the tokens from the JSON file
    public static void loadToken() {
        try {
            if (tokenFile.exists()) {
                String content = new String(Files.readAllBytes(tokenFile.toPath()));
                token = new JSONObject(content);
            }
        } catch (IOException e) {
            System.out.println("Failed to read token from file : " + e.getMessage());
            throw new RuntimeException("Failed to read token from file : " + e.getMessage());
        }
    }

    public static void checkIfExpired() throws IOException, URISyntaxException {
        synchronized (TokenStorage.class) { // Synchronize to avoid concurrent modifications
            if (token == null) {
                loadToken();
            }

            if (token.getLong("timestamp") <= System.currentTimeMillis()) {
                System.out.println("Token is expired");

                // Refresh the token and wait for completion
                boolean refreshed = SpotifyAuthHandler.refreshAccessToken(token.getString("refresh_token"));
                if (!refreshed) {
                    throw new IOException("Failed to refresh the token");
                }

                SpotifyScreen.spotifyApi = new SpotifyApi.Builder()
                        .setAccessToken(token.getString("access_token"))
                        .setRefreshToken(token.getString("refresh_token"))
                        .build();
            }
        }
    }
}
