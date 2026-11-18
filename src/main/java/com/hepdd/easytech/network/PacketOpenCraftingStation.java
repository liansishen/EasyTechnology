package com.hepdd.easytech.network;

import static com.hepdd.easytech.api.enums.ETHPacketTypes.OPEN_PORTABLE_CRAFTING_STATION;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.IBlockAccess;

import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizon.gtnhlib.util.ServerThreadUtil;
import com.hepdd.easytech.api.objects.PortableCraftingStationContainer;
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
    public void encode(ByteBuf buffer) {}

    @Override
    public GTPacket decode(ByteArrayDataInput buffer) {
        return new PacketOpenCraftingStation();
    }

    @Override
    public void setINetHandler(INetHandler aHandler) {
        if (aHandler instanceof NetHandlerPlayServer serverHandler) {
            player = serverHandler.playerEntity;
        }
    }

    @Override
    public void process(IBlockAccess world) {
        final EntityPlayerMP targetPlayer = this.player;
        if (targetPlayer == null) return;

        ServerThreadUtil.addScheduledTask(() -> {
            if (targetPlayer.playerNetServerHandler == null || targetPlayer.isDead || targetPlayer.worldObj == null) {
                return;
            }
            if (targetPlayer.openContainer instanceof PortableCraftingStationContainer) {
                return;
            }
            int currentSlot = targetPlayer.inventory.currentItem;
            ItemStack[] mainInv = targetPlayer.inventory.mainInventory;
            if (currentSlot >= 0 && currentSlot < mainInv.length) {
                ItemStack held = mainInv[currentSlot];
                if (held != null && held.getItem() instanceof ETHPortableCraftingStation station) {
                    station.openGUI(targetPlayer, currentSlot);
                    return;
                }
            }
            for (int i = 0; i < mainInv.length; i++) {
                ItemStack is = mainInv[i];
                if (is != null && is.getItem() instanceof ETHPortableCraftingStation station) {
                    station.openGUI(targetPlayer, i);
                    break;
                }
            }
        });
    }
}
