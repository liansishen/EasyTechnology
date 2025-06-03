package com.hepdd.easytech.api.objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import tconstruct.tools.inventory.CraftingStationContainer;
import tconstruct.tools.logic.CraftingStationLogic;

public class PortableCraftingStationContainer extends CraftingStationContainer {

    public PortableCraftingStationContainer(InventoryPlayer inventoryplayer, CraftingStationLogic logic, int x, int y,
        int z) {
        super(inventoryplayer, logic, x, y, z);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);

        if (!this.logic.getWorldObj().isRemote) {
            for (int i = 1; i <= 9; ++i) {
                ItemStack itemstack = this.logic.getStackInSlot(i);

                if (itemstack != null) {
                    par1EntityPlayer.dropPlayerItemWithRandomChoice(itemstack, false);
                }
            }
        }
    }
}
