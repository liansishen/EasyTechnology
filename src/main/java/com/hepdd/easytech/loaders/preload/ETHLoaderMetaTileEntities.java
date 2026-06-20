package com.hepdd.easytech.loaders.preload;

import com.hepdd.easytech.api.enums.ETHItemList;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHPrimitiveEntangledMiner;

public class ETHLoaderMetaTileEntities implements Runnable {

    @Override
    public void run() {
        ETHItemList.Machine_Primitive_Entangled_Miner.set(
            new ETHPrimitiveEntangledMiner(2800, "basic.primitiveentangledminer", "Primitive Entangled Miner")
                .getStackForm(1L));
    }
}
