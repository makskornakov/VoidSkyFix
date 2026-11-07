package com.maksim.voidskyfix.client;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.maksim.voidskyfix.VoidSkyFixConfig;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Temporary diagnostics (option debugFog): records the fog color at each stage of EntityRenderer.updateFogColor and
 * what OpenGL actually holds right before the sky is drawn, and prints it on the F3 screen.
 */
public final class FogDebug {

    public static final float[] vanillaFog = new float[3]; // before uniformSkyColor (vanilla blend incl. rain)
    public static final float[] uniformFog = new float[3]; // after uniformSkyColor
    public static final float[] preVoidFog = new float[3]; // after water/lava/cloud and the light-brightness multiplier
    public static final float[] clearFog = new float[3]; // what updateFogColor passes to glClearColor
    public static float fogColor1, fogColor2;
    public static double voidIn, voidOut;

    public static final float[] glFogColor = new float[4];
    public static final float[] glClearValue = new float[4];
    public static float glFogStart, glFogEnd;
    public static int glFogMode;

    private static final FloatBuffer BUF = BufferUtils.createFloatBuffer(16);

    public static void set(float[] dst, float r, float g, float b) {
        dst[0] = r;
        dst[1] = g;
        dst[2] = b;
    }

    /** Called at the start of RenderGlobal.renderSky: reads the real GL state (after any other mod's changes). */
    public static void captureGl() {
        readGl(GL11.GL_FOG_COLOR, glFogColor);
        readGl(GL11.GL_COLOR_CLEAR_VALUE, glClearValue);
        glFogStart = GL11.glGetFloat(GL11.GL_FOG_START);
        glFogEnd = GL11.glGetFloat(GL11.GL_FOG_END);
        glFogMode = GL11.glGetInteger(GL11.GL_FOG_MODE);
    }

    private static void readGl(int pname, float[] dst) {
        BUF.clear();
        GL11.glGetFloat(pname, BUF);
        for (int i = 0; i < 4; i++) dst[i] = BUF.get(i);
    }

    @SubscribeEvent
    public void onOverlayText(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!VoidSkyFixConfig.debugFog || !mc.gameSettings.showDebugInfo || mc.theWorld == null) return;
        EntityLivingBase e = mc.renderViewEntity;
        if (e == null) return;

        List<String> out = new ArrayList<>();
        int x = MathHelper.floor_double(e.posX), y = MathHelper.floor_double(e.posY),
            z = MathHelper.floor_double(e.posZ);
        Vec3 sky = mc.theWorld.getSkyColor(e, 1.0F);
        Vec3 fog = mc.theWorld.getFogColor(1.0F);

        out.add(
            String.format(
                Locale.ROOT,
                "[VSF] Y=%.2f rd=%d light=%.3f fc1=%.3f fc2=%.3f creative=%s",
                e.posY,
                mc.gameSettings.renderDistanceChunks,
                mc.theWorld.getLightBrightness(x, y, z),
                fogColor1,
                fogColor2,
                mc.playerController != null && mc.playerController.isInCreativeMode()));
        out.add("[VSF] sky color     " + rgb((float) sky.xCoord, (float) sky.yCoord, (float) sky.zCoord));
        out.add("[VSF] provider fog  " + rgb((float) fog.xCoord, (float) fog.yCoord, (float) fog.zCoord));
        out.add("[VSF] 1 vanilla     " + rgb(vanillaFog));
        out.add("[VSF] 2 uniform     " + rgb(uniformFog));
        out.add("[VSF] 3 pre-void    " + rgb(preVoidFog));
        out.add(
            String.format(
                Locale.ROOT,
                "[VSF]   void d0 %.3f -> %.3f (x%.3f)",
                voidIn,
                voidOut,
                voidOut < 1.0D ? Math.max(voidOut, 0.0D) * Math.max(voidOut, 0.0D) : 1.0D));
        out.add("[VSF] 4 clearColor  " + rgb(clearFog));
        out.add("[VSF] GL clear      " + rgb(glClearValue));
        out.add("[VSF] GL fog color  " + rgb(glFogColor));
        out.add(
            String.format(
                Locale.ROOT,
                "[VSF] GL fog %s start=%.1f end=%.1f",
                glFogMode == GL11.GL_LINEAR ? "LINEAR"
                    : glFogMode == GL11.GL_EXP ? "EXP" : "0x" + Integer.toHexString(glFogMode),
                glFogStart,
                glFogEnd));
        out.add("");
        event.left.addAll(0, out); // top of the list: GTNH's F3 is long and the bottom goes off-screen
    }

    private static String rgb(float[] c) {
        return rgb(c[0], c[1], c[2]);
    }

    private static String rgb(float r, float g, float b) {
        return String
            .format(Locale.ROOT, "#%02X%02X%02X (%.3f %.3f %.3f)", clamp255(r), clamp255(g), clamp255(b), r, g, b);
    }

    private static int clamp255(float v) {
        return Math.max(0, Math.min(255, Math.round(v * 255.0F)));
    }
}
