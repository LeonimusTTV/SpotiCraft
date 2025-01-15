package com.leonimust.spoticraft;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SpotiCraft.MOD_ID)
public class SpotiCraft {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "spoticraft";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public SpotiCraft(FMLJavaModLoadingContext context) {

    }
}
