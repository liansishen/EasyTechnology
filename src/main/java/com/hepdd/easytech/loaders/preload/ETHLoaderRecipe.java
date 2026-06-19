package com.hepdd.easytech.loaders.preload;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.hepdd.easytech.api.enums.ETHItemList;

import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;
import tconstruct.tools.TinkerTools;

public class ETHLoaderRecipe implements Runnable {

    @Override
    public void run() {
        registerCraftRecipe();
    }

    public void registerCraftRecipe() {

        GTModHandler.addCraftingRecipe(
            ETHItemList.ITEM_Void_Oil_Location_Card.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "ABA", "BCB", "ABA", 'A', new ItemStack(Items.redstone, 1), 'B',
                OrePrefixes.plate.get(Materials.Bronze), 'C', OrePrefixes.plate.get(Materials.Iron) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.ITEM_Portable_Crafting_Station.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { " A", "B ", 'A', new ItemStack(TinkerTools.craftingStationWood, 1), 'B',
                new ItemStack(Items.stick, 1) });
    }

}
