package com.maksim.voidskyfix.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.maksim.voidskyfix.VoidSkyFixConfig;
import com.maksim.voidskyfix.client.SkyDome;

/**
 * globalVoidFogFix: some mods copy the vanilla void darkening (d0 = playerY * getVoidFogYFactor(), color *= d0^2) and
 * call getVoidFogYFactor() directly, bypassing our redirect in EntityRenderer.updateFogColor. Biomes O' Plenty does
 * this in FogHandler.postProcessColor, which turned the fog almost black below Y=32 in its biomes.
 * Here the method itself returns a factor such that playerY * factor equals our shifted value (Y + depth) / depth,
 * so such mods darken exactly like the vanilla path. Dimensions that override this method are not affected.
 */
@Mixin(WorldProvider.class)
public abstract class MixinWorldProvider {

    @Inject(method = "getVoidFogYFactor", at = @At("HEAD"), cancellable = true, require = 0)
    private void voidskyfix$globalVoidFog(CallbackInfoReturnable<Double> cir) {
        if (!VoidSkyFixConfig.removeVoidDarkening || !VoidSkyFixConfig.globalVoidFogFix) return;
        double depth = VoidSkyFixConfig.voidDarkeningDepth;
        if (depth <= 0.0D) {
            cir.setReturnValue(1.0D); // d0 = Y: hard cut at Y = 0..1
            return;
        }
        EntityLivingBase viewer = Minecraft.getMinecraft().renderViewEntity;
        if (viewer == null) return;

        // Same interpolated Y the callers use (they get the same partialTicks from the frame).
        float pt = SkyDome.partialTicks;
        double y = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * pt;
        if (Math.abs(y) < 1.0E-4D) y = y < 0.0D ? -1.0E-4D : 1.0E-4D;
        cir.setReturnValue(((y + depth) / depth) / y);
    }
}
