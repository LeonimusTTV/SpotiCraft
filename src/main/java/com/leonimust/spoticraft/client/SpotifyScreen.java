package com.leonimust.spoticraft.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.leonimust.spoticraft.client.TokenStorage.token;

public class SpotifyScreen extends Screen {

    private GuiGraphics graphics;
    private int totalDurationMs;
    private int currentProgressMs;
    private boolean musicPlaying = false;
    private long lastUpdateTime;

    private static SpotifyApi spotifyApi;
    private Timer updateTimer;

    private final int barWidth = 300;
    private final int barHeight = 7;

    @Override
    public void init() {
        if (token == null) {
            return;
        }
        // Initialize the Spotify API client
        spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(TokenStorage.token.getString("access_token"))
                .setRefreshToken(TokenStorage.token.getString("refresh_token"))
                .build();

        // Sync playback state when the screen is opened
        syncPlaybackState();

        // Set up a timer to update progress every second
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (musicPlaying) {
                    long currentTime = System.currentTimeMillis();
                    int elapsedMs = (int) (currentTime - lastUpdateTime);
                    currentProgressMs = Math.min(currentProgressMs + elapsedMs, totalDurationMs);
                    lastUpdateTime = currentTime; // Update the last sync time
                }
            }
        }, 0, 1000);
    }

    public SpotifyScreen() {
        super(Component.translatable("gui.spoticraft.spotify_player"));
    }

    private void loginScreen() {
        this.drawCenteredString(graphics, "It seems you aren't logged in, press the button bellow to log yourself.", this.width / 2, 20, 16777215);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, button ->
                SpotifyAuthHandler.startAuthFlow()
        ).bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
    }

    private void mainScreen() {
        // Add a button that closes the screen
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OK, button -> {
            try {
                TokenStorage.checkIfExpired();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (musicPlaying) {
                spotifyApi.pauseUsersPlayback().build().executeAsync();
                musicPlaying = false;
            } else {
                spotifyApi.startResumeUsersPlayback().build().executeAsync();
                syncPlaybackState();
                musicPlaying = true;
            }
        }).bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());

        this.drawMusicControlBar(graphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Draw the background
        guiGraphics.fill(0, 0, this.width, this.height, 0xff080404); // Semi-transparent black

        // Draw the title at the top center of the screen
        //drawCenteredString(guiGraphics, this.title.getString(), this.width / 2, 20, 0xFFFFFF);

        graphics = guiGraphics;

        // Render all buttons and other widgets
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (token == null) {
            loginScreen();
        } else {
            spotifyApi = new SpotifyApi.Builder()
                    .setAccessToken(token.getString("access_token"))
                    .setRefreshToken(token.getString("refresh_token"))
                    .build();
            mainScreen();
        }
    }

    private void syncPlaybackState() {
        try {
            CurrentlyPlayingContext context = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();
            if (context != null && context.getItem() != null) {
                totalDurationMs = context.getItem().getDurationMs();
                currentProgressMs = context.getProgress_ms();
                musicPlaying = context.getIs_playing();
                lastUpdateTime = System.currentTimeMillis(); // Sync the timer with Spotify's state
            }
        } catch (Exception e) {
            System.out.println("Failed to sync playback state : " + e.getMessage());
        }
    }

    private void drawMusicControlBar(GuiGraphics graphics) {
        int barX = this.width / 2 - barWidth / 2;
        int barY = this.height - 20;

        // Draw the background of the bar
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFFCCCCCC);

        // Draw the filled portion of the bar
        int filledWidth = (int) ((currentProgressMs / (float) totalDurationMs) * barWidth);
        graphics.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFFFFFFFF);

        // Draw the time
        String currentTime = formatTime(currentProgressMs / 1000);
        String durationTime = formatTime(totalDurationMs / 1000);
        drawCenteredString(graphics, currentTime, this.width / 2 - ((barWidth + 30) / 2), barY - ((barHeight - 5) / 2), 0xFFFFFF);
        drawCenteredString(graphics, durationTime, this.width / 2 + ((barWidth + 30) / 2), barY - ((barHeight - 5) / 2), 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int barX = this.width / 2 - barWidth / 2;
        int barY = this.height - 100;

        if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
            currentProgressMs = (int) (((mouseX - barX) / barWidth) * totalDurationMs);
            try {
                spotifyApi.seekToPositionInCurrentlyPlayingTrack(currentProgressMs).build().executeAsync();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int barX = this.width / 2 - barWidth / 2;
        int barY = this.height - 100;

        // Check if dragging is within the bounds of the progress bar
        if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
            try {
                spotifyApi.seekToPositionInCurrentlyPlayingTrack(currentProgressMs).build().executeAsync();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int barX = this.width / 2 - barWidth / 2;
        int barY = this.height - 100;

        // Check if dragging is within the bounds of the progress bar
        if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
            // Update the music progress as the user drags
            currentProgressMs = (int) (((mouseX - barX) / barWidth) * totalDurationMs);
            currentProgressMs = Math.max(0, Math.min(currentProgressMs, totalDurationMs)); // Clamp between 0 and total duration
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    public void drawCenteredString(GuiGraphics guiGraphics, String text, int centerX, int y, int color) {
        // Calculate the text width and draw it centered
        int textWidth = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - textWidth / 2, y, color);
    }

    @Override
    public void onClose() {
        // Stop the timer when the screen is closed
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Pause the game when this screen is open
    }
}