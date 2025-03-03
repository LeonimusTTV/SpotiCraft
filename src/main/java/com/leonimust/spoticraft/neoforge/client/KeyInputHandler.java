package com.leonimust.spoticraft.neoforge.client;

import com.leonimust.spoticraft.Main;
import com.leonimust.spoticraft.neoforge.client.TokenStorage;
import com.leonimust.spoticraft.neoforge.client.ui.SpotifyScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {
    private static boolean wasPressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (ClientSetup.openSpotifyKey.isDown()) {
            if (!wasPressed) {
                wasPressed = true;
                TokenStorage.loadToken();
                Minecraft.getInstance().setScreen(new SpotifyScreen());
            }
        } else {
            wasPressed = false;
        }
    }
}