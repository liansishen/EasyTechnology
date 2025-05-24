package com.hepdd.easytech.common.tileentities.machines.multi;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;

import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.hepdd.easytech.api.metatileentity.implementations.base.ETHVoidMinerBase;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.util.GTUtility;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;

public class ETHPrimitiveVoidMiner extends ETHVoidMinerBase {

    private int intBurnTime;

    public ETHPrimitiveVoidMiner(String aName) {
        super(aName, 0);
        MTEMultiBlockBase.disableMaintenance = true;
        if (!shouldCheckMaintenance()) fixAllIssues();
    }

    public ETHPrimitiveVoidMiner(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 0);
        MTEMultiBlockBase.disableMaintenance = true;
        if (!shouldCheckMaintenance()) fixAllIssues();
    }

    @Override
    protected ItemList getCasingBlockItem() {
        return ItemList.WoodenCasing;
    }

    public Block getBlock() {
        return GregTechAPI.sBlockCasings9;
    }

    public int getMeta() {
        return 2;
    }

    @Override
    public Materials getFrameMaterial() {
        return Materials.Wood;
    }

    @Override
    public int getCasingTextureIndex() {
        return GTUtility.getCasingTextureIndex(getBlock(), getMeta());
    }

    @Override
    public void setElectricityStats() {
        this.mOutputItems = new ItemStack[0];
        this.mProgresstime = 0;
        this.mMaxProgresstime = 40; // 2s
        this.mEUt = -1;
        setMultiplier(4);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHPrimitiveVoidMiner(mName);
    }

    @Override
    public void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
        super.drawTexts(screenElements, inventorySlot);
        screenElements.widget(
            TextWidget.dynamicString(() -> "剩余燃烧时间: " + this.intBurnTime / 20 + " s")
                .setSynced(true)
                .setTextAlignment(Alignment.CenterLeft)
                .setDefaultColor(COLOR_TEXT_WHITE.get())
                .setEnabled(true));
    }

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
            int addedFuel = tryAddBurnTime();
            if (addedFuel <= 0) return false;
            this.intBurnTime += addedFuel;
            return tryConsumeFuel(aFuelVal);
        } else {
            this.intBurnTime -= aFuelVal;
        }
        return true;
    }

    private int tryAddBurnTime() {
        ArrayList<ItemStack> inputs = getStoredInputs();
        for (ItemStack itemStack : inputs) {
            if (TileEntityFurnace.isItemFuel(itemStack)) {
                int burnTime = TileEntityFurnace.getItemBurnTime(itemStack);
                if (burnTime < 300) return 0;
                ItemStack fuel = ItemStack.copyItemStack(itemStack);
                fuel.stackSize = 1;
                depleteInput(fuel);
                return burnTime;
            }
        }
        return 0;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("mBurnTime", this.intBurnTime);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        this.intBurnTime = aNBT.getInteger("mBurnTime");
    }
}
