package com.hepdd.easytech.common.tileentities.machines.multi;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import com.hepdd.easytech.api.metatileentity.implementations.base.ETHVoidMinerBase;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import gregtech.api.util.GTUtility;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;

public class ETHElectricVoidMiners {

    public static class EVMLV extends ETHVoidMinerBase {

        public EVMLV(int aID, String aName, String aNameRegional) {
            super(aID, aName, aNameRegional, 2);
        }

        public EVMLV(String aName) {
            super(aName, 2);
        }

        @Override
        public Materials getFrameMaterial() {
            return Materials.Steel;
        }

        @Override
        public ItemList getCasingBlockItem() {
            return ItemList.Casing_SolidSteel;
        }

        @Override
        public void setElectricityStats() {
            this.mOutputItems = new ItemStack[0];
            this.mProgresstime = 0;
            this.mMaxProgresstime = 10; // 0.5s
            this.mEUt = -32; // 32eu/t
            this.multiplier = 1;
            setOreType(1); // OreBlock
        }

        @Override
        public int getCasingTextureIndex() {
            return 16;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
            return new EVMLV(mName);
        }

        @Override
        public boolean onRunningTick(ItemStack aStack) {
            if (mEUt < 0) {
                if (!isEnergyEnough()) {
                    stopMachine(ShutDownReasonRegistry.POWER_LOSS);
                    return false;
                }
            }
            return true;
        }

        private boolean isEnergyEnough() {
            long requiredEnergy = this.mEUt;
            for (MTEHatchEnergy energyHatch : mEnergyHatches) {
                requiredEnergy -= energyHatch.getEUVar();
                if (requiredEnergy <= 0) return true;
            }
            return false;
        }

        @Override
        public String[] getToolTips() {
            String[] lines = new String[7];
            lines[0] = "32 EU/t"; // consume
            lines[1] = "1 Ores"; // output items
            lines[2] = "0.5 Second"; // cost time
            lines[3] = "LV+, any base casing"; // energy hatch
            lines[4] = "Any base casing"; // maintenance hatch
            lines[5] = "Ores, optional, any base casing"; // input bus
            lines[6] = ""; // input hatch
            return lines;
        }
    }

    public static class EVMHV extends ETHVoidMinerBase {

        public EVMHV(int aID, String aName, String aNameRegional) {
            super(aID, aName, aNameRegional, 3);
        }

        public EVMHV(String aName) {
            super(aName, 3);
        }

        @Override
        public Materials getFrameMaterial() {
            return Materials.StainlessSteel;
        }

        @Override
        public ItemList getCasingBlockItem() {
            return ItemList.Casing_CleanStainlessSteel;
        }

        public Block getBlock() {
            return GregTechAPI.sBlockCasings4;
        }

        public int getMeta() {
            return 1;
        }

        @Override
        public void setElectricityStats() {
            this.mOutputItems = new ItemStack[0];
            this.mProgresstime = 0;
            this.mMaxProgresstime = 10; // 0.5s
            this.mEUt = -512; // 512eu/t
            this.multiplier = 2;
            setOreType(1); // OreBlock
        }

        @Override
        public boolean onRunningTick(ItemStack aStack) {
            if (mEUt < 0) {
                if (!isEnergyEnough()) {
                    stopMachine(ShutDownReasonRegistry.POWER_LOSS);
                    return false;
                }
            }
            return true;
        }

        private boolean isEnergyEnough() {
            long requiredEnergy = this.mEUt;
            for (MTEHatchEnergy energyHatch : mEnergyHatches) {
                requiredEnergy -= energyHatch.getEUVar();
                if (requiredEnergy <= 0) return true;
            }
            return false;
        }

        @Override
        public int getCasingTextureIndex() {
            return GTUtility.getCasingTextureIndex(getBlock(), getMeta());
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
            return new EVMHV(mName);
        }

        @Override
        public String[] getToolTips() {
            String[] lines = new String[7];
            lines[0] = "512 EU/t"; // consume
            lines[1] = "2 Ores"; // output items
            lines[2] = "0.5 Second"; // cost time
            lines[3] = "HV+, any base casing"; // energy hatch
            lines[4] = "Any base casing"; // maintenance hatch
            lines[5] = "Ores, optional, any base casing"; // input bus
            lines[6] = ""; // input hatch
            return lines;
        }
    }

    public static class EVMIV extends ETHVoidMinerBase {

        public EVMIV(int aID, String aName, String aNameRegional) {
            super(aID, aName, aNameRegional, 4);
        }

        public EVMIV(String aName) {
            super(aName, 4);
        }

        @Override
        public Materials getFrameMaterial() {
            return Materials.TungstenSteel;
        }

        @Override
        protected ItemList getCasingBlockItem() {
            return ItemList.Casing_RobustTungstenSteel;
        }

        public Block getBlock() {
            return GregTechAPI.sBlockCasings4;
        }

        public int getMeta() {
            return 0;
        }

        @Override
        public void setElectricityStats() {
            this.mOutputItems = new ItemStack[0];
            this.mProgresstime = 0;
            this.mMaxProgresstime = 10; // 0.5s
            this.mEUt = -8192; // 8192eu/t
            this.multiplier = 4;
            setOreType(1); // OreBlock
        }

        @Override
        public boolean onRunningTick(ItemStack aStack) {
            if (mEUt < 0) {
                if (!isEnergyEnough()) {
                    stopMachine(ShutDownReasonRegistry.POWER_LOSS);
                    return false;
                }
            }
            return true;
        }

        private boolean isEnergyEnough() {
            long requiredEnergy = this.mEUt;
            for (MTEHatchEnergy energyHatch : mEnergyHatches) {
                requiredEnergy -= energyHatch.getEUVar();
                if (requiredEnergy <= 0) return true;
            }
            return false;
        }

        @Override
        public int getCasingTextureIndex() {
            return GTUtility.getCasingTextureIndex(getBlock(), getMeta());
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
            return new EVMIV(mName);
        }

        @Override
        public String[] getToolTips() {
            String[] lines = new String[7];
            lines[0] = "8192 EU/t"; // consume
            lines[1] = "4 Ores"; // output items
            lines[2] = "0.5 Second"; // cost time
            lines[3] = "IV+, any base casing"; // energy hatch
            lines[4] = "Any base casing"; // maintenance hatch
            lines[5] = "Ores, optional, any base casing"; // input bus
            lines[6] = ""; // input hatch
            return lines;
        }
    }
}
