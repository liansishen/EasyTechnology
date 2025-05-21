package com.hepdd.easytech.common.tileentities.machines.multi;

import static com.hepdd.easytech.api.enums.ETHTextures.MACHINE_CASING_VOID_MINER_WOOD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import com.hepdd.easytech.api.metatileentity.implementations.base.ETHVoidMinerBase;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;

public class ETHPrimitiveVoidMiner extends ETHVoidMinerBase {

    private int intBurnTime;

    public ETHPrimitiveVoidMiner(String aName) {
        super(aName);
        MTEMultiBlockBase.disableMaintenance = true;
        if (!shouldCheckMaintenance()) fixAllIssues();
    }

    public ETHPrimitiveVoidMiner(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        MTEMultiBlockBase.disableMaintenance = true;
        if (!shouldCheckMaintenance()) fixAllIssues();
    }

    @Override
    public ItemList getCasingBlockItem() {
        return ItemList.Casing_Reinforced_Wood;
    }

    @Override
    public Materials getFrameMaterial() {
        return Materials.Wood;
    }

    @Override
    public int getCasingTextureIndex() {
        return MACHINE_CASING_VOID_MINER_WOOD.ID;
    }

    @Override
    public void setElectricityStats() {
        this.mOutputItems = new ItemStack[0];
        this.mProgresstime = 0;
        this.mMaxProgresstime = 80; // 4s
        this.mEUt = -1;
        setMultiplier(4);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHPrimitiveVoidMiner(mName);
    }

    @Override
    public String[] getInfoData() {
        List<String> mInfo = new ArrayList<>(
            Arrays.stream(super.getInfoData())
                .collect(Collectors.toList()));
        mInfo.add("Burn time left:" + this.intBurnTime);

        return mInfo.toArray(new String[0]);
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
}
