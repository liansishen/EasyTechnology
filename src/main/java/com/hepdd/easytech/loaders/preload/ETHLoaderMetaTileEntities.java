package com.hepdd.easytech.loaders.preload;

import com.hepdd.easytech.api.enums.ETHItemList;
import com.hepdd.easytech.api.metatileentity.implementations.ETHPrimitiveHatchInput;
import com.hepdd.easytech.api.metatileentity.implementations.ETHPrimitiveHatchInputBus;
import com.hepdd.easytech.api.metatileentity.implementations.ETHPrimitiveHatchOutput;
import com.hepdd.easytech.api.metatileentity.implementations.ETHPrimitiveHatchOutputBus;
import com.hepdd.easytech.common.tileentities.machines.multi.ETHElectricVoidMiners;
import com.hepdd.easytech.common.tileentities.machines.multi.ETHLargeBlastFurnace;
import com.hepdd.easytech.common.tileentities.machines.multi.ETHLargeCokeOven;
import com.hepdd.easytech.common.tileentities.machines.multi.ETHPrimitiveVoidMiner;
import com.hepdd.easytech.common.tileentities.machines.multi.ETHSteamVoidMiner;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Textures;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;

public class ETHLoaderMetaTileEntities implements Runnable {

    public static final byte ETHMachineTexturePage = 21;

    @Override
    public void run() {
        registerMultiblockControllers();
        registerHatch();
        registerTexturePage();
    }

    private static void registerTexturePage() {
        GTUtility.addTexturePage(ETHMachineTexturePage);
        // [21][0]=2688

        // Coke Oven 2688~2690
        Textures.BlockIcons.casingTexturePages[ETHMachineTexturePage][0] = TextureFactory
            .of(new Textures.BlockIcons.CustomIcon("iconsets/MACHINE_CASING_COKEOVEN"));
        Textures.BlockIcons.casingTexturePages[ETHMachineTexturePage][1] = TextureFactory
            .of(new Textures.BlockIcons.CustomIcon("iconsets/MACHINE_CASING_COKEOVEN_INACTIVE"));
        Textures.BlockIcons.casingTexturePages[ETHMachineTexturePage][2] = TextureFactory
            .of(new Textures.BlockIcons.CustomIcon("iconsets/MACHINE_CASING_COKEOVEN_ACTIVE"));

        // Blast Furnace 2691~2694
        Textures.BlockIcons.casingTexturePages[ETHMachineTexturePage][3] = TextureFactory
            .of(Textures.BlockIcons.MACHINE_CASING_DENSEBRICKS);
        Textures.BlockIcons.casingTexturePages[ETHMachineTexturePage][4] = TextureFactory
            .of(Textures.BlockIcons.MACHINE_CASING_BRICKEDBLASTFURNACE_INACTIVE);
        Textures.BlockIcons.casingTexturePages[ETHMachineTexturePage][5] = TextureFactory
            .of(Textures.BlockIcons.MACHINE_CASING_BRICKEDBLASTFURNACE_ACTIVE);
        Textures.BlockIcons.casingTexturePages[ETHMachineTexturePage][6] = TextureFactory.builder()
            .addIcon(Textures.BlockIcons.MACHINE_CASING_BRICKEDBLASTFURNACE_ACTIVE_GLOW)
            .glow()
            .build();

        // Void Miner 2695~
        Textures.BlockIcons.casingTexturePages[ETHMachineTexturePage][7] = TextureFactory
            .of(ItemList.Casing_Reinforced_Wood.getBlock(), 15);
    }

    private static void registerMultiblockControllers() {
        int intMachineID = 2800;
        ETHItemList.Machine_Large_Bricked_BlastFurnace.set(
            new ETHLargeBlastFurnace(
                intMachineID++,
                "multimachine.largebrickedblastfurnace",
                "Large Bricked Blast Furnace").getStackForm(1L));

        ETHItemList.Machine_Large_Coke_Oven.set(
            new ETHLargeCokeOven(intMachineID++, "multimachine.largecokeoven", "Large Coke Oven").getStackForm(1L));

        ETHItemList.Machine_Primitive_Void_Miner.set(
            new ETHPrimitiveVoidMiner(intMachineID++, "multimachine.primitivevoidminer", "Primitive Void Miner")
                .getStackForm(1L));

        ETHItemList.Machine_Steam_Void_Miner.set(
            new ETHSteamVoidMiner(intMachineID++, "multimachine.steamvoidminer", "Steam Void Miner").getStackForm(1L));

        ETHItemList.Machine_LV_Void_Miner.set(
            new ETHElectricVoidMiners.EVMLV(intMachineID++, "multimachine.lvvoidminer", "LV Void Miner")
                .getStackForm(1L));

        ETHItemList.Machine_HV_Void_Miner.set(
            new ETHElectricVoidMiners.EVMHV(intMachineID++, "multimachine.hvvoidminer", "HV Void Miner")
                .getStackForm(1L));

        ETHItemList.Machine_IV_Void_Miner.set(
            new ETHElectricVoidMiners.EVMIV(intMachineID++, "multimachine.ivvoidminer", "IV Void Miner")
                .getStackForm(1L));
    }

    private static void registerHatch() {
        ETHItemList.Hatch_Input_Primitive.set(
            new ETHPrimitiveHatchInput(4000, 1, "hatch.input.tier.primitive", "Primitive Input Hatch", 0)
                .getStackForm(1L));

        ETHItemList.Hatch_Output_Primitive.set(
            new ETHPrimitiveHatchOutput(4001, "hatch.output.tier.primitive", "Primitive Output Hatch", 0)
                .getStackForm(1L));

        ETHItemList.Hatch_Input_Bus_Primitive.set(
            new ETHPrimitiveHatchInputBus(4002, "hatch.input_bus.tier.primitive", "Primitive Input Bus", 0)
                .getStackForm(1L));

        ETHItemList.Hatch_Output_Bus_Primitive.set(
            new ETHPrimitiveHatchOutputBus(4003, "hatch.output_bus.tier.primitive", "Primitive Output Bus", 0)
                .getStackForm(1L));
    }
}
