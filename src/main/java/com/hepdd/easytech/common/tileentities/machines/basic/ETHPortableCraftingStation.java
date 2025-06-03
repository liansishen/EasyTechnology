package com.hepdd.easytech.common.tileentities.machines.basic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.hepdd.easytech.EasyTechnology;
import com.hepdd.easytech.GuiHandler;
import com.hepdd.easytech.api.objects.PortableCraftingStationContainer;

import gregtech.api.items.GTGenericItem;
import tconstruct.tools.logic.CraftingStationLogic;

public class ETHPortableCraftingStation extends GTGenericItem {

    public ETHPortableCraftingStation(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
        super(aUnlocalized, aEnglish, aEnglishTooltip);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote) {
            player.openGui(
                EasyTechnology.instance,
                GuiHandler.GUI1,
                worldIn,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ);
        }
        return super.onItemRightClick(itemStackIn, worldIn, player);
    }

    public Object GetGUI(EntityPlayer player, World world, int x, int y, int z) {
        CraftingStationLogic logic = new CraftingStationLogic();
        logic.setWorldObj(world);
        return new PortableCraftingStationContainer(player.inventory, logic, x, y, z);
    }
}
