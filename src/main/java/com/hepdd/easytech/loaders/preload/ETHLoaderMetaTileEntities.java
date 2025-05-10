package com.hepdd.easytech.loaders.preload;

import com.hepdd.easytech.api.enums.ETHItemList;
import com.hepdd.easytech.common.tileentities.machines.multi.ETHBrickedBlastFurnace;

public class ETHLoaderMetaTileEntities implements Runnable {

    @Override
    public void run() {
        registerMultiblockControllers();
    }

    private static void registerMultiblockControllers() {
        ETHItemList.Machine_Super_Bricked_BlastFurnace
            .set(new ETHBrickedBlastFurnace(2800, "multimachine.superbrickedblastfurnace", "大型砖高炉").getStackForm(1L));
    }
}
