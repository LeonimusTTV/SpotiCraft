package com.leonimust.spoticraft.neoforge.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

import com.leonimust.spoticraft.neoforge.client.TokenStorage;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

//TODO optimize this if I feel like I wanna do it :p
public class SpotifyAuthHandler {

    // not the best way but 🤫
    private static final String CLIENT_ID = "d108b6364fff46f2b17c03145e48040a";
    // client secret was here 👀 no need to search for it, it has been refreshed :3
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String SCOPES = "user-read-playback-state user-modify-playback-state user-read-private playlist-read-private playlist-read-collaborative playlist-modify-private playlist-modify-public user-library-read user-library-modify";
    private static final String ENCODED_SCOPES = URLEncoder.encode(SCOPES, StandardCharsets.UTF_8);
    private static String codeVerifier;

    public static void startAuthFlow() throws Exception {
        codeVerifier = generateRandomString();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        openAuthUrl(codeChallenge);
    }

    private static String generateRandomString() {
        SecureRandom random = new SecureRandom();
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 64; i++) {
            int index = random.nextInt(possible.length()); // Ensures valid index range (0-61)
            result.append(possible.charAt(index));
        }

        return result.toString();
    }

    private static String generateCodeChallenge(String codeVerifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    public static void exchangeCodeForToken(String code) throws IOException, URISyntaxException {
        String url = "https://accounts.spotify.com/api/token";
        String data = "client_id=" + CLIENT_ID +
                "&grant_type=authorization_code" +
                "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                "&code_verifier=" + URLEncoder.encode(codeVerifier, StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(data.getBytes(StandardCharsets.UTF_8));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = reader.lines().reduce("", (acc, line) -> acc + line);
            System.out.println(response);
            JSONObject responseBody = new JSONObject(response);
            System.out.println(responseBody);
            TokenStorage.saveToken(
                    responseBody.getString("access_token"),
                    responseBody.getString("refresh_token"),
                    responseBody.getInt("expires_in")
            );
            System.out.println("Access Token Response: " + response);

            Minecraft.getInstance().setScreen(null);
        }
    }

    public static boolean refreshAccessToken(String refreshToken) throws IOException, URISyntaxException {
        System.out.println("Refresh Token: " + refreshToken);
        String url = "https://accounts.spotify.com/api/token";
        String data = "client_id=" + CLIENT_ID +
                "&grant_type=refresh_token" +
                "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(data.getBytes(StandardCharsets.UTF_8));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = reader.lines().reduce("", (acc, line) -> acc + line);
            JSONObject responseBody = new JSONObject(response);
            System.out.println("Refresh Token Response: " + responseBody);
            TokenStorage.saveToken(
                    responseBody.getString("access_token"),
                    refreshToken,
                    responseBody.getInt("expires_in")
            );
            System.out.println("Refresh Token Response: " + response);
            return true;
        }
    }

    private static void openAuthUrl(String codeChallenge) {
        try {
            String authUrl = "https://accounts.spotify.com/authorize?" +
                    "response_type=code" +
                    "&client_id=" + CLIENT_ID +
                    "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8) +
                    "&code_challenge_method=S256" +
                    "&code_challenge=" + codeChallenge +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

            String osName = System.getProperty("os.name");

            if (Objects.equals(osName, "Mac OS X")) {
                new ProcessBuilder("open", authUrl).start();
            } else if (osName.contains("Windows")) {
                // Pass the full command as a single argument to cmd /c
                System.out.println(authUrl);
                new ProcessBuilder("cmd", "/c", "start", "\"\" \"" + authUrl + "\"").start();
            } else {
                System.err.println("Unsupported OS: " + osName);
            }

            // Start callback server
            new CallbackServer(8080);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
