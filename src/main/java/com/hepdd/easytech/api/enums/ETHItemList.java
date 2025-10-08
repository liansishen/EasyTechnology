package com.hepdd.easytech.api.enums;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;

public enum ETHItemList {

    Machine_Large_Bricked_BlastFurnace,
    Machine_Large_Coke_Oven,
    Machine_Primitive_Void_Miner,
    Machine_Steam_Void_Miner,
    Machine_LV_Void_Miner,
    Machine_HV_Void_Miner,
    Machine_IV_Void_Miner,

    Hatch_Input_Primitive,
    Hatch_Output_Primitive,
    Hatch_Input_Bus_Primitive,
    Hatch_Output_Bus_Primitive,

    ITEM_Void_Oil_Location_Card,
    ITEM_Portable_Crafting_Station,

    ;

    private boolean mHasNotBeenSet;
    private boolean mDeprecated;
    private boolean mWarned;
    private ItemStack mStack;

    ETHItemList() {
        mHasNotBeenSet = true;
    }

    ETHItemList(boolean aDeprecated) {
        if (aDeprecated) {
            mDeprecated = true;
            mHasNotBeenSet = true;
        }
    }

    public int getMeta() {
        return mStack.getItemDamage();
    }

    public ETHItemList set(Item aItem) {
        mHasNotBeenSet = false;
        if (aItem == null) return this;
        ItemStack aStack = new ItemStack(aItem, 1, 0);
        mStack = GTUtility.copyAmount(1, aStack);
        return this;
    }

    public ETHItemList set(ItemStack aStack) {
        if (aStack != null) {
            mHasNotBeenSet = false;
            mStack = GTUtility.copyAmount(1, aStack);
        }
        return this;
    }

    public ETHItemList set(IMetaTileEntity metaTileEntity) {
        if (metaTileEntity == null) throw new IllegalArgumentException("Invalid Meta Tile Entity");
        set(metaTileEntity.getStackForm(1L));
        return this;
    }

    public boolean hasBeenSet() {
        return !mHasNotBeenSet;
    }

    public ItemStack getInternalStack_unsafe() {
        return mStack;
    }

    public Item getItem() {
        sanityCheck();
        if (GTUtility.isStackInvalid(mStack)) return null;
        return mStack.getItem();
    }

    public Block getBlock() {
        sanityCheck();
        return Block.getBlockFromItem(getItem());
    }

    public ItemStack get(int aAmount, Object... aReplacements) {
        sanityCheck();
        // if invalid, return a replacements
        if (GTUtility.isStackInvalid(mStack)) {
            GTLog.out.println("Object in the ItemList is null at:");
        }
        return GTUtility.copyAmount(aAmount, mStack);
    }

    private void sanityCheck() {
        if (mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + name() + "' has not been set to an Item at this time!");
        if (mDeprecated && !mWarned) {
            new Exception(this + " is now deprecated").printStackTrace(GTLog.err);
            // warn only once
            mWarned = true;
        }
    }
}
