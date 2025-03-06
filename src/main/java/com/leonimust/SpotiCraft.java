package com.leonimust;

import com.leonimust.client.TokenStorage;
import com.leonimust.client.ui.SpotifyScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotiCraft implements ClientModInitializer {
	public static final String MOD_ID = "spoticraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Remove static modifiers (client-only fields)
	private KeyBinding openSpotifyKey;
	private boolean wasPressed = false;

	@Override
	public void onInitializeClient() {
		openSpotifyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.spoticraft.open_spotify",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_P,
				"key.categories.spoticraft"
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