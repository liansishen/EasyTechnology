package com.hepdd.easytech.api.objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.IModifyable;
import tconstruct.library.tools.AbilityHelper;

// Uses Tinkers' ingredient rules with the portable container's immediate recipe updates.
class PortableCraftingStationSlot extends SlotCrafting {

    private final IInventory matrix;

    PortableCraftingStationSlot(EntityPlayer player, IInventory matrix, IInventory result, int index, int x, int y) {
        super(player, matrix, result, index, x, y);
        this.matrix = matrix;
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        ItemStack tool = matrix.getStackInSlot(4);
        if (stack.getItem() instanceof IModifyable modifyable && tool != null
            && tool.getItem() instanceof IModifyable) {
            NBTTagCompound tags = stack.getTagCompound()
                .getCompoundTag(modifyable.getBaseTagName());
            int[] toRemoveArray = tags.hasKey("ToRemove") ? tags.getIntArray("ToRemove") : null;
            int toRemoveIndex = 0;

            for (int i = 0; i < matrix.getSizeInventory(); i++) {
                if (i == 4) continue;
                ItemStack item = matrix.getStackInSlot(i);
                if (item == null) continue;
                if (toRemoveArray == null || toRemoveIndex >= toRemoveArray.length) {
                    matrix.decrStackSize(i, 1);
                } else {
                    matrix.decrStackSize(i, toRemoveArray[toRemoveIndex]);
                    toRemoveIndex++;
                }
            }
            tags.removeTag("ToRemove");
            matrix.setInventorySlotContents(4, null);
            player.worldObj.playSoundEffect(
                player.posX,
                player.posY,
                player.posZ,
                "tinker:little_saw",
                1.0F,
                (AbilityHelper.random.nextFloat() - AbilityHelper.random.nextFloat()) * 0.2F + 1.0F);
        } else {
            super.onPickupFromSlot(player, stack);
        }
    }
}
