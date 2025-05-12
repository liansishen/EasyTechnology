package com.hepdd.easytech.loaders.preload;

import com.hepdd.easytech.api.enums.ETHItemList;

import gregtech.api.enums.ItemList;
import gregtech.api.util.GTModHandler;

public class ETHLoaderRecipe implements Runnable {

    @Override
    public void run() {
        GTModHandler.addCraftingRecipe(
            ETHItemList.Machine_Big_Bricked_BlastFurnace.get(1),
            new Object[] { "AA ", "AA ", "   ", 'A', ItemList.Machine_Bricked_BlastFurnace.get(1) });
    }
}
