package com.hepdd.easytech.api.metatileentity.implementations.base;

import static gregtech.api.enums.GTValues.V;

import java.util.ArrayList;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;

import gregtech.api.enums.Materials;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.GTPPMultiBlockBase;

public abstract class ETHFuelMultiBase<T extends ETHFuelMultiBase<T>> extends GTPPMultiBlockBase<T> {

    private int intBurnTime = 0;

    public ETHFuelMultiBase(String aName) {
        super(aName);
        MTEMultiBlockBase.disableMaintenance = true;
        if (!shouldCheckMaintenance()) fixAllIssues();
    }

    public ETHFuelMultiBase(int aID, String aName, String aNameRegional) {
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
    public abstract IStructureDefinition getStructureDefinition();

    @Override
    protected abstract MultiblockTooltipBuilder createTooltip();

    @Override
    public abstract boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack);

    @Override
    public abstract IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity);

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        if (mEUt < 0) {
            if (!tryConsumeFuel(1)) {
                stopMachine(ShutDownReasonRegistry.POWER_LOSS);
                return false;
            }
        }
        return true;
    }

    private boolean tryConsumeFuel(int aFuelVal) {
        if (this.intBurnTime - aFuelVal < 0) {
            int addedFuel = tryAddFuelValue();
            if (addedFuel <= 0) return false;
            this.intBurnTime += addedFuel;
            return tryConsumeFuel(aFuelVal);
        } else {
            this.intBurnTime -= aFuelVal;
        }
        return true;
    }

    private int tryAddFuelValue() {
        ArrayList<ItemStack> inputs = getStoredInputs();
        for (ItemStack itemStack : inputs) {
            String lowerCaseBlockName = Block.getBlockFromItem(itemStack.getItem())
                .getUnlocalizedName()
                .toLowerCase();
            if (couldFuel(itemStack, lowerCaseBlockName)) {
                int burnTime = TileEntityFurnace.getItemBurnTime(itemStack);
                ItemStack fuel = ItemStack.copyItemStack(itemStack);
                fuel.stackSize = 1;
                depleteInput(fuel);
                return burnTime;
            }
        }
        return 0;
    }

    private static boolean couldFuel(ItemStack fuel, String lowerCaseBlockName) {
        return GTUtility.isPartOfMaterials(fuel, Materials.Coal) || GTUtility.isPartOfMaterials(fuel, Materials.Lignite)
            || lowerCaseBlockName.matches("tile\\..+compressedcoal")
            || GTUtility.isPartOfMaterials(fuel, Materials.Charcoal)
            || (Stream.of("^tile\\..+charcoal", "^tile\\..+coke", "^tile\\..+railcraft.cube")
                .anyMatch(lowerCaseBlockName::matches))
            || Stream.of("fuelCoke", "fuelCactusCharcoal", "fuelCactusCoke", "fuelSugarCharcoal", "fuelSugarCoke")
                .anyMatch(name -> GTOreDictUnificator.isItemStackInstanceOf(fuel, name));
    }
}
