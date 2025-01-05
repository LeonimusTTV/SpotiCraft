package com.leonimust.spoticraft.server;

import java.net.URI;

import com.leonimust.spoticraft.client.SpotifyScreen;
import com.leonimust.spoticraft.client.TokenStorage;
import net.minecraft.client.Minecraft;
import okhttp3.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.json.JSONObject;

public class SpotifyAuthHandler {

    // not the best way but 🤫
    private static final String CLIENT_ID = "d108b6364fff46f2b17c03145e48040a";
    // client secret was here 👀 no need to search for it, it has been refreshed :3
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String SCOPES = "user-read-playback-state user-modify-playback-state user-read-private playlist-read-private playlist-read-collaborative playlist-modify-private playlist-modify-public";
    private static final String ENCODED_SCOPES = URLEncoder.encode(SCOPES, StandardCharsets.UTF_8);

    private static final String BASE_URL = "https://spoticraft.leonimust.com";

    public static void exchangeCodeForToken(String code) {
        String url = BASE_URL + "/exchangeCodeForToken?code=" + code;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                JSONObject responseBody = new JSONObject(response.body().string());
                System.out.println("Access token response: " + responseBody);

                //refreshAccessToken(responseBody.getString("refresh_token"));
                // Parse and store the access token
                TokenStorage.saveToken(responseBody.getString("access_token"),
                        responseBody.getString("refresh_token"), responseBody.getInt("expires_in"));

                //spotifyScreen.loginSuccess();
                Minecraft.getInstance().setScreen(null);
                Minecraft.getInstance().setScreen(new SpotifyScreen());
            } else {
                System.err.println("Failed to exchange code: " + response.message());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean refreshAccessToken(String refreshToken) throws IOException {
        String url = BASE_URL + "/refreshToken?refresh_token=" + refreshToken;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                JSONObject responseBody = new JSONObject(response.body().string());
                System.out.println("Refresh token response: " + responseBody);
                // Parse and store the new access token
                TokenStorage.saveToken(responseBody.getString("access_token"), refreshToken, responseBody.getInt("expires_in"));  // Store the new token
                return responseBody.getBoolean("success");
            } else {
                System.err.println("Failed to refresh token: " + response.message());
            }
        }
        return false;
    }

    public static void startAuthFlow() {
        try {
            String authUrl = String.format(
                    "https://accounts.spotify.com/authorize?client_id=%s&response_type=code&redirect_uri=%s&scope=%s",
                    CLIENT_ID, URI.create(REDIRECT_URI), URI.create(ENCODED_SCOPES)
            );

            String osName = System.getProperty("os.name");

            if (Objects.equals(osName, "Mac OS X")) {
                new ProcessBuilder("open", authUrl).start();
            } else if (osName.contains("Windows")) {
                new ProcessBuilder("start", authUrl).start();
            } else {
                System.err.println("Unsupported OS: " + osName);
            }

            // Start callback server
            new CallbackServer(8080);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
