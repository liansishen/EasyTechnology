package com.hepdd.easytech.proxy;

import com.hepdd.easytech.loaders.preload.ETHLoaderKeybind;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Override
    public void init(FMLInitializationEvent event) {

        new ETHLoaderKeybind().run();
        super.init(event);
    }
}
