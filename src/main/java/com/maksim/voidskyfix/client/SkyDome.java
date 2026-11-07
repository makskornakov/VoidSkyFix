package com.maksim.voidskyfix.client;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

/**
 * Gradient sky: a sphere around the camera drawn instead of the vanilla upper sky plane. Each vertex is colored by its
 * elevation: zenith and nadir get the sky color, the horizon gets the fog color. Optionally each vertex also gets the
 * sunrise/sunset tint by its own direction to the sun (instead of the vanilla glow fan). Fog is disabled while drawing,
 * so the look doesn't depend on fog settings (Angelica effectively turns sky fog off).
 */
public final class SkyDome {

    /** Final fog color of the current frame (what updateFogColor hands to glClearColor). */
    public static final float[] fog = { 1.0F, 1.0F, 1.0F };
    /** partialTicks of the current frame, recorded at the start of EntityRenderer.updateFogColor. */
    public static float partialTicks;
    /** Void darkening multiplier of the current frame (1 = none, 0 = black). */
    public static float voidFactor = 1.0F;

    private static final float[] COLOR = new float[3];
    private static final double RADIUS = 16.0D; // well inside the near/far planes at any render distance

    /** Precomputed sphere grid: latitudes from nadir to zenith, longitudes around. */
    private static final class Mesh {

        final float[] sinLat, cosLat, sinLon, cosLon;

        Mesh(double[] latitudes, int segments) {
            int n = latitudes.length;
            sinLat = new float[n];
            cosLat = new float[n];
            for (int i = 0; i < n; i++) {
                sinLat[i] = (float) Math.sin(latitudes[i]);
                cosLat[i] = (float) Math.cos(latitudes[i]);
            }
            sinLon = new float[segments + 1];
            cosLon = new float[segments + 1];
            for (int j = 0; j <= segments; j++) {
                double lon = 2.0D * Math.PI * j / segments;
                sinLon[j] = (float) Math.sin(lon);
                cosLon[j] = (float) Math.cos(lon);
            }
        }
    }

    /** 24 evenly spaced rings (7.5 degrees apart), 32 segments. */
    private static final Mesh NORMAL = new Mesh(evenLatitudes(24), 32);
    /**
     * 2 x 40 rings packed towards the horizon (lat = 90 * (k/40)^2: ~0.06 degrees apart at the horizon, ~4 at the top),
     * 64 segments. Linear color interpolation between sparse rings makes visible kinks (Mach bands) in the gradient.
     */
    private static final Mesh HIGH = new Mesh(horizonPackedLatitudes(40), 64);

    private static double[] evenLatitudes(int rings) {
        double[] lat = new double[rings + 1];
        for (int i = 0; i <= rings; i++) lat[i] = -Math.PI / 2.0D + Math.PI * i / rings;
        return lat;
    }

    private static double[] horizonPackedLatitudes(int perHemisphere) {
        double[] lat = new double[2 * perHemisphere + 1];
        for (int k = 0; k <= perHemisphere; k++) {
            double u = (double) k / perHemisphere;
            double a = Math.PI / 2.0D * u * u;
            lat[perHemisphere + k] = a;
            lat[perHemisphere - k] = -a;
        }
        return lat;
    }

