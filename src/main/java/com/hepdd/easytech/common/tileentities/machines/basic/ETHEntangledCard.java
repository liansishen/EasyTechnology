package com.hepdd.easytech.common.tileentities.machines.basic;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import gregtech.api.items.GTGenericItem;
import gregtech.api.util.GTUtility;

public class ETHEntangledCard extends GTGenericItem {

    public ETHEntangledCard(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
        super(aUnlocalized, aEnglish, aEnglishTooltip);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("dimId") && tag.hasKey("x") && tag.hasKey("y") && tag.hasKey("z");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (world.isRemote) return itemStack;
        int x = 0;
        int y = 0;
        int z = 0;
        if (player instanceof EntityPlayerMP entityPlayer) {
            x = (int) Math.floor(entityPlayer.posX);
            y = (int) Math.floor(entityPlayer.posY);
            z = (int) Math.floor(entityPlayer.posZ);
        }
        writeTarget(itemStack, world, x, y, z);
        GTUtility.sendChatToPlayer(
            player,
            StatCollector.translateToLocalFormatted(
                "easytech.message.entangled_card.recorded",
                getDimensionName(world.provider.dimensionId, world),
                world.provider.dimensionId,
                x,
                y,
                z));
        return itemStack;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        writeTarget(itemStack, world, x, y, z);
        GTUtility.sendChatToPlayer(
            player,
            StatCollector.translateToLocalFormatted(
                "easytech.message.entangled_card.recorded",
                getDimensionName(world.provider.dimensionId, world),
                world.provider.dimensionId,
                x,
                y,
                z));
        return true;
    }

    @Override
    protected void addAdditionalToolTips(List<String> list, ItemStack stack, EntityPlayer player) {
        super.addAdditionalToolTips(list, stack, player);
        list.add(StatCollector.translateToLocal("easytech.tooltip.entangled_card.usage"));
        list.add(StatCollector.translateToLocal("easytech.tooltip.entangled_card.machine"));
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            list.add(StatCollector.translateToLocal("easytech.tooltip.entangled_card.empty"));
            return;
        }
        list.add(
            StatCollector.translateToLocalFormatted(
                "easytech.tooltip.entangled_card.dimension",
                tag.getString("dimName"),
                tag.getInteger("dimId")));
        list.add(
            StatCollector.translateToLocalFormatted(
                "easytech.tooltip.entangled_card.position",
                tag.getInteger("x"),
                tag.getInteger("y"),
                tag.getInteger("z")));
    }

    private static void writeTarget(ItemStack stack, World world, int x, int y, int z) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimId", world.provider.dimensionId);
        tag.setString("dimName", getDimensionName(world.provider.dimensionId, world));
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setInteger("z", z);
        stack.setTagCompound(tag);
    }

    public static boolean hasTarget(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ETHEntangledCard && stack.getTagCompound() != null;
    }

    public static String getDimensionName(int dimId, World world) {
        return switch (dimId) {
            case -1 -> StatCollector.translateToLocal("easytech.dimension.nether");
            case 0 -> StatCollector.translateToLocal("easytech.dimension.overworld");
            case 1 -> StatCollector.translateToLocal("easytech.dimension.the_end");
            default -> {
                World targetWorld = world != null ? world : DimensionManager.getWorld(dimId);
                yield targetWorld == null ? StatCollector.translateToLocal("easytech.tooltip.unknown")
                    : targetWorld.provider.getDimensionName();
            }
        };
    }
}
