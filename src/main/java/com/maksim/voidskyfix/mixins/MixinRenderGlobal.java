package com.maksim.voidskyfix.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.maksim.voidskyfix.VoidSkyFixConfig;
import com.maksim.voidskyfix.client.FogDebug;
import com.maksim.voidskyfix.client.SkyDome;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Shadow
    private int glSkyList;

    @Shadow
    private int glSkyList2;

    @Shadow
    private WorldClient theWorld;

    @Shadow
    private Minecraft mc;

    @Unique
    private float voidskyfix$partialTicks;

    // 1) Never "below horizon" -> the black box is not drawn.
    @Redirect(
        method = "renderSky",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getHorizon()D"),
        require = 0)
    private double voidskyfix$horizonClient(WorldClient world) {
        return VoidSkyFixConfig.removeBlackBox ? -1000.0D : world.getHorizon();
    }

    // 2) Skip the lower sky disc (the blue square). Vanilla GL11 path:
    @Redirect(
        method = "renderSky",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V", remap = false),
        require = 0)
    // 3) gradientSky: draw our own sphere instead of the upper sky plane.
    private void voidskyfix$callListGL11(int list) {
        if (list == this.glSkyList2 && VoidSkyFixConfig.removeLowerSkyDisc) return;
        if (list == this.glSkyList && VoidSkyFixConfig.gradientSky && voidskyfix$drawGradientSky()) return;
        GL11.glCallList(list);
    }

    @Unique
    private boolean voidskyfix$drawGradientSky() {
        if (this.theWorld == null || this.mc.renderViewEntity == null || !this.theWorld.provider.isSurfaceWorld())
            return false;
        float pt = this.voidskyfix$partialTicks;
        Vec3 sky = this.theWorld.getSkyColor(this.mc.renderViewEntity, pt);

        float[] sunrise = null;
        float sunX = 1.0F, tintDim = 1.0F;
        if (VoidSkyFixConfig.directionalSunsetActive()) {
            // Called directly on the provider, so the redirect below (which hides it from vanilla) doesn't apply.
            sunrise = this.theWorld.provider.calcSunriseSunsetColors(this.theWorld.getCelestialAngle(pt), pt);
            sunX = MathHelper.sin(this.theWorld.getCelestialAngleRadians(pt)) > 0.0F ? -1.0F : 1.0F;
            tintDim = 1.0F - this.theWorld.getRainStrength(pt) * 0.5F;
        }

        SkyDome.draw(
            (float) sky.xCoord,
            (float) sky.yCoord,
            (float) sky.zCoord,
            VoidSkyFixConfig.voidDarkenWholeSky,
            VoidSkyFixConfig.highDetailSky,
            sunrise,
            sunX,
            tintDim);
        return true;
    }

    // directionalSunset: the dome already has the sunrise/sunset tint per vertex, so skip the vanilla glow fan.
    @Redirect(
        method = "renderSky",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;calcSunriseSunsetColors(FF)[F"),
        require = 0)
    private float[] voidskyfix$sunsetFan(WorldProvider provider, float celestialAngle, float partialTicks) {
        if (VoidSkyFixConfig.directionalSunsetActive() && provider.isSurfaceWorld()) return null;
        return provider.calcSunriseSunsetColors(celestialAngle, partialTicks);
    }

    // Remember partialTicks for the gradient sky; debug: real GL fog/clear state before the sky is drawn.
    @Inject(method = "renderSky", at = @At("HEAD"), require = 0)
    private void voidskyfix$renderSkyHead(float partialTicks, CallbackInfo ci) {
        this.voidskyfix$partialTicks = partialTicks;
        if (VoidSkyFixConfig.debugFog) FogDebug.captureGl();
    }
}
