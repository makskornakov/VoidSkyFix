package com.maksim.voidskyfix.mixins;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.maksim.voidskyfix.VoidSkyFixConfig;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Shadow
    private int glSkyList2;

    // 1) Never "below horizon" -> the black box is not drawn.
    @Redirect(
        method = "renderSky",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getHorizon()D"),
        require = 0)
    private double voidskyfix$horizonClient(WorldClient world) {
        return VoidSkyFixConfig.removeBlackBox ? -1000.0D : world.getHorizon();
    }

    @Redirect(
        method = "renderSky",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getHorizon()D"),
        require = 0)
    private double voidskyfix$horizonWorld(World world) {
        return VoidSkyFixConfig.removeBlackBox ? -1000.0D : world.getHorizon();
    }

    // 2) Skip the lower sky disc (the blue square). Vanilla GL11 path:
    @Redirect(
        method = "renderSky",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V", remap = false),
        require = 0)
    private void voidskyfix$callListGL11(int list) {
        if (list != this.glSkyList2 || !VoidSkyFixConfig.removeLowerSkyDisc) GL11.glCallList(list);
    }
}
