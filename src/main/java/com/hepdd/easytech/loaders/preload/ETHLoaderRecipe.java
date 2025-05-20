package com.hepdd.easytech.loaders.preload;

import static com.hepdd.easytech.api.enums.ETHRecipeMaps.largeCokeOvenRecipe;
import static gregtech.api.enums.Mods.Railcraft;
import static gregtech.api.util.GTRecipeConstants.ADDITIVE_AMOUNT;
import static mods.railcraft.common.blocks.machine.alpha.EnumMachineAlpha.COKE_OVEN;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.hepdd.easytech.api.enums.ETHItemList;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
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