    /**
     * @param sunrise vanilla sunrise/sunset color {r, g, b, alpha}, or null for no per-vertex tint
     * @param sunX    x direction of the sunrise/sunset point on the horizon (+1 or -1)
     * @param tintDim extra multiplier for the tint (rain)
     */
    public static void draw(float skyR, float skyG, float skyB, boolean darkenSky, boolean highDetail, float[] sunrise,
        float sunX, float tintDim) {
        float k = darkenSky ? voidFactor : 1.0F;
        skyR *= k;
        skyG *= k;
        skyB *= k;

        float tintA = 0.0F, tintR = 0.0F, tintG = 0.0F, tintB = 0.0F;
        if (sunrise != null) {
            tintA = sunrise[3];
            float d = voidFactor * tintDim;
            tintR = sunrise[0] * d;
            tintG = sunrise[1] * d;
            tintB = sunrise[2] * d;
        }

        Mesh m = highDetail ? HIGH : NORMAL;
        int rings = m.sinLat.length - 1;
        int segments = m.sinLon.length - 1;

        boolean fogOn = GL11.glIsEnabled(GL11.GL_FOG);
        boolean cullOn = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        int shade = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        float[] c = COLOR;
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (int i = 0; i < rings; i++) {
            float s0 = m.sinLat[i], s1 = m.sinLat[i + 1];
            float cl0 = m.cosLat[i], cl1 = m.cosLat[i + 1];
            float w0 = skyWeight(s0), w1 = skyWeight(s1);
            double y0 = RADIUS * s0, y1 = RADIUS * s1;
            double r0 = RADIUS * cl0, r1 = RADIUS * cl1;
            for (int j = 0; j < segments; j++) {
                float ca = m.cosLon[j], sa = m.sinLon[j];
                float cb = m.cosLon[j + 1], sb = m.sinLon[j + 1];

                vertexColor(c, w0, s0, cl0 * ca * sunX, skyR, skyG, skyB, tintA, tintR, tintG, tintB);
                t.setColorRGBA_F(c[0], c[1], c[2], 1.0F);
                t.addVertex(r0 * ca, y0, r0 * sa);
                vertexColor(c, w0, s0, cl0 * cb * sunX, skyR, skyG, skyB, tintA, tintR, tintG, tintB);
                t.setColorRGBA_F(c[0], c[1], c[2], 1.0F);
                t.addVertex(r0 * cb, y0, r0 * sb);
                vertexColor(c, w1, s1, cl1 * cb * sunX, skyR, skyG, skyB, tintA, tintR, tintG, tintB);
                t.setColorRGBA_F(c[0], c[1], c[2], 1.0F);
                t.addVertex(r1 * cb, y1, r1 * sb);
                vertexColor(c, w1, s1, cl1 * ca * sunX, skyR, skyG, skyB, tintA, tintR, tintG, tintB);
                t.setColorRGBA_F(c[0], c[1], c[2], 1.0F);
                t.addVertex(r1 * ca, y1, r1 * sa);
            }
        }
        t.draw();

        GL11.glShadeModel(shade);
        if (cullOn) GL11.glEnable(GL11.GL_CULL_FACE);
        if (fogOn) GL11.glEnable(GL11.GL_FOG);
    }

    /**
     * @param w      sky weight (0 at the horizon, 1 at zenith/nadir)
     * @param sinLat elevation of the vertex
     * @param sunDot cos of the angle between the vertex direction and the sunrise point on the horizon
     */
    private static void vertexColor(float[] out, float w, float sinLat, float sunDot, float skyR, float skyG,
        float skyB, float tintA, float tintR, float tintG, float tintB) {
        float r = fog[0] + (skyR - fog[0]) * w;
        float g = fog[1] + (skyG - fog[1]) * w;
        float b = fog[2] + (skyB - fog[2]) * w;
        if (tintA > 0.0F && sunDot > 0.0F) {
            // Strongest on the horizon right under the sun, fading sideways and up/down (mirrored below).
            float v = 1.0F - sinLat * sinLat;
            float tint = tintA * sunDot * sunDot * v * v;
            r += (tintR - r) * tint;
            g += (tintG - g) * tint;
            b += (tintB - b) * tint;
        }
        out[0] = r;
        out[1] = g;
        out[2] = b;
    }

    /**
     * 0 at the horizon, 1 at zenith/nadir; a soft band around the horizon. Uses sin^2 (not |sin|), so the curve is flat
     * at the horizon: a kink there shows up as a visible line (Mach band) where the upper and lower halves meet.
     */
    private static float skyWeight(float sinLat) {
        float q = 1.0F - sinLat * sinLat; // 1 at the horizon
        q *= q; // ^2
        q *= q; // ^4
        q *= q; // ^8
        q *= q; // ^16
        return 1.0F - q;
    }

    private SkyDome() {}
}
