package com.hepdd.easytech.loaders.preload;

import static com.hepdd.easytech.api.enums.ETHRecipeMaps.largeCokeOvenRecipe;
import static gregtech.api.enums.Mods.Railcraft;
import static gregtech.api.util.GTRecipeConstants.ADDITIVE_AMOUNT;
import static mods.railcraft.common.blocks.machine.alpha.EnumMachineAlpha.COKE_OVEN;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.hepdd.easytech.api.enums.ETHItemList;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import mods.railcraft.common.fluids.Fluids;

public class ETHLoaderRecipe implements Runnable {

    protected final int COKE_COOK_TIME = 20 * 90;

    @Override
    public void run() {
        registerCokeOvenRecipes();
        registerCraftRecipe();
    }

    public void registerCraftRecipe() {

        GTModHandler.addCraftingRecipe(
            ETHItemList.Machine_Large_Bricked_BlastFurnace.get(1),
            new Object[] { "AA ", "AA ", "   ", 'A', ItemList.Machine_Bricked_BlastFurnace.get(1) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Machine_Large_Coke_Oven.get(1),
            new Object[] { "AA ", "AA ", "   ", 'A', COKE_OVEN.getItem() });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Machine_Primitive_Void_Miner.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "AAA", "ABA", "CCC", 'A', OrePrefixes.frameGt.get(Materials.Wood), 'B',
                OrePrefixes.gear.get(Materials.Wood), 'C', ItemList.WoodenCasing.get(1) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Machine_Steam_Void_Miner.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "AAA", "ABA", "CCC", 'A', OrePrefixes.frameGt.get(Materials.Bronze), 'B',
                OrePrefixes.gear.get(Materials.Bronze), 'C', ItemList.Casing_BronzePlatedBricks.get(1) });

        GTModHandler.addShapelessCraftingRecipe(
            ETHItemList.Machine_LV_Void_Miner.get(1),
            new Object[] { ItemList.Machine_LV_Miner.get(1) });

        GTModHandler.addShapelessCraftingRecipe(
            ETHItemList.Machine_HV_Void_Miner.get(1),
            new Object[] { ItemList.Machine_HV_Miner.get(1) });

        GTModHandler.addShapelessCraftingRecipe(
            ETHItemList.Machine_IV_Void_Miner.get(1),
            new Object[] { ItemList.OreDrill1.get(1) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Hatch_Input_Bus_Primitive.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "ABA", "CDC", "ABA", 'A', OrePrefixes.plank.get(Materials.Wood), 'B',
                OrePrefixes.plate.get(Materials.Iron), 'C', OrePrefixes.plate.get(Materials.Tin), 'D',
                new ItemStack(Blocks.chest, 1) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Hatch_Output_Bus_Primitive.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "ACA", "BDB", "ACA", 'A', OrePrefixes.plank.get(Materials.Wood), 'B',
                OrePrefixes.plate.get(Materials.Iron), 'C', OrePrefixes.plate.get(Materials.Tin), 'D',
                new ItemStack(Blocks.chest, 1) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Hatch_Input_Primitive.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "ABA", "CDC", "ABA", 'A', OrePrefixes.plank.get(Materials.Wood), 'B',
                OrePrefixes.plate.get(Materials.Iron), 'C', OrePrefixes.plate.get(Materials.Tin), 'D',
                new ItemStack(Blocks.glass, 1) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.Hatch_Output_Primitive.get(1),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "ACA", "BDB", "ACA", 'A', OrePrefixes.plank.get(Materials.Wood), 'B',
                OrePrefixes.plate.get(Materials.Iron), 'C', OrePrefixes.plate.get(Materials.Tin), 'D',
                new ItemStack(Blocks.glass, 1) });

        GTModHandler.addCraftingRecipe(
            ETHItemList.ITEM_Void_Oil_Location_Card.getInternalStack_unsafe(),
            GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.DO_NOT_CHECK_FOR_COLLISIONS
                | GTModHandler.RecipeBits.BUFFERED,
            new Object[] { "ABA", "BCB", "ABA", 'A', new ItemStack(Items.redstone, 1), 'B',
                OrePrefixes.plate.get(Materials.Bronze), 'C', OrePrefixes.plate.get(Materials.Iron) });

    }

    public void registerCokeOvenRecipes() {
        GTValues.RA.stdBuilder()
            .eut(0)
            .itemInputs(GTOreDictUnificator.get("coal", null, 1))
            .itemOutputs(GTOreDictUnificator.get("fuelCoke", null, 1))
            .fluidOutputs(Fluids.CREOSOTE.get(500))
            .duration(COKE_COOK_TIME)
            .metadata(ADDITIVE_AMOUNT, 4)
            .addTo(largeCokeOvenRecipe);

        GTValues.RA.stdBuilder()
            .eut(0)
            .itemInputs(GTOreDictUnificator.get("blockCoal", null, 1))
            .itemOutputs(GTModHandler.getModItem(Railcraft.ID, "cube", 1, 0))
            .fluidOutputs(Fluids.CREOSOTE.get(4500))
            .duration(9 * COKE_COOK_TIME)
            .metadata(ADDITIVE_AMOUNT, 4)
            .addTo(largeCokeOvenRecipe);

        GTValues.RA.stdBuilder()
            .eut(0)
            .itemInputs(GTOreDictUnificator.get("logWood", null, 1))
            .itemOutputs(new ItemStack(Items.coal, 1, 1))
            .fluidOutputs(Fluids.CREOSOTE.get(250))
            .duration(COKE_COOK_TIME)
            .metadata(ADDITIVE_AMOUNT, 4)
            .addTo(largeCokeOvenRecipe);

        GTValues.RA.stdBuilder()
            .eut(0)
            .itemInputs(GTOreDictUnificator.get("sugarcane", null, 1))
            .itemOutputs(GTOreDictUnificator.get("itemCharcoalSugar", null, 1))
            .fluidOutputs(Fluids.CREOSOTE.get(30))
            .duration(COKE_COOK_TIME / 3)
            .metadata(ADDITIVE_AMOUNT, 4)
            .addTo(largeCokeOvenRecipe);

        GTValues.RA.stdBuilder()
            .eut(0)
            .itemInputs(GTOreDictUnificator.get("itemCharcoalSugar", null, 1))
            .itemOutputs(GTOreDictUnificator.get("itemCokeSugar", null, 1))
            .fluidOutputs(Fluids.CREOSOTE.get(30))
            .duration(COKE_COOK_TIME / 3)
            .metadata(ADDITIVE_AMOUNT, 4)
            .addTo(largeCokeOvenRecipe);

        GTValues.RA.stdBuilder()
            .eut(0)
            .itemInputs(GTOreDictUnificator.get("blockCactus", null, 1))
            .itemOutputs(GTOreDictUnificator.get("itemCharcoalCactus", null, 1))
            .fluidOutputs(Fluids.CREOSOTE.get(30))
            .duration(COKE_COOK_TIME / 3)
            .metadata(ADDITIVE_AMOUNT, 4)
            .addTo(largeCokeOvenRecipe);

        GTValues.RA.stdBuilder()
            .eut(0)
            .itemInputs(GTOreDictUnificator.get("itemCharcoalCactus", null, 1))
            .itemOutputs(GTOreDictUnificator.get("itemCokeCactus", null, 1))
            .fluidOutputs(Fluids.CREOSOTE.get(30))
            .duration(COKE_COOK_TIME / 3)
            .metadata(ADDITIVE_AMOUNT, 4)
            .addTo(largeCokeOvenRecipe);
    }
}
