package com.maksim.voidskyfix;

import java.io.File;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

/** config/voidskyfix.cfg — loaded on first access, re-synced when changed from the in-game config GUI. */
public final class VoidSkyFixConfig {

    public static final Configuration config;

    private static final String LANG = "voidskyfix.config.";

    public static boolean removeBlackBox = true;
    public static boolean removeLowerSkyDisc = true;
    public static boolean removeVoidDarkening = false;
    public static int voidDarkeningDepth = 16;
    public static boolean uniformSkyColor = false;
    public static boolean fixFogBrightness = false;
    public static boolean gradientSky = false;
    public static boolean voidDarkenWholeSky = false;
    public static boolean directionalSunset = false;
    public static boolean highDetailSky = false;
    public static boolean globalVoidFogFix = false;
    public static boolean debugFog = false;

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
            "Remove the black box drawn under the sky when the player is below the horizon (Y < 63).",
            LANG + "removeBlackBox");
        removeLowerSkyDisc = config.getBoolean(
            "removeLowerSkyDisc",
            Configuration.CATEGORY_GENERAL,
            true,
            "Remove the lower sky disc (the blue square visible below you in void worlds).",
            LANG + "removeLowerSkyDisc");
        removeVoidDarkening = config.getBoolean(
            "removeVoidDarkening",
            Configuration.CATEGORY_GENERAL,
            false,
            "Disable vanilla void fog darkening (fog/sky color fades to black below ~Y=32).",
            LANG + "removeVoidDarkening");
        voidDarkeningDepth = config.getInt(
            "voidDarkeningDepth",
            Configuration.CATEGORY_GENERAL,
            16,
            0,
            256,
            "Only with removeVoidDarkening: the sky fades to black smoothly from Y=0 down to Y=-depth. 0 = hard cut at Y=0..1.",
            LANG + "voidDarkeningDepth");
        globalVoidFogFix = config.getBoolean(
            "globalVoidFogFix",
            Configuration.CATEGORY_GENERAL,
            false,
            "Only with removeVoidDarkening: also apply it to mods that copy the vanilla void darkening (e.g. Biomes O' Plenty biome fog turning black below Y=32).",
            LANG + "globalVoidFogFix");
        uniformSkyColor = config.getBoolean(
            "uniformSkyColor",
            Configuration.CATEGORY_GENERAL,
            false,
            "Make the fog color match the sky color, so the sky is one color everywhere with no pale horizon band. Also tints distant-terrain fog sky-colored. Turn off if a resource pack's custom sky looks wrong.",
            LANG + "uniformSkyColor");
        fixFogBrightness = config.getBoolean(
            "fixFogBrightness",
            Configuration.CATEGORY_GENERAL,
            false,
            "Fix the vanilla fog brightening in the dark at render distance > 16 (turquoise sky below Y=0, flash when flying up).",
            LANG + "fixFogBrightness");
        gradientSky = config.getBoolean(
            "gradientSky",
            Configuration.CATEGORY_GENERAL,
            false,
            "Draw the sky as a smooth sphere: sky color above and below, fog color at the horizon. No seam between the upper and lower sky, independent of fog settings.",
            LANG + "gradientSky");
        voidDarkenWholeSky = config.getBoolean(
            "voidDarkenWholeSky",
            Configuration.CATEGORY_GENERAL,
            false,
            "With gradientSky: void darkening darkens the whole sky. Off = only the horizon and below.",
            LANG + "voidDarkenWholeSky");
        directionalSunset = config.getBoolean(
            "directionalSunset",
            Configuration.CATEGORY_GENERAL,
            false,
            "With gradientSky: sunrise/sunset glow is computed for each direction of the sky instead of the vanilla glow fan and camera-dependent fog tint.",
            LANG + "directionalSunset");
        highDetailSky = config.getBoolean(
            "highDetailSky",
            Configuration.CATEGORY_GENERAL,
            false,
            "With gradientSky: denser sky mesh near the horizon, removes faint lines in the gradient at sunrise/sunset.",
            LANG + "highDetailSky");
        debugFog = config.getBoolean(
            "debugFog",
            Configuration.CATEGORY_GENERAL,
            false,
            "Diagnostics: show fog color at each rendering stage on the F3 screen.",
            LANG + "debugFog");

        if (config.hasChanged()) config.save();
    }

    public static boolean directionalSunsetActive() {
        return gradientSky && directionalSunset;
    }

    private VoidSkyFixConfig() {}
}
