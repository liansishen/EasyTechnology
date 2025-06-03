package com.hepdd.easytech.loaders.preload;

import com.hepdd.easytech.api.enums.ETHItemList;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHPortableCraftingStation;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHVoidOilLocationCard;

public class ETHLoaderItem implements Runnable {

    @Override
    public void run() {
        registerItem();
    }

    private void registerItem() {
        ETHItemList.ITEM_Void_Oil_Location_Card
            .set(new ETHVoidOilLocationCard("item.voidoillocationcard", "Void Oil Location Card", "test"));

        ETHItemList.ITEM_Portable_Crafting_Station.set(
            new ETHPortableCraftingStation("item.portablecraftingstation", "Portable Crafting Station", "test222"));
    }
}
