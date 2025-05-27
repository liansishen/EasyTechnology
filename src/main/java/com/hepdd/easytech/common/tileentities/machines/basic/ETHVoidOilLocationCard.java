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
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static gregtech.common.UndergroundOil.undergroundOilReadInformation;

public class ETHVoidOilLocationCard extends GTGenericItem {

    public ETHVoidOilLocationCard(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
        super(aUnlocalized, aEnglish, aEnglishTooltip);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (player.worldObj.isRemote) return itemStackIn;
        int dimId = worldIn.provider.dimensionId;
        int posX = 0,posZ = 0;
        if (player instanceof EntityPlayerMP entityPlayerMP) {
            posX = (int) entityPlayerMP.lastTickPosX;
            posZ = (int) entityPlayerMP.lastTickPosZ;
        }
        World world = DimensionManager.getWorld(dimId);
        Chunk chunk = world.getChunkFromBlockCoords(posX,posZ);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimId",dimId);
        tag.setInteger("posX",chunk.xPosition);
        tag.setInteger("posZ",chunk.zPosition);
        tag.setString("dimName",world.provider.getDimensionName());
        FluidStack fs = undergroundOilReadInformation(chunk);
        if (fs != null) {
            tag.setString("fluid",fs.getLocalizedName());
            tag.setInteger("fluidAmount",fs.amount);
        } else {
            tag.setString("fluid","无地下流体");
        }
        itemStackIn.setTagCompound(tag);
        GTUtility.sendChatToPlayer(player, "dim:" + dimId + "x:" + chunk.xPosition + "z:" + chunk.zPosition);
        return itemStackIn;
    }

    @Override
    protected void addAdditionalToolTips(List<String> aList, ItemStack aStack, EntityPlayer aPlayer) {
        super.addAdditionalToolTips(aList, aStack, aPlayer);
        NBTTagCompound tag = aStack.getTagCompound();
        if (tag != null) {
            int dimId = tag.getInteger("dimId");
            aList.add("目标维度：" + tag.getString("dimName"));
            aList.add("区块坐标：" + tag.getInteger("posX") + "," + tag.getInteger("posZ"));
            aList.add("流体：" + tag.getString("fluid")+"("+tag.getInteger("fluidAmount")+")");
        }
    }
}
