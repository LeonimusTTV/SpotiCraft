package com.leonimust.spoticraft.client.ui;

import com.leonimust.spoticraft.SpotiCraft;
import com.leonimust.spoticraft.client.TokenStorage;
import com.leonimust.spoticraft.server.SpotifyAuthHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONObject;
import org.lwjgl.glfw.GLFW;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ProductType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    private ImageButton nextButton;
    private ImageButton previousButton;

    public static SpotifyApi spotifyApi;
    private Timer updateTimer;

    private int barWidth;
    private final int barHeight = 4;

    private boolean userPremium = false;

    private TextManager textManager;
    private Timer tempMessageTimer;

    ResourceLocation PLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/play.png");
    ResourceLocation PAUSE_TEXTURE = ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/pause.png");

    private final String[] trackList = {"off", "context", "track"};
    private int trackIndex = 0;

    private ResourceLocation musicImage; // Holds the texture for the current music cover
    private String artistName;
    private String musicName;

    private final HashMap<String, JSONObject> trackCache = new HashMap<>();

    private int volumeBarWidth;
    private final int volumeBarHeight = 4;
    private int currentVolume = 50;

    private ItemScrollPanel playlistPanel;
    private ItemScrollPanel mainPanel;

    private EditBox searchInput;

    private final List<Item> playlistItems = new ArrayList<>();
    private final List<Item> mainItems = new ArrayList<>();

    @Override
    public void init() {
        if (token == null) {
            return;
        }

        this.barWidth = this.width / 3 - 10;
        this.volumeBarWidth = this.width / 8;

        this.textManager = new TextManager(this.font);

        try {
            TokenStorage.checkIfExpired();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Initialize the Spotify API client
        spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(token.getString("access_token"))
                .setRefreshToken(token.getString("refresh_token"))
                .build();

        final CompletableFuture<User> userFuture = spotifyApi.getCurrentUsersProfile().build().executeAsync();

        // Sync playback state when the screen is opened
        syncData();

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
                        try {
                            syncDataWithDelay();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }, 0, 1000);

        userPremium = userFuture.join().getProduct() == ProductType.PREMIUM;
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
        this.drawCenteredString(graphics, Component.translatable("gui.spoticraft.not_logged").getString(), this.width / 2, 20, 16777215);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, button ->
                SpotifyAuthHandler.startAuthFlow()
        ).bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
    }

    private void mainScreen() {

        if (playlistPanel == null) {
            playlistPanel = new ItemScrollPanel(this.minecraft, this.width / 3,this.height - 64, 20, 5);
            // useful for first init
            playlistPanel.setInfo(playlistItems);
            // don't move this line down, if minecraft keep refreshing this panel and that the items list changes
            // it will crash the game
            this.addRenderableWidget(playlistPanel);
        }

        if (mainPanel == null) {
            mainPanel = new ItemScrollPanel(this.minecraft, this.width - this.width / 3 - 15,this.height - 65, 20, this.width/3+10);
            // useful for first init
            mainPanel.setInfo(mainItems);
            // don't move this line down, if minecraft keep refreshing this panel and that the items list changes
            // it will crash the game
            this.addRenderableWidget(mainPanel);
        }

        if (musicImage != null) {
            int imageWidth = 30;
            int imageHeight = 30;

            ImageHandler.drawImage(graphics, musicImage, this.height, imageHeight, imageWidth);

            //title
            graphics.drawString(this.font, resizeText(musicName, 18), imageWidth + 10, this.height - imageWidth + 2, 0xFFFFFF);
            //artist name
            graphics.drawString(this.font, resizeText(artistName, 14), imageWidth + 10, this.height - imageWidth + 12, 0x474747);
        }

        //Minecraft ImageButton is shit and doesn't work ;_; thanks for the 4 hours of lost time xD
        if (playStopButton == null) {
            playStopButton = new ImageButton(
                    this.width / 2 - 8,
                    this.height - 35,
                    15, // Button width
                    15, // Button height
                    musicPlaying ? PAUSE_TEXTURE : PLAY_TEXTURE,  // Use stop texture if playing, otherwise play texture
                    15, // Full texture width
                    15, // Full texture height
                    musicPlaying ? "gui.spoticraft.pause" : "gui.spoticraft.play",
                    button -> {
                        try {
                            toggleMusicPlayback();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } // Toggle playback on click
            );
        }

        // Update the texture if the music playing state has changed
        playStopButton.setTexture(musicPlaying ? PAUSE_TEXTURE : PLAY_TEXTURE);

        // Update the tooltip if the music playing state has changed
        playStopButton.setTooltip(musicPlaying ? "gui.spoticraft.pause" : "gui.spoticraft.play");

        if (nextButton == null) {
            nextButton = new ImageButton(
                    this.width / 2 + 15,
                    this.height - 35,
                    13, // Button width
                    13, // Button height
                    ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/next.png"),  // Use stop texture if playing, otherwise play texture
                    13, // Full texture width
                    13, // Full texture height
                    "gui.spoticraft.next",
                    button -> {
                        try {
                            try {
                                TokenStorage.checkIfExpired();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
                            syncDataWithDelay();
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            ShowTempMessage("gui.spoticraft.no_device");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

        if (previousButton == null) {
            previousButton = new ImageButton(
                    this.width / 2 - 30,
                    this.height - 35,
                    13, // Button width
                    13, // Button height
                    ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/previous.png"),  // Use stop texture if playing, otherwise play texture
                    13, // Full texture width
                    13, // Full texture height
                    "gui.spoticraft.previous",
                    button -> {
                        try {
                            try {
                                TokenStorage.checkIfExpired();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            spotifyApi.skipUsersPlaybackToPreviousTrack().build().execute();
                            syncDataWithDelay();
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            ShowTempMessage("gui.spoticraft.no_device");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

        previousButton.setActive(!shuffleState);

        if (shuffleButton == null) {
            shuffleButton = new ImageButton(
                    this.width / 2 - 50,
                    this.height - 35,
                    13, // Button width
                    13, // Button height
                    ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/shuffle.png"),  // Use stop texture if playing, otherwise play texture
                    13, // Full texture width
                    13, // Full texture height
                    shuffleState ? "gui.spoticraft.disable_shuffle" : "gui.spoticraft.enable_shuffle",
                    button -> {
                        try {
                            try {
                                TokenStorage.checkIfExpired();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            spotifyApi.toggleShuffleForUsersPlayback(!shuffleState).build().execute();
                            shuffleState = !shuffleState;
                            shuffleButton.setTooltip(shuffleState ? "gui.spoticraft.disable_shuffle" : "gui.spoticraft.enable_shuffle");

                            previousButton.setActive(!shuffleState);
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            ShowTempMessage("gui.spoticraft.no_device");
                        }
                    }
            );
        }

        if (repeatButton == null) {
            repeatButton = new ImageButton(
                    this.width / 2 + 35,
                    this.height - 35,
                    13, // Button width
                    13, // Button height
                    ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/repeat.png"),  // Use stop texture if playing, otherwise play texture
                    13, // Full texture width
                    13, // Full texture height
                    trackIndex == 0 ? "gui.spoticraft.enable_repeat" : trackIndex == 1 ? "gui.spoticraft.enable_repeat_one" : "gui.spoticraft.disable_repeat",
                    button -> {
                        try {
                            try {
                                TokenStorage.checkIfExpired();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            trackIndex = (trackIndex + 1) % trackList.length;
                            spotifyApi.setRepeatModeOnUsersPlayback(trackList[trackIndex]).build().execute();
                            repeatButton.setTooltip(trackIndex == 0 ? "gui.spoticraft.enable_repeat" : trackIndex == 1 ? "gui.spoticraft.enable_repeat_one" : "gui.spoticraft.disable_repeat");
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            ShowTempMessage("gui.spoticraft.no_device");
                        }
                    }
            );
        }

        if (searchInput == null) {
            // components seems to be useless here
            searchInput = new EditBox(this.font, this.width/2, 3, this.width/4,15, CommonComponents.EMPTY);
            this.addRenderableWidget(searchInput);
        }

        // Add everything to the screen
        this.addRenderableWidget(playStopButton);
        this.addRenderableWidget(previousButton);
        this.addRenderableWidget(nextButton);
        this.addRenderableWidget(shuffleButton);
        this.addRenderableWidget(repeatButton);

        textManager.drawText(graphics);

        this.drawMusicControlBar(graphics);
        this.drawVolumeBar(graphics);
    }

    private void noPremium() {
        this.drawCenteredString(graphics, Component.translatable("gui.spoticraft.no_premium").getString(), this.width / 2, 20, 16777215);
        this.drawCenteredString(graphics, Component.translatable("gui.spoticraft.no_premium_2").getString(), this.width / 2, 35, 16777215);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, button ->
                SpotifyAuthHandler.startAuthFlow()
        ).bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
    }

    private void syncDataWithDelay() throws InterruptedException {
        Thread.sleep(250);
        syncData();
    }

    // sync
    private void syncData() {
        System.out.println("Sync playback state");

        try {
            CurrentlyPlayingContext context = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();

            if (context != null && context.getItem() != null) {
                totalDurationMs = context.getItem().getDurationMs();
                currentProgressMs = context.getProgress_ms();
                musicPlaying = context.getIs_playing();
                shuffleState = context.getShuffle_state();
                currentVolume = context.getDevice().getVolume_percent();
                musicName = context.getItem().getName();
                // artist is down

                for (int i = 0; i < trackList.length; i++) {
                    if (trackList[i].equalsIgnoreCase(context.getRepeat_state())) {
                        trackIndex = i;
                        break;
                    }
                }
                //trackIndex = trackList.;
                lastUpdateTime = System.currentTimeMillis() - 500; // Sync the timer with Spotify's state and add a lil more because of the request time

                // cache track image url so doesn't need to ask spotify api and avoid 304 Not Modified responses
                if (trackCache.get(context.getItem().getId()) != null) {
                    JSONObject track = trackCache.get(context.getItem().getId());
                    //url
                    loadMusicImage(track.getString("url"));
                    artistName = track.getString("artists");
                } else {
                    String trackId = context.getItem().getId();
                    AlbumSimplified track = spotifyApi.getTrack(trackId).build().execute().getAlbum();

                    // since some song can have multiple artist we do this to add then
                    StringBuilder artists = new StringBuilder();
                    for (ArtistSimplified artist : track.getArtists()) {
                        artists.append(artist.getName()).append(", ");
                    }
                    // cut the ", " on the last artist
                    artistName = artists.substring(0, artists.length() - 2);

                    String url = track.getImages()[0].getUrl();
                    System.out.println("Track URL: " + url);

                    loadMusicImage(url);

                    // save url and artist into trackCache
                    JSONObject trackJSON = new JSONObject();

                    trackJSON.put("url", url);
                    trackJSON.put("artists", artistName);

                    trackCache.put(trackId, trackJSON);
                }

                if (repeatButton != null) {
                    repeatButton.setTooltip(trackIndex == 0 ? "gui.spoticraft.enable_repeat" : trackIndex == 1 ? "gui.spoticraft.enable_repeat_one" : "gui.spoticraft.disable_repeat");
                }

                //playlist
                final Paging<PlaylistSimplified> playlistSimplifiedPaging = spotifyApi.getListOfCurrentUsersPlaylists().build().execute();

                playlistItems.clear();

                if (playlistPanel != null) {
                    playlistPanel.clear();
                }

                for (PlaylistSimplified playlist : playlistSimplifiedPaging.getItems()) {
                    ResourceLocation playlistImage;
                    if (playlist.getImages() == null) {
                        playlistImage = ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/default_playlist_image.png");
                    } else {
                        playlistImage = ImageHandler.downloadImage(playlist.getImages()[0].getUrl());
                    }

                    playlistItems.add(new Item(
                            playlistImage,
                            resizeText(playlist.getName(), 17),
                            playlist.getUri(),
                            Item.itemType.TRACK,
                            this.font));
                }

                //TODO find a better fix for the bug where the last item isn't rendered correctly in the scroll panel
                playlistItems.add(new Item(
                        ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/empty.png"),
                        "",
                        "",
                        Item.itemType.EMPTY,
                        this.font
                ));

                if (playlistPanel != null) {
                    playlistPanel.setInfo(playlistItems);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to sync playback state : " + e.getMessage());
            // most of the time when the sync failed it's because of an expired token
            try {
                TokenStorage.checkIfExpired();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            ShowTempMessage("gui.spoticraft.sync_error");
        }
    }

    // ui stuff
    private void drawMusicControlBar(GuiGraphics graphics) {
        int barX = this.width / 2 - barWidth / 2;
        int barY = this.height - 15;

        // Draw the background of the bar
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFFCCCCCC);

        // Draw the filled portion of the bar
        int filledWidth = (int) ((currentProgressMs / (float) totalDurationMs) * barWidth);
        graphics.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFFFFFFFF);

        // Draw the time
        String currentTime = formatTime(currentProgressMs / 1000);
        String durationTime = formatTime(totalDurationMs / 1000);
        drawCenteredString(graphics, currentTime, this.width / 2 - ((barWidth + 30) / 2), barY - 2, 0xFFFFFF);
        drawCenteredString(graphics, durationTime, this.width / 2 + ((barWidth + 30) / 2), barY - 2, 0xFFFFFF);
    }

    private void drawVolumeBar(GuiGraphics graphics) {
        int barX = this.width - volumeBarWidth - 35;
        int barY = this.height - 15;

        // Draw the background of the volume bar
        graphics.fill(barX, barY, barX + volumeBarWidth, barY + volumeBarHeight, 0xFFCCCCCC);

        // Draw the filled portion of the volume bar
        int filledWidth = (int) ((currentVolume / 100.0) * volumeBarWidth);
        graphics.fill(barX, barY, barX + filledWidth, barY + volumeBarHeight, 0xFFFFFFFF);

        // Draw the volume percentage
        String volumeText = currentVolume + "%";
        drawCenteredString(graphics, volumeText, barX + volumeBarWidth + 15, barY + (volumeBarHeight / 2) - 4, 0xFFFFFF);
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER && searchInput.isFocused()) {
            search(searchInput.getValue());
            return true; // Consume the event
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void search(String query) {
        System.out.println("Searching for " + query);
        CompletableFuture<Paging<Track>> pagingFuture = spotifyApi.searchTracks(query).build().executeAsync();

        final Paging<Track> tracks = pagingFuture.join();

        System.out.println(Arrays.toString(tracks.getItems()));

        for (Track track : tracks.getItems()) {
            ResourceLocation trackImage;
            if (track.getAlbum().getImages() == null) {
                trackImage = ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/default_playlist_image.png");
            } else {
                trackImage = ImageHandler.downloadImage(track.getAlbum().getImages()[0].getUrl());
            }

            mainItems.add(new Item(
                    trackImage,
                    resizeText(track.getName(), 40),
                    track.getUri(),
                    Item.itemType.TRACK,
                    this.font));
        }

        mainItems.add(new Item(
                ResourceLocation.fromNamespaceAndPath(SpotiCraft.MOD_ID, "textures/gui/empty.png"),
                "",
                "",
                Item.itemType.EMPTY,
                this.font
        ));

        mainPanel.setInfo(mainItems);
    }

    // mouse action
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int barX = this.width / 2 - barWidth / 2;
        int barY = this.height - 15;
        int volumeBarX = this.width - volumeBarWidth - 35;
        int volumeBarY = this.height - 15;

        if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
            currentProgressMs = (int) (((mouseX - barX) / barWidth) * totalDurationMs);
            return changePositionInCurrentTrack();
        }

        if (mouseX >= volumeBarX && mouseX <= volumeBarX + volumeBarWidth && mouseY >= volumeBarY && mouseY <= volumeBarY + volumeBarHeight) {
            updateVolume((int) ((mouseX - volumeBarX) / volumeBarWidth * 100));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int barX = this.width / 2 - barWidth / 2;
        int barY = this.height - 15;
        int volumeBarX = this.width - volumeBarWidth - 35;
        int volumeBarY = this.height - 15;

        // Check if dragging is within the bounds of the progress bar
        if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
            return changePositionInCurrentTrack();
        }

        if (mouseX >= volumeBarX && mouseX <= volumeBarX + volumeBarWidth && mouseY >= volumeBarY && mouseY <= volumeBarY + volumeBarHeight) {
            updateVolume((int) ((mouseX - volumeBarX) / volumeBarWidth * 100));
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int barX = this.width / 2 - barWidth / 2;
        int barY = this.height - 15;
        int volumeBarX = this.width - volumeBarWidth - 35;
        int volumeBarY = this.height - 15;

        // Check if dragging is within the bounds of the progress bar
        if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
            // Update the music progress as the user drags
            currentProgressMs = (int) (((mouseX - barX) / barWidth) * totalDurationMs);
            currentProgressMs = Math.max(0, Math.min(currentProgressMs, totalDurationMs)); // Clamp between 0 and total duration
            return true;
        }

        if (mouseX >= volumeBarX && mouseX <= volumeBarX + volumeBarWidth && mouseY >= volumeBarY && mouseY <= volumeBarY + volumeBarHeight) {
            //updateVolume((int) ((mouseX - volumeBarX) / volumeBarWidth * 100));
            currentVolume = (int) ((mouseX - volumeBarX) / volumeBarWidth * 100);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    // ui controls
    private void toggleMusicPlayback() throws InterruptedException {
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
            syncData();
            musicPlaying = true;
        }
    }

    private void updateVolume(int newVolume) {
        currentVolume = Math.max(0, Math.min(newVolume, 100)); // Clamp between 0 and 100

        // Send the volume update to Spotify API
        try {
            spotifyApi.setVolumeForUsersPlayback(currentVolume).build().executeAsync();
        } catch (Exception e) {
            ShowTempMessage("Failed to set volume: " + e.getMessage());
        }
    }

    // other
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    public void loadMusicImage(String url) {
        musicImage = ImageHandler.downloadImage(url); // Download and set the image
    }

    private boolean changePositionInCurrentTrack() {
        try {
            try {
                TokenStorage.checkIfExpired();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            spotifyApi.seekToPositionInCurrentlyPlayingTrack(currentProgressMs).build().executeAsync();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    private String resizeText(String text, int maxSize) {
        StringBuilder resizedText = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (i + 1 == maxSize) {
                resizedText.append("...");
                break;
            }

            assert false;
            resizedText.append(text.charAt(i));
        }

        return resizedText.toString();
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