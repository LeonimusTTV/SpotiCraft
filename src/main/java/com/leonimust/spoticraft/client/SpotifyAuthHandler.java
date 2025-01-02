package com.leonimust.spoticraft.client;

import java.net.URI;

import net.minecraft.client.Minecraft;
import okhttp3.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import org.json.JSONObject;

public class SpotifyAuthHandler {

    private static final String CLIENT_ID = "d108b6364fff46f2b17c03145e48040a";
    private static final String CLIENT_SECRET = "cd9d5622ec944476a5fb204a656e3614";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String SCOPES = "user-read-playback-state user-modify-playback-state";
    private static final String ENCODED_SCOPES = URLEncoder.encode(SCOPES, StandardCharsets.UTF_8);

    //private static TokenStorage tokenStorage = TokenStorage.getInstance();

    public static void exchangeCodeForToken(String code) throws IOException {
        String url = "https://accounts.spotify.com/api/token";
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI)
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
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
        }
    }

    public static void refreshAccessToken(String refreshToken) throws IOException {
        String url = "https://accounts.spotify.com/api/token";
        OkHttpClient client = new OkHttpClient();

        String authHeader = "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)  // Use the saved refresh token here
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", authHeader)  // Add the Authorization header
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                JSONObject responseBody = new JSONObject(response.body().string());
                System.out.println("Refresh token response: " + responseBody);
                // Parse and store the new access token
                TokenStorage.saveToken(responseBody.getString("access_token"), refreshToken, responseBody.getInt("expires_in"));  // Store the new token
            } else {
                System.err.println("Failed to refresh token: " + response.message());
            }
        }
    }

    public static void startAuthFlow() {
        try {
            /*if (TokenStorage.loadToken() != null) {
                System.out.println("TokenStorage has something");
                return;
            }*/

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
