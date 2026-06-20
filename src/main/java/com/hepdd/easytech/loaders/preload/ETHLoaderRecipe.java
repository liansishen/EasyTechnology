package com.hepdd.easytech.loaders.preload;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.hepdd.easytech.api.enums.ETHItemList;

import gregtech.api.enums.ItemList;
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

        GTModHandler.addCraftingRecipe(
            ETHItemList.ITEM_Entangled_Card.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "FSF", "SPS", "FSF", 'F', new ItemStack(Items.flint, 1), 'S', new ItemStack(Items.stick, 1),
                'P', new ItemStack(Items.paper, 1) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Machine_Primitive_Entangled_Miner.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "BBB", "PFH", "BBB", 'B', ItemList.Firebrick.get(1L), 'P',
                new ItemStack(TinkerTools.pickaxeHead, 1, 3), 'F', new ItemStack(Blocks.furnace, 1), 'H',
                new ItemStack(TinkerTools.hammerHead, 1, 3) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Machine_Bronze_Entangled_Miner.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "PPP", "ABC", "PPP", 'P', OrePrefixes.pipeSmall.get(Materials.Bronze), 'A',
                new ItemStack(TinkerTools.pickaxeHead, 1, 14), 'B', ItemList.Hull_Bronze_Bricks.get(1L), 'C',
                new ItemStack(TinkerTools.hammerHead, 1, 14) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Machine_LV_Entangled_Miner.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "CSC", "WMW", "EEE", 'C', OrePrefixes.circuit.get(Materials.LV), 'S',
                ItemList.Sensor_LV.get(1L), 'W', OrePrefixes.cableGt01.get(Materials.Tin), 'M',
                ItemList.Hull_LV.get(1L), 'E', ItemList.Electric_Motor_LV.get(1L) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Machine_MV_Entangled_Miner.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "CSC", "WMW", "PEP", 'C', OrePrefixes.circuit.get(Materials.MV), 'S',
                ItemList.Sensor_MV.get(1L), 'W', OrePrefixes.cableGt02.get(Materials.Copper), 'M',
                ItemList.Hull_MV.get(1L), 'P', ItemList.Electric_Piston_MV.get(1L), 'E',
                ItemList.Electric_Motor_MV.get(1L) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Machine_HV_Entangled_Miner.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "CSC", "WMW", "RPR", 'C', OrePrefixes.circuit.get(Materials.HV), 'S',
                ItemList.Sensor_HV.get(1L), 'W', OrePrefixes.cableGt04.get(Materials.Gold), 'M',
                ItemList.Hull_HV.get(1L), 'R', ItemList.Robot_Arm_HV.get(1L), 'P', ItemList.Electric_Piston_HV.get(1L),
                'E', ItemList.Electric_Motor_HV.get(1L) });
    }

}
