package com.maksim.voidskyfix;

import net.minecraftforge.common.MinecraftForge;

import com.maksim.voidskyfix.client.FogDebug;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(
    modid = VoidSkyFix.MODID,
    name = "Void Sky Fix",
    useMetadata = true,
    acceptableRemoteVersions = "*",
    guiFactory = "com.maksim.voidskyfix.client.VoidSkyFixGuiFactory")
public class VoidSkyFix {

    public static final String MODID = "voidskyfix";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        VoidSkyFixConfig.sync();
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        if (event.getSide()
            .isClient()) MinecraftForge.EVENT_BUS.register(new FogDebug());
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (MODID.equals(event.modID)) VoidSkyFixConfig.sync();
    }
}
