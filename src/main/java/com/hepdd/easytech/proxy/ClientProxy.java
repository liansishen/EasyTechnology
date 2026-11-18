package com.hepdd.easytech.proxy;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.hepdd.easytech.loaders.preload.ETHLoaderKeybind;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        new ETHLoaderKeybind().run();
        super.init(event);
    }

    @Override
    public String getOpenCraftingStationKeyName() {
        KeyBinding keyBinding = ETHLoaderKeybind.openCraftingStation;
        if (keyBinding == null) return "None";
        int keyCode = keyBinding.getKeyCode();
        if (keyCode == Keyboard.KEY_NONE) return "None";
        String display = GameSettings.getKeyDisplayString(keyCode);
        return display == null || display.isEmpty() ? "None" : display;
    }
}
