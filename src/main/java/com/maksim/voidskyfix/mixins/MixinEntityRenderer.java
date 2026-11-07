package com.maksim.voidskyfix.mixins;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.world.WorldProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.maksim.voidskyfix.VoidSkyFixConfig;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    // Vanilla: d0 = playerY * getVoidFogYFactor(); if d0 < 1 the fog/sky color is scaled by d0^2 (black at d0 <= 0).
    // Normal worlds use factor 1/32, i.e. the sky darkens between Y=32 and Y=0.
    // Factor 1.0 makes d0 equal the player's Y; the shift below then moves the fade to Y = -depth .. 0.
    @Redirect(
        method = "updateFogColor",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;getVoidFogYFactor()D"),
        require = 0)
    private double voidskyfix$voidFog(WorldProvider provider) {
        return VoidSkyFixConfig.removeVoidDarkening ? 1.0D : provider.getVoidFogYFactor();
    }

    // d0 is the only double local in updateFogColor; modify it right after its first assignment.
    @ModifyVariable(method = "updateFogColor", at = @At(value = "STORE", ordinal = 0), ordinal = 0, require = 0)
    private double voidskyfix$voidFogDepth(double y) {
        if (!VoidSkyFixConfig.removeVoidDarkening) return y;
        double depth = VoidSkyFixConfig.voidDarkeningDepth;
        return depth > 0.0D ? (y + depth) / depth : y;
    }
}
