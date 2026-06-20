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
        aList.add(
            StatCollector
                .translateToLocalFormatted("easytech.tooltip.portable_crafting_station.key", getOpenKeyName()));
    }

    private String getOpenKeyName() {
        try {
            Object keyBinding = Class.forName("com.hepdd.easytech.loaders.preload.ETHLoaderKeybind")
                .getField("openCraftingStation")
                .get(null);
            if (keyBinding == null) return "None";
            int keyCode = (int) keyBinding.getClass()
                .getMethod("getKeyCode")
                .invoke(keyBinding);
            if (keyCode == 0) return "None";
            Object keyName = Class.forName("org.lwjgl.input.Keyboard")
                .getMethod("getKeyName", int.class)
                .invoke(null, keyCode);
            return keyName == null ? "None" : keyName.toString();
        } catch (ReflectiveOperationException e) {
            return "None";
        }
    }
}
