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

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        // Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        // EasyTechnology.LOG.info(Config.greeting);
        // EasyTechnology.LOG.info("I am EasyTechnology at version " + Tags.VERSION);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        NBTTagCompound craftingTweaks = new NBTTagCompound();
        craftingTweaks.setString("ContainerClass", PortableCraftingStationContainer.class.getName());
        craftingTweaks.setInteger("GridSlotNumber", 1);
        craftingTweaks.setInteger("GridSize", 9);
        craftingTweaks.setString("AlignToGrid", "left");
        FMLInterModComms.sendMessage("craftingtweaks", "RegisterProvider", craftingTweaks);
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}

    public void loadComplate(FMLLoadCompleteEvent event) {}
}
