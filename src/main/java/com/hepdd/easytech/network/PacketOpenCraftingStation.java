package com.hepdd.easytech.network;

import static com.hepdd.easytech.api.enums.ETHPacketTypes.OPEN_PORTABLE_CRAFTING_STATION;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.IBlockAccess;

import com.google.common.io.ByteArrayDataInput;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHPortableCraftingStation;

import gregtech.api.net.GTPacket;
import io.netty.buffer.ByteBuf;

public class PacketOpenCraftingStation extends GTPacket {

    private EntityPlayerMP player;

    public PacketOpenCraftingStation() {
        super();
    }

    @Override
    public byte getPacketID() {
        return OPEN_PORTABLE_CRAFTING_STATION.id;
    }

    @Override
    public void encode(ByteBuf buffer) {

    }

    @Override
    public GTPacket decode(ByteArrayDataInput buffer) {
        return new PacketOpenCraftingStation();
    }

    @Override
    public void setINetHandler(INetHandler aHandler) {
        player = ((NetHandlerPlayServer) aHandler).playerEntity;
    }

    @Override
    public void process(IBlockAccess world) {

        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack is = player.inventory.getStackInSlot(i);
            if (is != null && is.getItem() instanceof ETHPortableCraftingStation station) {
                station.openGUI(player);
                break;
            }
        }
    }

}
