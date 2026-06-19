package com.hepdd.easytech.common.tileentities.machines.basic;

import static gregtech.common.UndergroundOil.undergroundOilReadInformation;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.items.GTGenericItem;
import gregtech.api.util.GTUtility;

public class ETHVoidOilLocationCard extends GTGenericItem {

    public ETHVoidOilLocationCard(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
        super(aUnlocalized, aEnglish, aEnglishTooltip);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (player.worldObj.isRemote) return itemStackIn;
        int dimId = worldIn.provider.dimensionId;
        int posX = 0, posZ = 0;
        if (player instanceof EntityPlayerMP entityPlayerMP) {
            posX = (int) entityPlayerMP.lastTickPosX;
            posZ = (int) entityPlayerMP.lastTickPosZ;
        }
        World world = DimensionManager.getWorld(dimId);
        Chunk chunk = world.getChunkFromBlockCoords(posX, posZ);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimId", dimId);
        tag.setInteger("posX", chunk.xPosition);
        tag.setInteger("posZ", chunk.zPosition);
        String dimName = getDimensionName(dimId, world);
        tag.setString("dimName", dimName);
        FluidStack fs = undergroundOilReadInformation(chunk);
        if (fs != null) {
            String fluidName = getFluidName(fs);
            tag.setString("fluid", fluidName);
            tag.setInteger("fluidAmount", fs.amount);
            GTUtility.sendChatToPlayer(
                player,
                StatCollector.translateToLocalFormatted(
                    "easytech.message.void_oil_card.recorded",
                    dimName,
                    dimId,
                    chunk.xPosition,
                    chunk.zPosition,
                    fluidName,
                    fs.amount));
        } else {
            tag.setString("fluid", StatCollector.translateToLocal("easytech.tooltip.void_oil_card.no_fluid"));
            GTUtility.sendChatToPlayer(
                player,
                StatCollector.translateToLocalFormatted(
                    "easytech.message.void_oil_card.recorded_empty",
                    dimName,
                    dimId,
                    chunk.xPosition,
                    chunk.zPosition));
        }
        itemStackIn.setTagCompound(tag);
        return itemStackIn;
    }

    @Override
    protected void addAdditionalToolTips(List<String> aList, ItemStack aStack, EntityPlayer aPlayer) {
        super.addAdditionalToolTips(aList, aStack, aPlayer);
        aList.add(StatCollector.translateToLocal("easytech.tooltip.void_oil_card.usage"));
        aList.add(StatCollector.translateToLocal("easytech.tooltip.void_oil_card.purpose"));
        aList.add(StatCollector.translateToLocal("easytech.tooltip.void_oil_card.machine"));
        NBTTagCompound tag = aStack.getTagCompound();
        if (tag == null) {
            aList.add(StatCollector.translateToLocal("easytech.tooltip.void_oil_card.empty"));
            return;
        }
        aList.add(
            StatCollector.translateToLocalFormatted(
                "easytech.tooltip.void_oil_card.dimension",
                tag.getString("dimName"),
                tag.getInteger("dimId")));
        aList.add(
            StatCollector.translateToLocalFormatted(
                "easytech.tooltip.void_oil_card.chunk",
                tag.getInteger("posX"),
                tag.getInteger("posZ")));
        aList.add(StatCollector.translateToLocalFormatted("easytech.tooltip.void_oil_card.fluid", tag.getString("fluid")));
        aList.add(
            StatCollector.translateToLocalFormatted(
                "easytech.tooltip.void_oil_card.amount",
                tag.hasKey("fluidAmount") ? tag.getInteger("fluidAmount") : StatCollector.translateToLocal("easytech.tooltip.unknown")));
    }

    private static String getFluidName(FluidStack fluidStack) {
        String registryName = FluidRegistry.getFluidName(fluidStack);
        if (registryName != null) {
            String translated = StatCollector.translateToLocal("fluid." + registryName);
            if (!translated.equals("fluid." + registryName)) return translated;
        }
        return fluidStack.getLocalizedName();
    }

    private static String getDimensionName(int dimId, World world) {
        return switch (dimId) {
            case -1 -> StatCollector.translateToLocal("easytech.dimension.nether");
            case 0 -> StatCollector.translateToLocal("easytech.dimension.overworld");
            case 1 -> StatCollector.translateToLocal("easytech.dimension.the_end");
            default -> world.provider.getDimensionName();
        };
    }
}
