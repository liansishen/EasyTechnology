package com.hepdd.easytech.proxy;

import net.minecraft.nbt.NBTTagCompound;

import com.hepdd.easytech.api.objects.PortableCraftingStationContainer;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {}

    public void init(FMLInitializationEvent event) {
        NBTTagCompound craftingTweaks = new NBTTagCompound();
        craftingTweaks.setString("ContainerClass", PortableCraftingStationContainer.class.getName());
        craftingTweaks.setInteger("GridSlotNumber", 1);
        craftingTweaks.setInteger("GridSize", 9);
        craftingTweaks.setString("AlignToGrid", "left");
        FMLInterModComms.sendMessage("craftingtweaks", "RegisterProvider", craftingTweaks);
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {}

    public void loadComplate(FMLLoadCompleteEvent event) {}

    public String getOpenCraftingStationKeyName() {
        return "None";
    }
}
