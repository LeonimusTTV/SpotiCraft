package com.leonimust.spoticraft.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

import com.leonimust.spoticraft.client.TokenStorage;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static com.leonimust.spoticraft.SpotiCraft.LOGGER;

//TODO optimize this if I feel like I wanna do it :p
public class SpotifyAuthHandler {

    // not the best way but 🤫
    private static final String CLIENT_ID = "d108b6364fff46f2b17c03145e48040a";
    // client secret was here 👀 no need to search for it, it has been refreshed :3
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String SCOPES = "user-read-playback-state user-modify-playback-state user-read-private playlist-read-private playlist-read-collaborative playlist-modify-private playlist-modify-public user-library-read";
    private static final String ENCODED_SCOPES = URLEncoder.encode(SCOPES, StandardCharsets.UTF_8);

    private static final String BASE_URL = "https://spoticraft.leonimust.com";

    public static void exchangeCodeForToken(String code) {
        System.out.println("Exchange code for token: " + code);
        LOGGER.info("Exchange code for token: {}", code);

        String urlString = BASE_URL + "/exchangeCodeForToken?code=" + code;
        LOGGER.info(urlString);

        HttpURLConnection connection = null;
        try {
            URL url = new URI(urlString).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            LOGGER.info("Response code: {}", responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject responseBody = getJsonObject(connection);

                System.out.println("Access token response: " + responseBody);
                LOGGER.info("Access token response: {}", responseBody);

                // Save the token data
                TokenStorage.saveToken(
                        responseBody.getString("access_token"),
                        responseBody.getString("refresh_token"),
                        responseBody.getInt("expires_in")
                );

                // reset the screen so everything loads again with the access_token now available
                Minecraft.getInstance().setScreen(null);
            } else {
                LOGGER.info("Failed to exchange code: {}", connection.getResponseMessage());
                throw new RuntimeException("Failed to exchange code: " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute request: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static @NotNull JSONObject getJsonObject(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
        );
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }
        reader.close();

        String responseBodyString = responseBuilder.toString();
        return new JSONObject(responseBodyString);
    }

    public static boolean refreshAccessToken(String refreshToken) {
        String urlString = BASE_URL + "/refreshToken?refresh_token=" + refreshToken;
        LOGGER.info("Refreshing token with URL: {}", urlString);

        HttpURLConnection connection = null;
        try {
            URL url = new URI(urlString).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            LOGGER.info("Response code: {}", responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject responseBody = getJsonObject(connection);

                System.out.println("Refresh token response: " + responseBody);
                LOGGER.info("Refresh token response: {}", responseBody);

                // Parse and store the new access token
                TokenStorage.saveToken(
                        responseBody.getString("access_token"),
                        refreshToken,
                        responseBody.getInt("expires_in")
                );

                return responseBody.getBoolean("success");
            } else {
                LOGGER.error("Failed to refresh token: {}", connection.getResponseMessage());
                throw new RuntimeException("Failed to refresh token: " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute request: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void startAuthFlow() {
        try {
            String authUrl =
                    "https://accounts.spotify.com/authorize?client_id=" + CLIENT_ID +
                            "&response_type=code&redirect_uri=" + URI.create(REDIRECT_URI) +
                            "&scope=" + URI.create(ENCODED_SCOPES);

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
