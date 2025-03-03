package com.leonimust.spoticraft.neoforge.server;

import com.leonimust.spoticraft.neoforge.server.SpotifyAuthHandler;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.List;

import static com.leonimust.spoticraft.Main.LOGGER;

public class CallbackServer extends NanoHTTPD {

    public CallbackServer(int port) {
        super(port);
        try {
            start(SOCKET_READ_TIMEOUT, false);
            System.out.println("Callback server started on port " + port);
        } catch (IOException e) {
            System.out.println("Port already used : " + port);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        List<String> code = session.getParameters().get("code");
        if (code != null) {
            System.out.println("Authorization code received: " + code);
            LOGGER.info("Authorization code received: {}", code);
            // Exchange code for token
            new Thread(() -> {
                try {
                    //System.out.println(code.getFirst());
                    SpotifyAuthHandler.exchangeCodeForToken(code.get(0));

                    this.stop();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    LOGGER.info(e.getMessage());
                    throw new RuntimeException(e);
                }
            }).start();
            return newFixedLengthResponse("Authorization successful! You can close this window.");
        } else {
            return newFixedLengthResponse("Error: No code received.");
        }
    }
}