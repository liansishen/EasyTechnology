package com.hepdd.easytech.common.tileentities.machines.multi;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import com.hepdd.easytech.api.metatileentity.implementations.base.ETHVoidMinerBase;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.util.GTUtility;

public class ETHSteamVoidMiner extends ETHVoidMinerBase {

    public ETHSteamVoidMiner(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        MTEMultiBlockBase.disableMaintenance = true;
        if (!shouldCheckMaintenance()) fixAllIssues();
    }

    public ETHSteamVoidMiner(String aName) {
        super(aName);
        MTEMultiBlockBase.disableMaintenance = true;
        if (!shouldCheckMaintenance()) fixAllIssues();
    }

    @Override
    public Materials getFrameMaterial() {
        return Materials.Bronze;
    }

    @Override
    public Block getBlock() {
        return GregTechAPI.sBlockCasings1;
    }

    @Override
    public int getMeta() {
        return 10;
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
        return GTUtility.getCasingTextureIndex(getBlock(), getMeta());
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHSteamVoidMiner(mName);
    }
}
