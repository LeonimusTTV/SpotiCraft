package com.leonimust.spoticraft.neoforge.client.ui;

import com.leonimust.spoticraft.Main;
import com.leonimust.spoticraft.neoforge.client.TokenStorage;
import com.leonimust.spoticraft.common.client.ui.ImageButton;
import com.leonimust.spoticraft.common.client.ui.ImageHandler;
import com.leonimust.spoticraft.common.client.ui.TextManager;
import com.leonimust.spoticraft.neoforge.server.SpotifyAuthHandler;
import com.neovisionaries.i18n.CountryCode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
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
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.leonimust.spoticraft.neoforge.client.TokenStorage.token;

public class SpotifyScreen extends Screen {
    private static SpotifyScreen instance;

    private GuiGraphics graphics;
    private int totalDurationMs;
    private int currentProgressMs;
    private boolean musicPlaying = false;
    private long lastUpdateTime;
    private boolean shuffleState = false;
    private boolean likedSong = false;

    private ImageButton playStopButton;
    private ImageButton shuffleButton;
    private ImageButton repeatButton;
    private ImageButton nextButton;
    private ImageButton previousButton;
    private ImageButton goBackButton;
    private ImageButton goForwardButton;
    private ImageButton homeButton;
    private ImageButton likeButton;

    public static SpotifyApi spotifyApi;
    private Timer updateTimer;

    private int barWidth;
    private final int barHeight = 4;

    private boolean userPremium = false;
    private CountryCode userCountryCode;

    private boolean tokenExpired = false;

    private TextManager textManager;
    private Timer tempMessageTimer;

    ResourceLocation PLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/play.png");
    ResourceLocation PAUSE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/pause.png");
    ResourceLocation EMPTY_IMAGE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/empty.png");
    ResourceLocation LIKE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/like_icon.png");
    ResourceLocation LIKED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/liked_icon.png");
    ResourceLocation SHUFFLE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/shuffle.png");
    ResourceLocation SHUFFLE_ENABLE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/shuffle_enable.png");
    ResourceLocation REPEAT = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/repeat.png");
    ResourceLocation REPEAT_ENABLE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/repeat_enable.png");
    ResourceLocation REPEAT_ONE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/repeat_1.png");

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
    private List<Item> mainItems = new ArrayList<>();

    // save all actions so user can go back
    private final List<List<Item>> itemCache = new ArrayList<>();
    private final List<List<Item>> itemCacheForward = new ArrayList<>();

    private String currentTrackId;

    int imageWidth = 30;
    int imageHeight = 30;

    @Override
    public void init() {
        if (token == null) {
            return;
        }

        this.barWidth = this.width / 3 - 10;
        this.volumeBarWidth = this.width / 8;

        this.textManager = new TextManager(this.font);

        if (checkIfExpired()) {return;}

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

        userCountryCode = userFuture.join().getCountry();
    }

    public SpotifyScreen() {
        super(Component.translatable("gui.spoticraft.spotify_player"));
        instance = this;
    }

