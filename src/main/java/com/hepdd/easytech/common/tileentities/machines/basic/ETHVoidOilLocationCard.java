package com.hepdd.easytech.common.tileentities.machines.basic;

import com.hepdd.easytech.api.objects.GTChunkManagerEx;
import gregtech.GTMod;
import gregtech.api.objects.GTChunkManager;
import gtneioreplugin.util.DimensionHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import gregtech.api.items.GTGenericItem;
import gregtech.api.util.GTUtility;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;

public class ETHVoidOilLocationCard extends GTGenericItem {

    public ETHVoidOilLocationCard(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
        super(aUnlocalized, aEnglish, aEnglishTooltip);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (player.worldObj.isRemote) return itemStackIn;
        int dimId = worldIn.provider.dimensionId;
        int posX = 0,posZ = 0;
        //ChunkCoordinates chunkCoordinates = player.playerLocation;
        if (player instanceof EntityPlayerMP entityPlayerMP) {
            posX = (int) entityPlayerMP.lastTickPosX;
            posZ = (int) entityPlayerMP.lastTickPosZ;
            GTUtility.sendChatToPlayer(player, "dim:" + dimId + "x:" + posX + "z:" + posZ);
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimId",dimId);
        tag.setInteger("posX",posX);
        tag.setInteger("posZ",posZ);
        itemStackIn.setTagCompound(tag);
//        ChunkCoordIntPair chunkXZ;
//        chunkXZ = new ChunkCoordIntPair(posX,posZ);
//        if (player.isSneaking()) {
//            GTChunkManagerEx.releaseChunk(player.worldObj.getTileEntity(37,4,334),chunkXZ);
//            GTChunkManagerEx.releaseTicket(player.worldObj.getTileEntity(37,4,334));
//            GTUtility.sendChatToPlayer(player, "chunk release");
//            return itemStackIn;
//        }
//        GTChunkManagerEx.requestPlayerChunkLoad(player.worldObj.getTileEntity(37,4,334), chunkXZ,"",-1);
//        GTUtility.sendChatToPlayer(player, "chunk loaded");
        return itemStackIn;
    }

}
