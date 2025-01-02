package com.leonimust.spoticraft.client;

import com.leonimust.spoticraft.SpotiCraft;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SpotiCraft.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {
    private static boolean wasPressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (ClientSetup.openSpotifyKey.isDown()) {
            if (!wasPressed) {
                wasPressed = true;
                TokenStorage.loadToken();
                Minecraft.getInstance().setScreen(new SpotifyScreen());
                System.out.println("tetwatwattw");
            }
        } else {
            wasPressed = false;
        }
    }
}