package com.hepdd.easytech.common.tileentities.machines.basic;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import gregtech.api.items.GTGenericItem;

public class ETHEntangledCard extends GTGenericItem {

    public ETHEntangledCard(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
        super(aUnlocalized, aEnglish, aEnglishTooltip);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return hasTarget(stack);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (world.isRemote) return itemStack;
        int x = MathHelper.floor_double(player.posX);
        int y = MathHelper.floor_double(player.posY);
        int z = MathHelper.floor_double(player.posZ);
        if (player instanceof EntityPlayerMP entityPlayer) {
            x = MathHelper.floor_double(entityPlayer.posX);
            y = MathHelper.floor_double(entityPlayer.posY);
            z = MathHelper.floor_double(entityPlayer.posZ);
        }
        writeTarget(itemStack, world, x, y, z);
        sendRecordedMessage(player, world, x, y, z);
        return itemStack;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        writeTarget(itemStack, world, x, y, z);
        sendRecordedMessage(player, world, x, y, z);
        return true;
    }

    private static void sendRecordedMessage(EntityPlayer player, World world, int x, int y, int z) {
        int dimId = world.provider.dimensionId;
        player.addChatMessage(
            new ChatComponentTranslation(
                "easytech.message.entangled_card.recorded",
                createDimensionChatArg(dimId, world),
                dimId,
                x,
                y,
                z));
    }

    @Override
    protected void addAdditionalToolTips(List<String> list, ItemStack stack, EntityPlayer player) {
        super.addAdditionalToolTips(list, stack, player);
        list.add(StatCollector.translateToLocal("easytech.tooltip.entangled_card.usage"));
        list.add(StatCollector.translateToLocal("easytech.tooltip.entangled_card.machine"));
        NBTTagCompound tag = stack.getTagCompound();
        if (!hasTarget(stack) || tag == null) {
            list.add(StatCollector.translateToLocal("easytech.tooltip.entangled_card.empty"));
            return;
        }
        int dimId = tag.getInteger("dimId");
        list.add(
            StatCollector.translateToLocalFormatted(
                "easytech.tooltip.entangled_card.dimension",
                resolveClientDimensionName(tag, dimId),
                dimId));
        list.add(
            StatCollector.translateToLocalFormatted(
                "easytech.tooltip.entangled_card.position",
                tag.getInteger("x"),
                tag.getInteger("y"),
                tag.getInteger("z")));
    }

    private static void writeTarget(ItemStack stack, World world, int x, int y, int z) {
        int dimId = world.provider.dimensionId;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimId", dimId);
        if (dimId != -1 && dimId != 0 && dimId != 1 && world.provider != null) {
            String providerName = world.provider.getDimensionName();
            if (providerName != null && !providerName.isEmpty()) {
                tag.setString("dimProviderName", providerName);
            }
        }
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setInteger("z", z);
        stack.setTagCompound(tag);
    }

    public static boolean hasTarget(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ETHEntangledCard)) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("dimId") && tag.hasKey("x") && tag.hasKey("y") && tag.hasKey("z");
    }

    static Object createDimensionChatArg(int dimId, World world) {
        return switch (dimId) {
            case -1 -> new ChatComponentTranslation("easytech.dimension.nether");
            case 0 -> new ChatComponentTranslation("easytech.dimension.overworld");
            case 1 -> new ChatComponentTranslation("easytech.dimension.the_end");
            default -> world != null && world.provider != null ? world.provider.getDimensionName()
                : new ChatComponentTranslation("easytech.tooltip.unknown");
        };
    }

    static String resolveClientDimensionName(NBTTagCompound tag, int dimId) {
        return switch (dimId) {
            case -1 -> StatCollector.translateToLocal("easytech.dimension.nether");
            case 0 -> StatCollector.translateToLocal("easytech.dimension.overworld");
            case 1 -> StatCollector.translateToLocal("easytech.dimension.the_end");
            default -> {
                if (tag != null) {
                    if (tag.hasKey("dimProviderName")) {
                        String raw = tag.getString("dimProviderName");
                        if (!raw.isEmpty()) yield raw;
                    }
                    // Backward compatibility for legacy cards storing "dimName"
                    if (tag.hasKey("dimName")) {
                        String legacy = tag.getString("dimName");
                        if (!legacy.isEmpty()) yield legacy;
                    }
                }
                World targetWorld = DimensionManager.getWorld(dimId);
                if (targetWorld != null && targetWorld.provider != null) {
                    yield targetWorld.provider.getDimensionName();
                }
                yield StatCollector.translateToLocal("easytech.tooltip.unknown");
            }
        };
    }
}
