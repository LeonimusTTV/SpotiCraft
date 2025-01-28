package com.leonimust.spoticraft.neoforge.client;

import com.leonimust.spoticraft.Main;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static KeyMapping openSpotifyKey;

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        openSpotifyKey = new KeyMapping(
                "key.spoticraft.open_spotify", // Translation key
                GLFW.GLFW_KEY_P,               // Default key
                "key.categories.spoticraft"    // Category
        );
        event.register(openSpotifyKey);
    }
}