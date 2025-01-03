package com.leonimust.spoticraft.client;

import org.json.JSONObject;
import se.michaelthelin.spotify.SpotifyApi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class TokenStorage {
    private static final File tokenFile = new File("spotify_tokens.json");

    public static JSONObject token;

    // Save the tokens in JSON format
    public static void saveToken(String accessToken, String refreshToken, int expiresIn) {
        JSONObject tokenJson = new JSONObject();
        tokenJson.put("access_token", accessToken);
        tokenJson.put("refresh_token", refreshToken);
        tokenJson.put("expires_in", expiresIn);
        tokenJson.put("timestamp", System.currentTimeMillis() + expiresIn * 1000L);

        // time
        /*Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(stamp.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(date);
        System.out.println(formattedDate);*/

        try (FileWriter writer = new FileWriter(tokenFile)) {
            writer.write(tokenJson.toString());
            token = tokenJson;
        } catch (IOException e) {
            System.out.println("Failed to write token to file : " + e.getMessage());
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
        }
    }

    public static void checkIfExpired() throws IOException {
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
