package com.hepdd.easytech.common.tileentities.machines.multi;

import net.minecraft.item.ItemStack;

import com.hepdd.easytech.api.metatileentity.implementations.base.ETHVoidMinerBase;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GTUtility;

public class ETHSteamVoidMiner extends ETHVoidMinerBase {

    public ETHSteamVoidMiner(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public ETHSteamVoidMiner(String aName) {
        super(aName);
    }

    @Override
    public Materials getFrameMaterial() {
        return Materials.Bronze;
    }

    @Override
    public ItemList getCasingBlockItem() {
        return ItemList.Hull_Bronze;
    }

    @Override
    public void setElectricityStats() {
        this.mOutputItems = new ItemStack[0];
        this.mProgresstime = 0;
        this.mMaxProgresstime = 20; // 1s
        this.mEUt = -80; // 80mB Steam/t
        setMultiplier(4);
    }

    @Override
    public int getCasingTextureIndex() {
        return GTUtility.getCasingTextureIndex(ItemList.Hull_Bronze.getBlock(), 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHSteamVoidMiner(mName);
    }
}
