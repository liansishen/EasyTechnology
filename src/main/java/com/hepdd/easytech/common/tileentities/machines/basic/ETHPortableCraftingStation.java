package com.hepdd.easytech.common.tileentities.machines.basic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.hepdd.easytech.EasyTechnology;
import com.hepdd.easytech.proxy.GuiHandler;

import gregtech.api.items.GTGenericItem;

public class ETHPortableCraftingStation extends GTGenericItem {

    public ETHPortableCraftingStation(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
        super(aUnlocalized, aEnglish, aEnglishTooltip);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote) {
            openGUI(player, player.inventory.currentItem);
        }
        return super.onItemRightClick(itemStackIn, worldIn, player);
    }

    public void openGUI(EntityPlayer player, int slotId) {
        player.openGui(EasyTechnology.instance, GuiHandler.GUI1, player.worldObj, slotId, 0, 0);
    }
}
