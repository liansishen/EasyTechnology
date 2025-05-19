package com.hepdd.easytech.loaders.preload;

import com.hepdd.easytech.api.enums.ETHItemList;
import com.hepdd.easytech.api.metatileentity.implementations.ETHHatchInput;
import com.hepdd.easytech.api.metatileentity.implementations.ETHHatchInputBus;
import com.hepdd.easytech.api.metatileentity.implementations.ETHHatchOutput;
import com.hepdd.easytech.api.metatileentity.implementations.ETHHatchOutputBus;
import com.hepdd.easytech.common.tileentities.machines.multi.ETHBigCokeOven;
import com.hepdd.easytech.common.tileentities.machines.multi.ETHBrickedBlastFurnace;

public class ETHLoaderMetaTileEntities implements Runnable {

    @Override
    public void run() {
        registerMultiblockControllers();
        registerHatch();
    }

    private static void registerMultiblockControllers() {
        ETHItemList.Machine_Big_Bricked_BlastFurnace
            .set(new ETHBrickedBlastFurnace(2800, "multimachine.bigbrickedblastfurnace", "大型砖高炉").getStackForm(1L));

        ETHItemList.Machine_Big_Coke_Oven
            .set(new ETHBigCokeOven(2801, "multimachine.bigcokeoven", "大型焦炉").getStackForm(1L));
    }

    private static void registerHatch(){
        ETHItemList.Hatch_Input_Primitive
            .set(new ETHHatchInput(4000,1,"hatch.input.tier.primitive","Primitive Input Hatch",0).getStackForm(1L));

        ETHItemList.Hatch_Output_Primitive
                .set(new ETHHatchOutput(4001, "hatch.output.tier.primitive","Primitive Output Hatch",0).getStackForm(1L));

        ETHItemList.Hatch_Input_Bus_Primitive
            .set(new ETHHatchInputBus(4002,"hatch.inputbus.tier.primitive","Primitive Input Bus",0).getStackForm(1L));

        ETHItemList.Hatch_Output_Bus_Primitive
            .set(new ETHHatchOutputBus(4003,"hatch.outputbus.tier.primitive","Primitive Output Bus",0).getStackForm(1L));
    }
}
