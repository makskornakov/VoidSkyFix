package com.maksim.voidskyfix;

import java.io.File;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

/** config/voidskyfix.cfg — loaded on first access, re-synced when changed from the in-game config GUI. */
public final class VoidSkyFixConfig {

    public static final Configuration config;

    public static boolean removeBlackBox = true;
    public static boolean removeLowerSkyDisc = true;
    public static boolean removeVoidDarkening = true;
    public static int voidDarkeningDepth = 16;

    static {
        File dir = Launch.minecraftHome != null ? new File(Launch.minecraftHome, "config") : new File("config");
        config = new Configuration(new File(dir, "voidskyfix.cfg"));
        config.load();
        sync();
    }

    public static void sync() {
        removeBlackBox = config.getBoolean(
            "removeBlackBox",
            Configuration.CATEGORY_GENERAL,
            true,
            "Remove the black box drawn under the sky when the player is below the horizon (Y < 63).");
        removeLowerSkyDisc = config.getBoolean(
            "removeLowerSkyDisc",
            Configuration.CATEGORY_GENERAL,
            true,
            "Remove the lower sky disc (the blue square visible below you in void worlds).");
        removeVoidDarkening = config.getBoolean(
            "removeVoidDarkening",
            Configuration.CATEGORY_GENERAL,
            true,
            "Disable vanilla void fog darkening (fog/sky color fades to black below ~Y=32).");
        voidDarkeningDepth = config.getInt(
            "voidDarkeningDepth",
            Configuration.CATEGORY_GENERAL,
            16,
            0,
            256,
            "Only with removeVoidDarkening: the sky fades to black smoothly from Y=0 down to Y=-depth. 0 = hard cut at Y=0..1.");

        if (config.hasChanged()) config.save();
    }

    private VoidSkyFixConfig() {}
}
