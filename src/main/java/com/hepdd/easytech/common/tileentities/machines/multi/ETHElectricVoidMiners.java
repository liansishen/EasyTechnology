package com.hepdd.easytech.common.tileentities.machines.multi;

import com.hepdd.easytech.api.metatileentity.implementations.base.ETHVoidMinerBase;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;

public class ETHElectricVoidMiners {

    public static class EVMLV extends ETHVoidMinerBase {

        public EVMLV(int aID, String aName, String aNameRegional) {
            super(aID, aName, aNameRegional);
        }

        public EVMLV(String aName) {
            super(aName);
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
            this.mEUt = -32;    //32eu/t
            setMultiplier(2);
            setOreType(1);  // OreBlock
        }

        @Override
        public int getCasingTextureIndex() {
            return 16;
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
            return new EVMLV(mName);
        }
    }

    public static class EVMHV extends ETHVoidMinerBase {

        public EVMHV(int aID, String aName, String aNameRegional) {
            super(aID, aName, aNameRegional);
        }

        public EVMHV(String aName) {
            super(aName);
        }

        @Override
        public Materials getFrameMaterial() {
            return Materials.StainlessSteel;
        }

        @Override
        public ItemList getCasingBlockItem() {
            return ItemList.Casing_CleanStainlessSteel;
        }

        @Override
        public void setElectricityStats() {
            this.mOutputItems = new ItemStack[0];
            this.mProgresstime = 0;
            this.mMaxProgresstime = 10; // 0.5s
            this.mEUt = -512;    // 512eu/t
            setMultiplier(4);
            setOreType(1);  // OreBlock
        }

        @Override
        public int getCasingTextureIndex() {
            return GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings4, 1);
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
            return new EVMHV(mName);
        }
    }

    public static class EVMIV extends ETHVoidMinerBase {

        public EVMIV(int aID, String aName, String aNameRegional) {
            super(aID, aName, aNameRegional);
        }

        public EVMIV(String aName) {
            super(aName);
        }

        @Override
        public Materials getFrameMaterial() {
            return Materials.TungstenSteel;
        }

        @Override
        public ItemList getCasingBlockItem() {
            return ItemList.Casing_RobustTungstenSteel;
        }

        @Override
        public void setElectricityStats() {
            this.mOutputItems = new ItemStack[0];
            this.mProgresstime = 0;
            this.mMaxProgresstime = 10; // 0.5s
            this.mEUt = -8192;    // 8192eu/t
            setMultiplier(8);
            setOreType(1);  // OreBlock
        }

        @Override
        public int getCasingTextureIndex() {
            return GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings4, 0);
        }

        @Override
        public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
            return new EVMIV(mName);
        }
    }
}
