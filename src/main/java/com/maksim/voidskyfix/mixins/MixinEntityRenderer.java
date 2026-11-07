package com.maksim.voidskyfix.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.maksim.voidskyfix.VoidSkyFixConfig;
import com.maksim.voidskyfix.client.FogDebug;
import com.maksim.voidskyfix.client.SkyDome;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow
    private Minecraft mc;

    @Shadow
    float fogColorRed;

    @Shadow
    float fogColorGreen;

    @Shadow
    float fogColorBlue;

    @Shadow
    private float fogColor1;

    @Shadow
    private float fogColor2;

    // Uniform sky: the background (glClearColor) and the fade of the upper sky plane both use the fog color, which
    // vanilla takes from WorldProvider.getFogColor() (pale, almost white). Replacing it with the sky color makes the
    // whole sky one color, with no light horizon band. Injected after the vanilla sky/rain/thunder blending and
    // before the water/lava/cloud overrides, so those still work, as do void darkening, blindness and night vision.
    @Inject(method = "updateFogColor", at = @At("HEAD"), require = 0)
    private void voidskyfix$fogFrameStart(float partialTicks, CallbackInfo ci) {
        SkyDome.partialTicks = partialTicks;
    }

    @Inject(
        method = "updateFogColor",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;F)Lnet/minecraft/block/Block;"),
        require = 0)
    private void voidskyfix$uniformSky(float partialTicks, CallbackInfo ci) {
        if (VoidSkyFixConfig.debugFog) FogDebug.set(FogDebug.vanillaFog, fogColorRed, fogColorGreen, fogColorBlue);
        voidskyfix$applyUniformSky(partialTicks);
        if (VoidSkyFixConfig.debugFog) FogDebug.set(FogDebug.uniformFog, fogColorRed, fogColorGreen, fogColorBlue);
    }

    @Unique
    private void voidskyfix$applyUniformSky(float partialTicks) {
        if (!VoidSkyFixConfig.uniformSkyColor) return;
        WorldClient world = this.mc.theWorld;
        EntityLivingBase viewer = this.mc.renderViewEntity;
        if (world == null || viewer == null || !world.provider.isSurfaceWorld()) return;

        // Already includes biome color, time of day, rain, thunder and lightning flashes.
        Vec3 sky = world.getSkyColor(viewer, partialTicks);
        float r = (float) sky.xCoord;
        float g = (float) sky.yCoord;
        float b = (float) sky.zCoord;

        // Keep the vanilla sunrise/sunset tint when looking towards the sun (not needed with directionalSunset:
        // the dome tints each direction itself, and a camera-dependent fog tint would recolor the whole horizon).
        if (this.mc.gameSettings.renderDistanceChunks >= 4 && !VoidSkyFixConfig.directionalSunsetActive()) {
            Vec3 sunDir = Vec3.createVectorHelper(
                MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) > 0.0F ? -1.0D : 1.0D,
                0.0D,
                0.0D);
            float f = (float) viewer.getLook(partialTicks)
                .dotProduct(sunDir);
            if (f > 0.0F) {
                float[] c = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);
                if (c != null) {
                    f *= c[3];
                    r = r * (1.0F - f) + c[0] * f;
                    g = g * (1.0F - f) + c[1] * f;
                    b = b * (1.0F - f) + c[2] * f;
                }
            }
        }

        this.fogColorRed = r;
        this.fogColorGreen = g;
        this.fogColorBlue = b;
    }

    // directionalSunset: no camera-dependent sunrise tint in the fog (the dome tints each direction itself).
    @Redirect(
        method = "updateFogColor",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;calcSunriseSunsetColors(FF)[F"),
        require = 0)
    private float[] voidskyfix$fogSunsetTint(WorldProvider provider, float celestialAngle, float partialTicks) {
        if (VoidSkyFixConfig.directionalSunsetActive() && provider.isSurfaceWorld()) return null;
        return provider.calcSunriseSunsetColors(celestialAngle, partialTicks);
    }

    // Vanilla: d0 = playerY * getVoidFogYFactor(); if d0 < 1 the fog/sky color is scaled by d0^2 (black at d0 <= 0).
    // Normal worlds use factor 1/32, i.e. the sky darkens between Y=32 and Y=0.
    // Factor 1.0 makes d0 equal the player's Y; the shift below then moves the fade to Y = -depth .. 0.
    @Redirect(
        method = "updateFogColor",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;getVoidFogYFactor()D"),
        require = 0)
    private double voidskyfix$voidFog(WorldProvider provider) {
        if (VoidSkyFixConfig.debugFog) {
            FogDebug.set(FogDebug.preVoidFog, fogColorRed, fogColorGreen, fogColorBlue);
            FogDebug.fogColor1 = fogColor1;
            FogDebug.fogColor2 = fogColor2;
        }
        return VoidSkyFixConfig.removeVoidDarkening ? 1.0D : provider.getVoidFogYFactor();
    }

    // d0 is the only double local in updateFogColor; modify it right after its first assignment.
    @ModifyVariable(method = "updateFogColor", at = @At(value = "STORE", ordinal = 0), ordinal = 0, require = 0)
    private double voidskyfix$voidFogDepth(double y) {
        double out = y;
        if (VoidSkyFixConfig.removeVoidDarkening) {
            double depth = VoidSkyFixConfig.voidDarkeningDepth;
            if (depth > 0.0D) out = (y + depth) / depth;
        }
        double k = out < 1.0D ? Math.max(out, 0.0D) : 1.0D;
        SkyDome.voidFactor = (float) (k * k);
        if (VoidSkyFixConfig.debugFog) {
            FogDebug.voidIn = y;
            FogDebug.voidOut = out;
        }
        return out;
    }

    // The final fog color handed to glClearColor: the gradient sky uses it as its horizon color.
    @Inject(
        method = "updateFogColor",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glClearColor(FFFF)V", remap = false),
        require = 0)
    private void voidskyfix$finalFogColor(float partialTicks, CallbackInfo ci) {
        SkyDome.fog[0] = fogColorRed;
        SkyDome.fog[1] = fogColorGreen;
        SkyDome.fog[2] = fogColorBlue;
        if (VoidSkyFixConfig.debugFog) FogDebug.set(FogDebug.clearFog, fogColorRed, fogColorGreen, fogColorBlue);
    }

    // Vanilla bug with render distance > 16 chunks: the fog brightness multiplier fogColor1 targets
    // light * (1 - rd/16) + rd/16, which goes above 1 in the dark (up to 2x at 32 chunks) and turns the fog
    // bright turquoise below Y=0 or in caves. Vanilla's slider stops at 16, where it is exactly 1 at most.
    @Inject(method = "updateRenderer", at = @At("TAIL"), require = 0)
    private void voidskyfix$clampFogBrightness(CallbackInfo ci) {
        if (VoidSkyFixConfig.fixFogBrightness && fogColor1 > 1.0F) fogColor1 = 1.0F;
    }
}
