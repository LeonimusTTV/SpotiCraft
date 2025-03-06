package com.leonimust;

import com.leonimust.client.TokenStorage;
import com.leonimust.client.ui.SpotifyScreen;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotiCraft implements ModInitializer {
	public static final String MOD_ID = "spoticraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static KeyBinding openSpotifyKey;
	private static boolean wasPressed = false;

	@Override
	public void onInitialize() {
		openSpotifyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.spoticraft.open_spotify", // Translation key
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_P,               // Default key
				"key.categories.spoticraft"    // Category
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (openSpotifyKey.wasPressed()) {
				if (!wasPressed) {
					wasPressed = true;
					TokenStorage.loadToken();
                    MinecraftClient.getInstance().setScreen(new SpotifyScreen());
				}
			} else {
				wasPressed = false;
			}
		});
	}
}