    public static SpotifyScreen getInstance() {
        return instance;
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
            if (!userPremium && !tokenExpired) {
                noPremium();
            } else if (!tokenExpired) {
                try {
                    mainScreen();
                } catch (IOException | ParseException | SpotifyWebApiException e) {
                    throw new RuntimeException(e);
                }
            } else {
                tokenExpiredScreen();
            }
        }
    }

    // screens
    private void loginScreen() {
        this.drawCenteredString(graphics, Component.translatable("gui.spoticraft.not_logged").getString(), this.width / 2, 20, 16777215);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, button ->
                {
                    try {
                        SpotifyAuthHandler.startAuthFlow();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ).bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
    }

    private void mainScreen() throws IOException, ParseException, SpotifyWebApiException {
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

            ImageHandler.drawImage(graphics, musicImage, this.height, imageHeight, imageWidth);

            //title
            graphics.drawString(this.font, musicName, imageWidth + 10, this.height - imageWidth + 2, 0xFFFFFF);
            //artist name
            graphics.drawString(this.font, artistName, imageWidth + 10, this.height - imageWidth + 12, 0x474747);
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
            this.addRenderableWidget(playStopButton);
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
                    ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/next.png"),  // Use stop texture if playing, otherwise play texture
                    13, // Full texture width
                    13, // Full texture height
                    "gui.spoticraft.next",
                    button -> {
                        try {
                            if (checkIfExpired()) {return;}

                            spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
                            syncDataWithDelay();
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            ShowTempMessage("gui.spoticraft.no_device");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            this.addRenderableWidget(nextButton);
        }

        if (previousButton == null) {
            previousButton = new ImageButton(
                    this.width / 2 - 30,
                    this.height - 35,
                    13, // Button width
                    13, // Button height
                    ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/previous.png"),  // Use stop texture if playing, otherwise play texture
                    13, // Full texture width
                    13, // Full texture height
                    "gui.spoticraft.previous",
                    button -> {
                        try {
                            if (checkIfExpired()) {return;}

                            spotifyApi.skipUsersPlaybackToPreviousTrack().build().execute();
                            syncDataWithDelay();
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            ShowTempMessage("gui.spoticraft.no_device");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            previousButton.setActive(!shuffleState);

            this.addRenderableWidget(previousButton);
        }

        if (shuffleButton == null) {
            shuffleButton = new ImageButton(
                    this.width / 2 - 50,
                    this.height - 35,
                    13, // Button width
                    13, // Button height
                    shuffleState ? SHUFFLE_ENABLE : SHUFFLE,  // Use stop texture if playing, otherwise play texture
                    13, // Full texture width
                    13, // Full texture height
                    shuffleState ? "gui.spoticraft.disable_shuffle" : "gui.spoticraft.enable_shuffle",
                    button -> {
                        try {
                            if (checkIfExpired()) {return;}

                            spotifyApi.toggleShuffleForUsersPlayback(!shuffleState).build().execute();
                            shuffleState = !shuffleState;
                            shuffleButton.setTooltip(shuffleState ? "gui.spoticraft.disable_shuffle" : "gui.spoticraft.enable_shuffle");
                            shuffleButton.setTexture(shuffleState ? SHUFFLE_ENABLE : SHUFFLE);

                            previousButton.setActive(!shuffleState);
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            ShowTempMessage("gui.spoticraft.no_device");
                        }
                    }
            );
            this.addRenderableWidget(shuffleButton);
        }

        if (repeatButton == null) {
            repeatButton = new ImageButton(
                    this.width / 2 + 35,
                    this.height - 35,
                    13, // Button width
                    13, // Button height
                    trackIndex == 0 ? REPEAT : trackIndex == 1 ? REPEAT_ENABLE : REPEAT_ONE,  // Use stop texture if playing, otherwise play texture
                    13, // Full texture width
                    13, // Full texture height
                    trackIndex == 0 ? "gui.spoticraft.enable_repeat" : trackIndex == 1 ? "gui.spoticraft.enable_repeat_one" : "gui.spoticraft.disable_repeat",
                    button -> {
                        try {
                            if (checkIfExpired()) {return;}

                            trackIndex = (trackIndex + 1) % trackList.length;
                            spotifyApi.setRepeatModeOnUsersPlayback(trackList[trackIndex]).build().execute();
                            repeatButton.setTooltip(trackIndex == 0 ? "gui.spoticraft.enable_repeat" : trackIndex == 1 ? "gui.spoticraft.enable_repeat_one" : "gui.spoticraft.disable_repeat");
                            repeatButton.setTexture(trackIndex == 0 ? REPEAT : trackIndex == 1 ? REPEAT_ENABLE : REPEAT_ONE);
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            ShowTempMessage("gui.spoticraft.no_device");
                        }
                    }
            );
            this.addRenderableWidget(repeatButton);
        }

        if (searchInput == null) {
            // components seems to be useless here
            searchInput = new EditBox(this.font, this.width/2 - this.width/8, 3, this.width/4,15, CommonComponents.EMPTY);
            this.addRenderableWidget(searchInput);
        }

        if (goBackButton == null) {
            goBackButton = new ImageButton(
                    this.width/2 - this.width/6 + 6,
                    4,
                    13, // Button width
                    13, // Button height
                    ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/go_back.png"),  // Use stop texture if playing, otherwise play texture
                    13, // Full texture width
                    13, // Full texture height
                    "gui.spoticraft.go_back",
                    button -> goBack()
            );
            this.addRenderableWidget(goBackButton);
        }

        if (goForwardButton == null) {
            goForwardButton = new ImageButton(
                    this.width/2 + this.width/6 - 19,
                    4,
                    13, // Button width
                    13, // Button height
                    ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/go_forward.png"),  // Use stop texture if playing, otherwise play texture
                    13, // Full texture width
                    13, // Full texture height
                    "gui.spoticraft.go_forward",
                    button -> goForward()
            );
            this.addRenderableWidget(goForwardButton);
        }

        if (homeButton == null) {
            homeButton = new ImageButton(
                    5,
                    3,
                    15, // Button width
                    15, // Button height
                    ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/home.png"),  // Use stop texture if playing, otherwise play texture
                    15, // Full texture width
                    15, // Full texture height
                    "gui.spoticraft.home",
                    button -> {
                        try {
                            showHomePage();
                        } catch (IOException | ParseException | SpotifyWebApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            this.addRenderableWidget(homeButton);
        }

        if (mainItems.isEmpty()) {
            this.showHomePage();
            //once it's finished save the main page
            saveLastAction();
        }

        if (playlistItems.isEmpty()) {
            showUserPlaylists();
        }

        if (likeButton == null && musicName != null) {
            likeButton = new ImageButton(
                    imageWidth + 10 + font.width(musicName) + 2,
                    this.height - imageHeight - 1,
                    10, // Button width
                    10, // Button height
                    likedSong ? LIKED_TEXTURE : LIKE_TEXTURE,
                    10, // Full texture width
                    10, // Full texture height
                    likedSong ? "gui.spoticraft.liked" : "gui.spoticraft.like",
                    button -> {
                        try {
                            addOrRemoveLikedSong();
                        } catch (IOException | ParseException | SpotifyWebApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            this.addRenderableWidget(likeButton);
        }

        textManager.drawText(graphics);

        this.drawMusicControlBar(graphics);
        this.drawVolumeBar(graphics);
    }

    private void noPremium() {
        this.drawCenteredString(graphics, Component.translatable("gui.spoticraft.no_premium").getString(), this.width / 2, 20, 16777215);
        this.drawCenteredString(graphics, Component.translatable("gui.spoticraft.no_premium_2").getString(), this.width / 2, 35, 16777215);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, button ->
                {
                    try {
                        SpotifyAuthHandler.startAuthFlow();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ).bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
    }

    private void tokenExpiredScreen() {
        this.clearWidgets();
        this.drawCenteredString(graphics, Component.translatable("gui.spoticraft.token_expired").getString(), this.width / 2, 20, 16777215);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, button ->
                {
                    try {
                        SpotifyAuthHandler.startAuthFlow();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ).bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
    }

    public void syncDataWithDelay() throws InterruptedException {
        Thread.sleep(500);
        syncData();
    }

    // sync
    public void syncData() {
        Main.LOGGER.info("Syncing data");

        try {
            if (checkIfExpired()) {return;}

            CurrentlyPlayingContext context = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();

            if (context != null && context.getItem() != null) {
                currentTrackId = context.getItem().getId();
                totalDurationMs = context.getItem().getDurationMs();
                currentProgressMs = context.getProgress_ms();
                musicPlaying = context.getIs_playing();
                shuffleState = context.getShuffle_state();
                currentVolume = context.getDevice().getVolume_percent();
                musicName = resizeText(context.getItem().getName(), 100);
                // artist is down
                for (int i = 0; i < trackList.length; i++) {
                    if (trackList[i].equalsIgnoreCase(context.getRepeat_state())) {
                        trackIndex = i;
                        break;
                    }
                }
                //trackIndex = trackList.;
                lastUpdateTime = System.currentTimeMillis() - 700; // Sync the timer with Spotify's state and add a lil more because of the request time

                // cache track image url so doesn't need to ask spotify api and avoid 304 Not Modified responses
                if (trackCache.get(context.getItem().getId()) != null) {
                    JSONObject track = trackCache.get(context.getItem().getId());
                    //url
                    loadMusicImage(track.getString("url"));
                    artistName = track.getString("artists");
                } else {
                    String trackId = context.getItem().getId();
                    AlbumSimplified track = spotifyApi.getTrack(trackId).build().execute().getAlbum();

                    artistName = resizeText(formatArtists(track.getArtists()), 80);

                    String url = track.getImages()[0].getUrl();
                    System.out.println("Track URL: " + url);

                    loadMusicImage(url);

                    // save url and artist into trackCache
                    JSONObject trackJSON = new JSONObject();

                    trackJSON.put("url", url);
                    trackJSON.put("artists", artistName);
                    trackJSON.put("uri", track.getUri());

                    trackCache.put(trackId, trackJSON);
                }

                if (repeatButton != null) {
                    repeatButton.setTooltip(trackIndex == 0 ? "gui.spoticraft.enable_repeat" : trackIndex == 1 ? "gui.spoticraft.enable_repeat_one" : "gui.spoticraft.disable_repeat");
                    repeatButton.setTexture(trackIndex == 0 ? REPEAT : trackIndex == 1 ? REPEAT_ENABLE : REPEAT_ONE);
                }

                likedSong = isSongLiked(new String[]{currentTrackId});

                if (previousButton != null) {
                    previousButton.setActive(!shuffleState);
                }

                updateLikeButton();
            } else {
                ShowTempMessage("gui.spoticraft.no_device");
            }
        } catch (Exception e) {
            System.out.println("Failed to sync data : " + e.getMessage());
            // most of the time when the sync failed it's because of an expired token
            if (checkIfExpired()) {return;}
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

    public void ShowTempMessage(String message) {
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
        if (checkIfExpired()) {return;}
        System.out.println("Searching for " + query);
        CompletableFuture<Paging<Track>> pagingFutureTrack = spotifyApi.searchTracks(query).build().executeAsync();
        CompletableFuture<Paging<AlbumSimplified>> pagingFutureAlbum = spotifyApi.searchAlbums(query).build().executeAsync();
        CompletableFuture<Paging<PlaylistSimplified>> pagingFuturePlaylist = spotifyApi.searchPlaylists(query).build().executeAsync();
        CompletableFuture<Paging<Artist>> pagingFutureArtists = spotifyApi.searchArtists(query).build().executeAsync();

        final Paging<Track> tracks = pagingFutureTrack.join();
        final Paging<AlbumSimplified> albums = pagingFutureAlbum.join();
        final Paging<PlaylistSimplified> playlists = pagingFuturePlaylist.join();
        final Paging<Artist> artists = pagingFutureArtists.join();

        saveLastAction();

        mainItems.clear();

        for (int i = 0; i < Math.min(2, artists.getItems().length); i++) {
            Artist artist = artists.getItems()[i];
            if (artist == null) {
                continue;
            }
            ResourceLocation artistImage = getImage(artist.getImages() == null || artist.getImages().length == 0 ? null : artist.getImages()[0].getUrl());

            mainItems.add(new Item(
                    artistImage,
                    resizeText(artist.getName(), 200),
                    "",
                    artist.getId(),
                    Item.itemType.ARTIST,
                    "",
                    this.font));
        }

        for (int i = 0; i < Math.min(5, tracks.getItems().length); i++) {
            Track track = tracks.getItems()[i];
            if (track == null) {
                continue;
            }
            ResourceLocation trackImage = getImage(track.getAlbum().getImages() == null || track.getAlbum().getImages().length == 0 ? null : track.getAlbum().getImages()[0].getUrl());

            mainItems.add(new Item(
                    trackImage,
                    resizeText(track.getName(), 200),
                    track.getUri(),
                    track.getId(),
                    Item.itemType.TRACK,
                    "",
                    this.font));
        }

        for (int i = 0; i < Math.min(5, albums.getItems().length); i++) {
            AlbumSimplified album = albums.getItems()[i];
            if (album == null) {
                continue;
            }
            ResourceLocation albumImage = getImage(album.getImages() == null || album.getImages().length == 0 ? null : album.getImages()[0].getUrl());

            mainItems.add(new Item(
                    albumImage,
                    resizeText(album.getName(), 200),
                    album.getUri(),
                    album.getId(),
                    Item.itemType.ALBUM,
                    "",
                    this.font));
        }

        for (int i = 0; i < Math.min(5, playlists.getItems().length); i++) {
            PlaylistSimplified playlist = playlists.getItems()[i];
            if (playlist == null) {
                continue;
            }
            ResourceLocation playlistImage = getImage(playlist.getImages() == null || playlist.getImages().length == 0 ? null : playlist.getImages()[0].getUrl());

            mainItems.add(new Item(
                    playlistImage,
                    resizeText(playlist.getName(), 200),
                    playlist.getUri(),
                    playlist.getId(),
                    Item.itemType.PLAYLIST,
                    "",
                    this.font));
        }

        addEmpty(mainItems);

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
        if (checkIfExpired()) {return;}

        try {
            if (musicPlaying) {
                spotifyApi.pauseUsersPlayback().build().execute();
                musicPlaying = false;
            } else {
                spotifyApi.startResumeUsersPlayback().build().execute();
                syncData();
                musicPlaying = true;
            }
        } catch (Exception e) {
            ShowTempMessage(e.getMessage());
        }
    }

    private void updateVolume(int newVolume) {
        currentVolume = Math.max(0, Math.min(newVolume, 100)); // Clamp between 0 and 100

        // Send the volume update to Spotify API
        try {
            if (checkIfExpired()) {return;}
            spotifyApi.setVolumeForUsersPlayback(currentVolume).build().executeAsync();
        } catch (Exception e) {
            ShowTempMessage("Failed to set volume: " + e.getMessage());
        }
    }

    public void showPlaylist(String playlistId, String playlistContext) throws IOException, ParseException, SpotifyWebApiException {
        if (checkIfExpired()) {return;}

        PlaylistTrack[] tracks = spotifyApi.getPlaylistsItems(playlistId).build().execute().getItems();

        saveLastAction();

        mainItems.clear();

        mainItems.add(new Item(
                ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/play.png"),
                Component.translatable("gui.spoticraft.play_playlist").getString(),
                "",
                "",
                Item.itemType.PLAY_ALBUM_PLAYLIST,
                playlistContext,
                this.font
        ));

        for (PlaylistTrack track : tracks) {
            showTrack(track.getTrack().getId(), track.getTrack().getUri(), track.getTrack().getName(), playlistContext);
        }

        addEmpty(mainItems);

        mainPanel.setInfo(mainItems);
    }

    public void showAlbum(String albumId, String albumContext) throws IOException, ParseException, SpotifyWebApiException {
        if (checkIfExpired()) {return;}

        Paging<TrackSimplified> tracks = spotifyApi.getAlbumsTracks(albumId).build().execute();

        System.out.println(Arrays.toString(tracks.getItems()));

        saveLastAction();

        mainItems.clear();

        mainItems.add(new Item(
                ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/play.png"),
                Component.translatable("gui.spoticraft.play_album").getString(),
                "",
                "",
                Item.itemType.PLAY_ALBUM_PLAYLIST,
                albumContext,
                this.font
        ));

        for (TrackSimplified track : tracks.getItems()) {
            showTrack(track.getId(), track.getUri(), track.getName(), albumContext);
        }

        addEmpty(mainItems);

        mainPanel.setInfo(mainItems);
    }

    private void showTrack(String trackId, String trackUri, String trackName, String context) throws IOException, ParseException, SpotifyWebApiException {
        if (checkIfExpired()) {return;}

        String url;
        if (trackCache.get(trackId) != null) {
            JSONObject trackJson = trackCache.get(trackId);
            //url
            url = trackJson.getString("url");
        } else {
            AlbumSimplified trackAlbum = spotifyApi.getTrack(trackId).build().execute().getAlbum();

            url = trackAlbum.getImages() == null ? null : trackAlbum.getImages()[0].getUrl();

            // save url and artist into trackCache
            JSONObject trackJSON = new JSONObject();

            trackJSON.put("url", url);
            trackJSON.put("artists", resizeText(formatArtists(trackAlbum.getArtists()), 80));
            trackJSON.put("uri", trackUri);

            trackCache.put(trackId, trackJSON);
        }

        ResourceLocation trackImage = getImage(url);

        mainItems.add(new Item(
                trackImage,
                resizeText(trackName, 200),
                trackUri,
                "",
                Item.itemType.TRACK,
                context,
                this.font));
    }

    public void showLikedTracks() throws IOException, ParseException, SpotifyWebApiException {
        if (checkIfExpired()) {return;}

        Paging<SavedTrack> tracks = spotifyApi.getUsersSavedTracks().build().execute();

        saveLastAction();

        mainItems.clear();

        for (SavedTrack savedTrack : tracks.getItems()) {
            Track track = savedTrack.getTrack();
            showTrack(track.getId(), track.getUri(), track.getName(), "");
        }

        addEmpty(mainItems);

        mainPanel.setInfo(mainItems);
    }

    public void showArtist(String artistId) throws IOException, ParseException, SpotifyWebApiException {
        if (checkIfExpired()) {return;}

        Track[] tracks = spotifyApi.getArtistsTopTracks(artistId, userCountryCode).build().execute();
        Paging<AlbumSimplified> albums = spotifyApi.getArtistsAlbums(artistId).build().execute();

        saveLastAction();

        mainItems.clear();

        for (Track track : tracks) {
            showTrack(track.getId(), track.getUri(), track.getName(), "");
        }

        for (int i = 0; i < Math.min(5, albums.getItems().length); i++) {
            AlbumSimplified album = albums.getItems()[i];
            ResourceLocation albumImage = getImage(album.getImages() == null || album.getImages().length == 0 ? null : album.getImages()[0].getUrl());

            mainItems.add(new Item(
                    albumImage,
                    resizeText(album.getName(), 200),
                    album.getUri(),
                    album.getId(),
                    Item.itemType.ALBUM,
                    "",
                    this.font
            ));
        }

        addEmpty(mainItems);

        mainPanel.setInfo(mainItems);
    }

    private void showHomePage() throws IOException, ParseException, SpotifyWebApiException {
        Paging<AlbumSimplified> newRelease = spotifyApi.getListOfNewReleases().build().execute();

        // if main items is empty it means that the ui just init
        if (!mainItems.isEmpty()) {
            saveLastAction();
        }

        mainItems.clear();

        mainItems.add(new Item(
                EMPTY_IMAGE,
                Component.translatable("gui.spoticraft.new_releases").getString(),
                "",
                "",
                Item.itemType.CATEGORY,
                "",
                this.font
        ));

        for (int i = 0; i < Math.min(5, newRelease.getItems().length); i++) {
            AlbumSimplified album = newRelease.getItems()[i];
            ResourceLocation albumImage = getImage(album.getImages() == null || album.getImages().length == 0 ? null : album.getImages()[0].getUrl());

            mainItems.add(new Item(
                    albumImage,
                    album.getName(),
                    album.getUri(),
                    album.getId(),
                    Item.itemType.ALBUM,
                    "",
                    this.font
            ));
        }

        addEmpty(mainItems);

        mainPanel.setInfo(mainItems);
    }

    private void showUserPlaylists() throws IOException, ParseException, SpotifyWebApiException {
        Paging<PlaylistSimplified> playlistSimplifiedPaging = spotifyApi.getListOfCurrentUsersPlaylists().build().execute();
        Paging<SavedAlbum> savedAlbumPaging = spotifyApi.getCurrentUsersSavedAlbums().build().execute();

        playlistItems.clear();

        playlistItems.add(new Item(
                ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/liked_songs.png"),
                Component.translatable("gui.spoticraft.liked_songs").getString(),
                "",
                "",
                Item.itemType.LIKED_TRACK,
                "",
                this.font));

        for (SavedAlbum savedAlbum : savedAlbumPaging.getItems()) {
            Album album = savedAlbum.getAlbum();
            ResourceLocation albumImage = getImage(album.getImages() == null || album.getImages().length == 0 ? null : album.getImages()[0].getUrl());

            playlistItems.add(new Item(
                    albumImage,
                    resizeText(album.getName(), 100),
                    album.getUri(),
                    album.getId(),
                    Item.itemType.ALBUM,
                    "",
                    this.font));
        }

        for (PlaylistSimplified playlist : playlistSimplifiedPaging.getItems()) {
            ResourceLocation playlistImage = getImage(playlist.getImages() == null || playlist.getImages().length == 0 ? null : playlist.getImages()[0].getUrl());

            playlistItems.add(new Item(
                    playlistImage,
                    resizeText(playlist.getName(), 100),
                    playlist.getUri(),
                    playlist.getId(),
                    Item.itemType.PLAYLIST,
                    "",
                    this.font));
        }

        //System.out.println(Arrays.toString(spotifyApi.getCurrentUsersSavedAlbums().build().execute().getItems()));

        //TODO find a better fix for the bug where the last item isn't rendered correctly in the scroll panel
        addEmpty(playlistItems);

        if (playlistPanel != null) {
            playlistPanel.setInfo(playlistItems);
        }
    }

    public void goBack() {
        if (itemCache.isEmpty() || itemCache.size() == 1)
            return;

        itemCacheForward.addLast(new ArrayList<>(mainItems));
        mainItems.clear();
        mainItems = new ArrayList<>(itemCache.getLast());
        mainPanel.setInfo(mainItems);
        itemCache.removeLast();
    }

    public void goForward() {
        if (itemCacheForward.isEmpty())
            return;

        itemCache.addLast(new ArrayList<>(mainItems));
        mainItems.clear();
        mainItems = new ArrayList<>(itemCacheForward.getLast());
        mainPanel.setInfo(mainItems);
        itemCacheForward.removeLast();
    }

    private void saveLastAction() {
        itemCache.addLast(new ArrayList<>(mainItems));
        System.out.println("save : "+itemCache);
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
            if (checkIfExpired()) {return false;}

            spotifyApi.seekToPositionInCurrentlyPlayingTrack(currentProgressMs).build().executeAsync();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    private String resizeText(String text, int maxSize) {

        int size = this.font.width(text);
        System.out.println("text: "+ text + " maxSize: " + maxSize + " size: " + size);

        if (size <= maxSize) {
            return text;
        }

        for (int i = text.length() - 1; i > 0; i--) {
            String res = text.substring(0, i);
            if (this.font.width(res) <= maxSize) {
                if (i - 3 < 0) {
                    return text;
                }

                return text.substring(0, i - 3) + "...";
            }
        }

        return text;
    }

    private void addEmpty(List<Item> itemsList) {
        itemsList.add(new Item(
                EMPTY_IMAGE,
                "",
                "",
                "",
                Item.itemType.EMPTY,
                "",
                this.font
        ));
    }

    private String formatArtists(ArtistSimplified[] artists) {
        // since some song can have multiple artist we do this to add then
        StringBuilder artistsFormated = new StringBuilder();
        for (ArtistSimplified artist : artists) {
            artistsFormated.append(artist.getName()).append(", ");
        }
        // cut the ", " on the last artist
        return artistsFormated.substring(0, artistsFormated.length() - 2);
    }

    private ResourceLocation getImage(String url) {
        ResourceLocation image;
        if (url == null) {
            image = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/default_playlist_image.png");
        } else {
            image = ImageHandler.downloadImage(url);
        }

        return image;
    }

    private void addOrRemoveLikedSong() throws IOException, ParseException, SpotifyWebApiException {
        String[] ids = new String[]{currentTrackId};

        if (likedSong) {
            spotifyApi.removeUsersSavedTracks(ids).build().executeAsync();
            likedSong = false;
        } else {
            spotifyApi.saveTracksForUser(ids).build().executeAsync();
            likedSong = true;
        }

        updateLikeButton();
    }

    private boolean isSongLiked(String[] ids) throws IOException, ParseException, SpotifyWebApiException {
        Boolean[] liked = spotifyApi.checkUsersSavedTracks(ids).build().execute();

        return liked != null && liked[0];
    }

    private void updateLikeButton() {
        if (likeButton != null) {
            likeButton.setTexture(likedSong ? LIKED_TEXTURE : LIKE_TEXTURE);

            likeButton.setTooltip(likedSong ? "gui.spoticraft.liked" : "gui.spoticraft.like");

            likeButton.setX(imageWidth + 10 + font.width(musicName) + 2);
        }
    }

    @Override
    public void onClose() {
        // Stop the timer when the screen is closed
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        super.onClose();
    }

    private boolean checkIfExpired() {
        try {
            TokenStorage.checkIfExpired();
            tokenExpired = false;
            return false;
        } catch (IOException | URISyntaxException e) {
            tokenExpired = true;
            return true;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Pause the game when this screen is open
    }
}