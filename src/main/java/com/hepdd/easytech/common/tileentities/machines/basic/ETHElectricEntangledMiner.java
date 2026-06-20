package com.hepdd.easytech.common.tileentities.machines.basic;

import net.minecraft.util.StatCollector;

import gregtech.api.gui.modularui.GUITextureSet;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.BasicUIProperties;

@IMetaTileEntity.SkipGenerateDescription
public class ETHElectricEntangledMiner extends ETHAbstractEntangledMiner {

    public ETHElectricEntangledMiner(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, aTier + 1, aTier + 1, 1, 2);
    }

    public ETHElectricEntangledMiner(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures, aTier + 1, aTier, 1, 2);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aBaseTileEntity) {
        return new ETHElectricEntangledMiner(mName, mTier, mDescriptionArray, mTextures);
    }

    // ==================== Energy (EU) ====================

    @Override
    public boolean isEnetInput() {
        return true;
    }

    @Override
    public long maxEUStore() {
        return Math.max(4096, gregtech.api.enums.GTValues.V[mTier] * 64);
    }

    @Override
    protected boolean hasEnoughEnergy(IGregTechTileEntity aBaseMetaTileEntity, int aRequiredEU) {
        return aBaseMetaTileEntity.isUniversalEnergyStored(aRequiredEU);
    }

    @Override
    protected void consumeEnergy(IGregTechTileEntity aBaseMetaTileEntity, int aEU) {
        aBaseMetaTileEntity.decreaseStoredEnergyUnits(aEU, true);
    }

    @Override
    protected long getAvailableEnergy(IGregTechTileEntity aBaseMetaTileEntity) {
        return aBaseMetaTileEntity.getUniversalEnergyStored();
    }

    @Override
    protected String getEnergyDisplayString() {
        return StatCollector.translateToLocalFormatted(
            "easytech.machine.entangled_miner.energy",
            getBaseMetaTileEntity().getUniversalEnergyStored(),
            maxEUStore());
    }

    // ==================== GUI ====================

    @Override
    public GUITextureSet getGUITextureSet() {
        return GUITextureSet.DEFAULT;
    }

    @Override
    protected BasicUIProperties getUIProperties() {
        return super.getUIProperties().toBuilder()
            .build();
    }
}
