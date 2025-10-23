package com.hepdd.easytech.loaders.preload;

import com.hepdd.easytech.api.enums.ETHItemList;
import com.hepdd.easytech.common.items.ETHPlatformBuilder;
import com.hepdd.easytech.common.items.ETHPortableCraftingStation;
import com.hepdd.easytech.common.items.ETHVoidOilLocationCard;

public class ETHLoaderItem implements Runnable {

    @Override
    public void run() {
        registerItem();
    }

    private void registerItem() {
        ETHItemList.ITEM_Void_Oil_Location_Card
            .set(new ETHVoidOilLocationCard("item.voidoillocationcard", "Void Oil Location Card", "test"));

        ETHItemList.ITEM_Portable_Crafting_Station.set(
            new ETHPortableCraftingStation(
                "item.portablecraftingstation",
                "Portable Crafting Station",
                "可以方便的修武器和工具。"));

        ETHItemList.ITEM_Platform_Builder
            .set(new ETHPlatformBuilder("item.platformbuilder", "Platform Builder", "useful tool"));
    }
}
