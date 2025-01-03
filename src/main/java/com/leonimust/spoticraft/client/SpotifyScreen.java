package com.leonimust.spoticraft.client;

import com.leonimust.spoticraft.SpotiCraft;
import com.leonimust.spoticraft.client.ui.ImageButton;
import com.leonimust.spoticraft.client.ui.TextManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ProductType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.leonimust.spoticraft.client.TokenStorage.token;

public class SpotifyScreen extends Screen {

    private GuiGraphics graphics;
    private int totalDurationMs;
    private int currentProgressMs;
    private boolean musicPlaying = false;
    private long lastUpdateTime;
    private boolean shuffleState = false;

    private ImageButton playStopButton;
    private ImageButton shuffleButton;
    private ImageButton repeatButton;

    public static SpotifyApi spotifyApi;
    private Timer updateTimer;

    private final int barWidth = 300;
    private final int barHeight = 7;

    private boolean userPremium = false;

    private TextManager textManager;
    private Timer tempMessageTimer;

    ResourceLocation PLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/play.png");
    ResourceLocation PAUSE_TEXTURE = ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/pause.png");

    private final String[] trackList = {"off", "context", "track"};
    private int trackIndex = 0;

    @Override
    public void init() {
        if (token == null) {
            return;
        }

        this.textManager = new TextManager(this.font);

        try {
            TokenStorage.checkIfExpired();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Initialize the Spotify API client
        spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(TokenStorage.token.getString("access_token"))
                .setRefreshToken(TokenStorage.token.getString("refresh_token"))
                .build();

        try {
            userPremium = spotifyApi.getCurrentUsersProfile().build().execute().getProduct() == ProductType.PREMIUM;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }

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

                    if (currentProgressMs >= totalDurationMs) {
                        syncPlaybackState();
                    }
                }
            }
        }, 0, 1000);
    }

    public SpotifyScreen() {
        super(Component.translatable("gui.spoticraft.spotify_player"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Draw the background
        guiGraphics.fill(0, 0, this.width, this.height, 0xff080404);

        // Draw the title at the top center of the screen
        //drawCenteredString(guiGraphics, this.title.getString(), this.width / 2, 20, 0xFFFFFF);

        graphics = guiGraphics;

        // Render all buttons and other widgets
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // if no token is found that means the user is not logged
        if (token == null) {
            loginScreen();
        } else {
            // check if the user has premium or not
            if (!userPremium) {
                noPremium();
            } else {
                mainScreen();
            }
        }
    }

    // screens
    private void loginScreen() {
        this.drawCenteredString(graphics, "It seems you aren't logged in, press the button bellow to log yourself.", this.width / 2, 20, 16777215);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, button ->
                SpotifyAuthHandler.startAuthFlow()
        ).bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
    }

    private void mainScreen() {
        //Minecraft ImageButton is shit and doesn't work ;_; thanks for the 4 hours of lost time xD
        if (playStopButton == null) {
            playStopButton = new ImageButton(
                    this.width / 2 - 10,
                    this.height - 50,
                    20, // Button width
                    20, // Button height
                    musicPlaying ? PAUSE_TEXTURE : PLAY_TEXTURE,  // Use stop texture if playing, otherwise play texture
                    20, // Full texture width
                    20, // Full texture height
                    musicPlaying ? "gui.spoticraft.pause" : "gui.spoticraft.play",
                    button -> toggleMusicPlayback() // Toggle playback on click
            );
        }

        // Update the texture if the music playing state has changed
        playStopButton.setTexture(musicPlaying ? PAUSE_TEXTURE : PLAY_TEXTURE);

        // Update the tooltip if the music playing state has changed
        playStopButton.setTooltip(musicPlaying ? "gui.spoticraft.pause" : "gui.spoticraft.play");

        ImageButton nextButton = new ImageButton(
                this.width / 2 + 15,
                this.height - 48,
                16, // Button width
                16, // Button height
                ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/next.png"),  // Use stop texture if playing, otherwise play texture
                16, // Full texture width
                16, // Full texture height
                "gui.spoticraft.next",
                button -> {
                    try {
                        spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
                        syncPlaybackState();
                    } catch (IOException | SpotifyWebApiException | ParseException e) {
                        ShowTempMessage("No device found !");
                    }
                }
        );

        ImageButton previousButton = new ImageButton(
                this.width / 2 - 30,
                this.height - 47,
                15, // Button width
                15, // Button height
                ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/previous.png"),  // Use stop texture if playing, otherwise play texture
                15, // Full texture width
                15, // Full texture height
                "gui.spoticraft.previous",
                button -> {
                    try {
                        spotifyApi.skipUsersPlaybackToPreviousTrack().build().execute();
                        syncPlaybackState();
                    } catch (IOException | SpotifyWebApiException | ParseException e) {
                        ShowTempMessage("No device found !");
                    }
                }
        );

        previousButton.setActive(!shuffleState);

        if (shuffleButton == null) {
            shuffleButton = new ImageButton(
                    this.width / 2 - 50,
                    this.height - 47,
                    15, // Button width
                    15, // Button height
                    ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/shuffle.png"),  // Use stop texture if playing, otherwise play texture
                    15, // Full texture width
                    15, // Full texture height
                    shuffleState ? "gui.spoticraft.disable-shuffle" : "gui.spoticraft.enable-shuffle",
                    button -> {
                        try {
                            spotifyApi.toggleShuffleForUsersPlayback(!shuffleState).build().execute();
                            shuffleState = !shuffleState;
                            shuffleButton.setTooltip(shuffleState ? "gui.spoticraft.disable-shuffle" : "gui.spoticraft.enable-shuffle");

                            previousButton.setActive(!shuffleState);
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            ShowTempMessage("No device found !");
                        }
                    }
            );
        }

        if (repeatButton == null) {
            repeatButton = new ImageButton(
                    this.width / 2 + 35,
                    this.height - 47,
                    15, // Button width
                    15, // Button height
                    ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/repeat.png"),  // Use stop texture if playing, otherwise play texture
                    15, // Full texture width
                    15, // Full texture height
                    trackIndex == 0 ? "gui.spoticraft.enable-repeat" : trackIndex == 1 ? "gui.spoticraft.enable-repeat-one" : "gui.spoticraft.disable-repeat",
                    button -> {
                        try {
                            trackIndex = (trackIndex + 1) % trackList.length;
                            spotifyApi.setRepeatModeOnUsersPlayback(trackList[trackIndex]).build().execute();
                            repeatButton.setTooltip(trackIndex == 0 ? "gui.spoticraft.enable-repeat" : trackIndex == 1 ? "gui.spoticraft.enable-repeat-one" : "gui.spoticraft.disable-repeat");
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            ShowTempMessage("No device found !");
                        }
                    }
            );
        }

        // Add the button to the screen
        this.addRenderableWidget(playStopButton);

        this.addRenderableWidget(previousButton);
        this.addRenderableWidget(nextButton);
        this.addRenderableWidget(shuffleButton);
        this.addRenderableWidget(repeatButton);

        textManager.drawText(graphics);

        this.drawMusicControlBar(graphics);
    }

    private void noPremium() {
        this.drawCenteredString(graphics, "It seems that you don't have Spotify Premium on this account, you need Spotify Premium to use this mod.", this.width / 2, 20, 16777215);
        this.drawCenteredString(graphics, "Click the button bellow if you want to change account.", this.width / 2, 35, 16777215);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, button ->
                SpotifyAuthHandler.startAuthFlow()
        ).bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
    }

    // sync
    private void syncPlaybackState() {
        try {
            CurrentlyPlayingContext context = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();
            if (context != null && context.getItem() != null) {
                totalDurationMs = context.getItem().getDurationMs();
                currentProgressMs = context.getProgress_ms();
                musicPlaying = context.getIs_playing();
                shuffleState = context.getShuffle_state();
                System.out.println(context.getRepeat_state());
                for (int i = 0; i < trackList.length; i++) {
                    if (trackList[i].equalsIgnoreCase(context.getRepeat_state())) {
                        trackIndex = i;
                        break;
                    }
                }
                //trackIndex = trackList.;
                lastUpdateTime = System.currentTimeMillis(); // Sync the timer with Spotify's state
            }
        } catch (Exception e) {
            System.out.println("Failed to sync playback state : " + e.getMessage());
            ShowTempMessage("Failed to sync playback state !");
        }
    }

    // ui stuff
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

    public void drawCenteredString(GuiGraphics guiGraphics, String text, int centerX, int y, int color) {
        // Calculate the text width and draw it centered
        int textWidth = this.font.width(text);
        guiGraphics.drawString(this.font, text, centerX - textWidth / 2, y, color);
    }

    private void ShowTempMessage(String message) {
        // Set text for the message
        textManager.setText(message, this.width / 2, this.height / 2, 16777215);

        // Clear text after 5 seconds
        if (tempMessageTimer != null) {
            tempMessageTimer.cancel(); // Cancel any existing timer
        }
        tempMessageTimer = new Timer();
        tempMessageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                textManager.clearText();
            }
        }, 5000);
    }

    // mouse action
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int barX = this.width / 2 - barWidth / 2;
        int barY = this.height - 20;

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
        int barY = this.height - 20;

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
        int barY = this.height - 20;

        // Check if dragging is within the bounds of the progress bar
        if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
            // Update the music progress as the user drags
            currentProgressMs = (int) (((mouseX - barX) / barWidth) * totalDurationMs);
            currentProgressMs = Math.max(0, Math.min(currentProgressMs, totalDurationMs)); // Clamp between 0 and total duration
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    // ui controls
    private void toggleMusicPlayback() {
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
    }

    // other
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
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