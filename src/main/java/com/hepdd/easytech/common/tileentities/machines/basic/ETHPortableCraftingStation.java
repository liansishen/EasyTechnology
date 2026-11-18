package com.hepdd.easytech.common.tileentities.machines.basic;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.hepdd.easytech.EasyTechnology;
import com.hepdd.easytech.proxy.GuiHandler;

import gregtech.api.items.GTGenericItem;

public class ETHPortableCraftingStation extends GTGenericItem {

    public ETHPortableCraftingStation(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
        super(aUnlocalized, aEnglish, aEnglishTooltip);
        setMaxStackSize(1);
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

    @Override
    protected void addAdditionalToolTips(List<String> aList, ItemStack aStack, EntityPlayer aPlayer) {
        super.addAdditionalToolTips(aList, aStack, aPlayer);
        aList.add(StatCollector.translateToLocal("easytech.tooltip.portable_crafting_station.usage"));
        String keyName = EasyTechnology.proxy != null ? EasyTechnology.proxy.getOpenCraftingStationKeyName() : "None";
        aList.add(StatCollector.translateToLocalFormatted("easytech.tooltip.portable_crafting_station.key", keyName));
    }
}
