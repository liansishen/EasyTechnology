package com.hepdd.easytech.loaders.preload;

import com.hepdd.easytech.api.enums.ETHItemList;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHBronzeEntangledMiner;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHElectricEntangledMiner;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHPrimitiveEntangledMiner;

public class ETHLoaderMetaTileEntities implements Runnable {

    @Override
    public void run() {
        int id = 2800;

        ETHItemList.Machine_Primitive_Entangled_Miner.set(
            new ETHPrimitiveEntangledMiner(id++, "basic.primitiveentangledminer", "Primitive Entangled Miner")
                .getStackForm(1L));

        ETHItemList.Machine_Bronze_Entangled_Miner.set(
            new ETHBronzeEntangledMiner(id++, "basic.bronzeentangledminer", "Bronze Entangled Miner").getStackForm(1L));

        ETHItemList.Machine_LV_Entangled_Miner.set(
            new ETHElectricEntangledMiner(id++, "basic.lventangledminer", "LV Entangled Miner", 1).getStackForm(1L));

        ETHItemList.Machine_MV_Entangled_Miner.set(
            new ETHElectricEntangledMiner(id++, "basic.mventangledminer", "MV Entangled Miner", 2).getStackForm(1L));

        ETHItemList.Machine_HV_Entangled_Miner.set(
            new ETHElectricEntangledMiner(id++, "basic.hventangledminer", "HV Entangled Miner", 3).getStackForm(1L));
    }
}
