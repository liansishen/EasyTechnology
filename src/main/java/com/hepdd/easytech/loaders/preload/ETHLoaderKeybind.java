package com.hepdd.easytech.loaders.preload;

import com.hepdd.easytech.EasyTechnology;
import com.hepdd.easytech.GuiHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

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
        openCraftingStation = new KeyBinding(EasyTechnology.MODID+".key.open_crafting_station",
            Keyboard.KEY_NONE,
            "itemGroup."+ EasyTechnology.MODID);
        ClientRegistry.registerKeyBinding(openCraftingStation);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null) return;
        EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
        if (openCraftingStation.isPressed() && !p.isClientWorld()) {
            p.openGui(
                EasyTechnology.instance,
                GuiHandler.GUI1,
                Minecraft.getMinecraft().theWorld,
                (int) p.posX,
                (int) p.posY,
                (int) p.posZ);
        }
    }
}
