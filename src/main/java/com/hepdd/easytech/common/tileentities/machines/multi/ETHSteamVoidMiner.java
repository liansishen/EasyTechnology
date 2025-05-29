package com.hepdd.easytech.common.tileentities.machines.multi;

import static gregtech.api.enums.HatchElement.*;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.hepdd.easytech.api.metatileentity.implementations.base.ETHVoidMinerBase;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.util.GTUtility;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gtPlusPlus.core.util.minecraft.FluidUtils;

public class ETHSteamVoidMiner extends ETHVoidMinerBase {

    private int totalSteam = 0;

    public ETHSteamVoidMiner(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 1);
        MTEMultiBlockBase.disableMaintenance = true;
        if (!shouldCheckMaintenance()) fixAllIssues();
    }

    public ETHSteamVoidMiner(String aName) {
        super(aName, 1);
        MTEMultiBlockBase.disableMaintenance = true;
        if (!shouldCheckMaintenance()) fixAllIssues();
    }

    @Override
    public Materials getFrameMaterial() {
        return Materials.Bronze;
    }

    @Override
    protected ItemList getCasingBlockItem() {
        return ItemList.Casing_BronzePlatedBricks;
    }

    public Block getBlock() {
        return GregTechAPI.sBlockCasings1;
    }

    public int getMeta() {
        return 10;
    }

    @Override
    public void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
        super.drawTexts(screenElements, inventorySlot);
        screenElements.widget(
            TextWidget.dynamicString(() -> "剩余蒸汽: " + this.totalSteam + " mB")
                .setSynced(true)
                .setTextAlignment(Alignment.CenterLeft)
                .setDefaultColor(COLOR_TEXT_WHITE.get())
                .setEnabled(true));
    }

    @Override
    public void setElectricityStats() {
        this.mOutputItems = new ItemStack[0];
        this.mProgresstime = 0;
        this.mMaxProgresstime = 20; // 1s
        this.mEUt = -2; // 20L Steam/t
        this.multiplier = 2;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        getTotalSteamStored();
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        if (mEUt < 0) {
            long aSteamVal = ((-mEUt * 10000L) / Math.max(1000, mEfficiency));
            // Logger.INFO("Trying to drain "+aSteamVal+" steam per tick.");
            if (!tryConsumeSteam((int) aSteamVal)) {
                stopMachine(ShutDownReasonRegistry.POWER_LOSS);
                return false;
            }
        }
        return true;
    }

    public ArrayList<FluidStack> getAllSteamStacks() {
        ArrayList<FluidStack> aFluids = new ArrayList<>();
        FluidStack aSteam = FluidUtils.getSteam(1);
        for (FluidStack aFluid : this.getStoredFluids()) {
            if (aFluid.isFluidEqual(aSteam)) {
                aFluids.add(aFluid);
            }
        }
        return aFluids;
    }

    public int getTotalSteamStored() {
        int aSteam = 0;
        for (FluidStack aFluid : getAllSteamStacks()) {
            aSteam += aFluid.amount;
        }
        this.totalSteam = aSteam;
        return aSteam;
    }

    public boolean tryConsumeSteam(int aAmount) {
        if (getTotalSteamStored() <= 0) {
            return false;
        } else {
            return this.depleteInput(FluidUtils.getSteam(aAmount));
        }
    }

    @Override
    public int getCasingTextureIndex() {
        return GTUtility.getCasingTextureIndex(getBlock(), getMeta());
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHSteamVoidMiner(mName);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("mSteamAmount", this.totalSteam);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        this.totalSteam = aNBT.getInteger("mSteamAmount");
    }

    @Override
    protected List<IHatchElement<? super ETHVoidMinerBase>> getAllowedHatches() {
        return ImmutableList.of(InputBus, OutputBus, InputHatch);
    }

    @Override
    public String[] getToolTips() {
        String[] lines = new String[7];
        lines[0] = "20L steam/t"; // consume
        lines[1] = "2 Raw Ores"; // output items
        lines[2] = "1 Second"; // cost time
        lines[3] = ""; // energy hatch
        lines[4] = ""; // maintenance hatch
        lines[5] = "Ores, optional, any base casing"; // input bus
        lines[6] = "Steam, any base casing"; // input hatch
        return lines;
    }
}
