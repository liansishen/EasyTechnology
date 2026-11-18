package com.hepdd.easytech;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hepdd.easytech.api.objects.GTChunkManagerEx;
import com.hepdd.easytech.common.ETHNetwork;
import com.hepdd.easytech.loaders.preload.ETHLoaderItem;
import com.hepdd.easytech.loaders.preload.ETHLoaderMetaTileEntities;
import com.hepdd.easytech.loaders.preload.ETHLoaderRecipe;
import com.hepdd.easytech.loaders.preload.ETHStatics;
import com.hepdd.easytech.proxy.CommonProxy;
import com.hepdd.easytech.proxy.GuiHandler;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;

@Mod(
    modid = EasyTechnology.MODID,
    version = Tags.VERSION,
    name = "EasyTechnology",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:gtnhlib;required-after:gregtech;required-after:TConstruct;required-after:Baubles|Expanded;after:ExtraUtilities")
public class EasyTechnology {

    public static final String MODID = "easytech";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.Instance("easytech")
    public static EasyTechnology instance;

    @SidedProxy(
        clientSide = "com.hepdd.easytech.proxy.ClientProxy",
        serverSide = "com.hepdd.easytech.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GTChunkManagerEx.init();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        GuiHandler.init();
        new ETHLoaderMetaTileEntities().run();
        new ETHLoaderItem().run();
        new ETHLoaderRecipe().run();
        ETHStatics.NW = new ETHNetwork();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        GTChunkManagerEx.onServerStopped();
    }

    @Mod.EventHandler
    public void loadComplated(FMLLoadCompleteEvent event) {
        proxy.loadComplate(event);
    }
}
