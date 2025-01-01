package com.leonimust.spoticraft.client;

import net.minecraftforge.fml.ISystemReportExtender;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class TokenStorage {
    private static final File tokenFile = new File("spotify_tokens.json");

    // Save the tokens in JSON format
    public static void saveToken(String accessToken, String refreshToken, int expiresIn) {
        JSONObject tokenJson = new JSONObject();
        tokenJson.put("access_token", accessToken);
        tokenJson.put("refresh_token", refreshToken);
        tokenJson.put("expires_in", expiresIn);
        tokenJson.put("timestamp", System.currentTimeMillis());

        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(stamp.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(date);
        System.out.println(formattedDate);

        try (FileWriter writer = new FileWriter(tokenFile)) {
            writer.write(tokenJson.toString());
        } catch (IOException e) {
            System.out.println("Failed to write token to file : " + e.getMessage());
        }
    }

    // Load the tokens from the JSON file
    public static JSONObject loadToken() {
        try {
            if (tokenFile.exists()) {
                String content = new String(Files.readAllBytes(tokenFile.toPath()));
                return new JSONObject(content);
            } else {
                return null;
            }
        } catch (IOException e) {
            System.out.println("Failed to read token from file : " + e.getMessage());
        }
        return null;
    }
}
