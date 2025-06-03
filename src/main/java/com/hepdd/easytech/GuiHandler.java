package com.hepdd.easytech;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.hepdd.easytech.common.tileentities.machines.basic.ETHPortableCraftingStation;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import tconstruct.tools.gui.CraftingStationGui;
import tconstruct.tools.logic.CraftingStationLogic;

public class GuiHandler implements IGuiHandler {

    public static final int GUI1 = 0;

    public static void init() {
        NetworkRegistry.INSTANCE.registerGuiHandler(EasyTechnology.instance, new GuiHandler());
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI1) {
            ItemStack itemStack = player.getHeldItem();
            if (itemStack.getItem() instanceof ETHPortableCraftingStation station) {
                return station.GetGUI(player, world, x, y, z);
            }
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI1) {
            CraftingStationLogic logic = new CraftingStationLogic();
            logic.setWorldObj(world);
            return new CraftingStationGui(player.inventory, logic, world, x, y, z);
        }
        return null;
    }
}
