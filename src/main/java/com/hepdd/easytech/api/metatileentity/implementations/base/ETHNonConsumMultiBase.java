package com.hepdd.easytech.api.metatileentity.implementations.base;

import static gregtech.api.enums.GTValues.V;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.util.MultiblockTooltipBuilder;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.GTPPMultiBlockBase;

public abstract class ETHNonConsumMultiBase<T extends ETHNonConsumMultiBase<T>> extends GTPPMultiBlockBase<T> {

    public ETHNonConsumMultiBase(String aName) {
        super(aName);
        MTEMultiBlockBase.disableMaintenance = true;
        if (!shouldCheckMaintenance()) fixAllIssues();
    }

    public ETHNonConsumMultiBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        MTEMultiBlockBase.disableMaintenance = true;
        if (!shouldCheckMaintenance()) fixAllIssues();
    }

    @Override
    public ITexture[] getTexture(final IGregTechTileEntity aBaseMetaTileEntity, final ForgeDirection side,
        final ForgeDirection facing, final int aColorIndex, final boolean aActive, final boolean aRedstone) {
        if (side == facing) {
            return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(getCasingTextureIndex()),
                aActive ? getFrontOverlayActive() : getFrontOverlay() };
        }
        return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(getCasingTextureIndex()) };
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic().setMaxParallelSupplier(this::getMaxParallelRecipes);
    }

    @Override
    protected void setProcessingLogicPower(ProcessingLogic logic) {
        logic.setAvailableVoltage(V[0]);
        // We need to trick the GT_ParallelHelper we have enough amps for all recipe parallels.
        logic.setAvailableAmperage(getMaxParallelRecipes());
        logic.setAmperageOC(false);
    }

    @Override
    public int getMaxEfficiency(ItemStack arg0) {
        return 0;
    }

    protected abstract int getCasingTextureIndex();

    protected abstract ITexture getFrontOverlay();

    protected abstract ITexture getFrontOverlayActive();

    @Override
    public abstract String getMachineType();

    @Override
    public abstract int getMaxParallelRecipes();

    @Override
    public abstract void construct(ItemStack stackSize, boolean hintsOnly);

    @Override
    public abstract IStructureDefinition<T> getStructureDefinition();

    @Override
    protected abstract MultiblockTooltipBuilder createTooltip();

    @Override
    public abstract boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack);

    @Override
    public abstract IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity);

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        return true;
    }
}
