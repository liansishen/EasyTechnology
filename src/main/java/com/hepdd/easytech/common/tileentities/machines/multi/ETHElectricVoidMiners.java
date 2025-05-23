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
            setMultiplier(2);
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
            setMultiplier(4);
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
            setMultiplier(8);
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
    }
}
