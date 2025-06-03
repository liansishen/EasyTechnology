package com.hepdd.easytech;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hepdd.easytech.api.objects.VoidMinerUtilityEx;
import com.hepdd.easytech.loaders.preload.ETHLoaderItem;
import com.hepdd.easytech.loaders.preload.ETHLoaderMetaTileEntities;
import com.hepdd.easytech.loaders.preload.ETHLoaderRecipe;
import com.hepdd.easytech.loaders.preload.ETHStatics;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = EasyTechnology.MODID,
    version = Tags.VERSION,
    name = "EasyTechnology",
    acceptedMinecraftVersions = "[1.7.10]")
public class EasyTechnology {

    public static final String MODID = "easytech";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "com.hepdd.easytech.ClientProxy", serverSide = "com.hepdd.easytech.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        new ETHLoaderMetaTileEntities().run();
        new ETHLoaderItem().run();
        new ETHLoaderRecipe().run();
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void loadComplated(FMLLoadCompleteEvent event) {
        proxy.loadComplate(event);
        VoidMinerUtilityEx.generateDropMaps();
        new ETHStatics().run();
    }
}
