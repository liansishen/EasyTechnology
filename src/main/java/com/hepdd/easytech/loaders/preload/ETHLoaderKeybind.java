package com.hepdd.easytech.loaders.preload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.hepdd.easytech.EasyTechnology;
import com.hepdd.easytech.network.PacketOpenCraftingStation;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ETHLoaderKeybind implements Runnable {

    @Override
    public void run() {
        bindKeys();
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    public static KeyBinding openCraftingStation;

    public void bindKeys() {
        openCraftingStation = new KeyBinding(
            EasyTechnology.MODID + ".key.open_crafting_station",
            Keyboard.KEY_NONE,
            "itemGroup." + EasyTechnology.MODID);
        ClientRegistry.registerKeyBinding(openCraftingStation);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null) return;
        if (openCraftingStation.isPressed()) {
            ETHStatics.NW.sendToServer(new PacketOpenCraftingStation());
        }
    }
}
