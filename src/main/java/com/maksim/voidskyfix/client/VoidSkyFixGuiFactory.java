package com.maksim.voidskyfix.client;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import com.maksim.voidskyfix.VoidSkyFix;
import com.maksim.voidskyfix.VoidSkyFixConfig;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;

public class VoidSkyFixGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    public static class ConfigGui extends GuiConfig {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ConfigGui(GuiScreen parent) {
            super(
                parent,
                new ConfigElement(VoidSkyFixConfig.config.getCategory(Configuration.CATEGORY_GENERAL))
                    .getChildElements(),
                VoidSkyFix.MODID,
                false,
                false,
                "Void Sky Fix");
        }
    }
}
