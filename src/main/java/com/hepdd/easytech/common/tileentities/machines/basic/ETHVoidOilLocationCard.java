package com.hepdd.easytech.common.tileentities.machines.basic;

import static gregtech.common.UndergroundOil.undergroundOilReadInformation;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.items.GTGenericItem;
import gregtech.api.util.GTUtility;

public class ETHVoidOilLocationCard extends GTGenericItem {

    private static final String TEXTURE = "easytechnology:void_oil_location_card";

    public ETHVoidOilLocationCard(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
        super(aUnlocalized, aEnglish, aEnglishTooltip);
    }

    public static boolean hasValidTarget(ItemStack stack) {
        if (!GTUtility.isStackValid(stack) || !(stack.getItem() instanceof ETHVoidOilLocationCard)) {
            return false;
        }
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("dimId") && tag.hasKey("posX") && tag.hasKey("posZ");
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return hasValidTarget(stack);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        mIcon = iconRegister.registerIcon(TEXTURE);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (player.worldObj.isRemote) return itemStackIn;
        int dimId = worldIn.provider.dimensionId;
        int posX = MathHelper.floor_double(player.posX);
        int posZ = MathHelper.floor_double(player.posZ);
        if (player instanceof EntityPlayerMP entityPlayerMP) {
            posX = MathHelper.floor_double(entityPlayerMP.posX);
            posZ = MathHelper.floor_double(entityPlayerMP.posZ);
        }
        World world = DimensionManager.getWorld(dimId);
        if (world == null) world = worldIn;
        Chunk chunk = world.getChunkFromBlockCoords(posX, posZ);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimId", dimId);
        tag.setInteger("posX", chunk.xPosition);
        tag.setInteger("posZ", chunk.zPosition);
        if (dimId != -1 && dimId != 0 && dimId != 1 && world.provider != null) {
            String providerName = world.provider.getDimensionName();
            if (providerName != null && !providerName.isEmpty()) {
                tag.setString("dimProviderName", providerName);
            }
        }

        Object dimChatArg = ETHEntangledCard.createDimensionChatArg(dimId, world);
        FluidStack fs = undergroundOilReadInformation(chunk);
        if (fs != null && fs.getFluid() != null && fs.amount > 0) {
            String registryName = FluidRegistry.getFluidName(fs);
            tag.setString(
                "fluidName",
                registryName != null ? registryName
                    : fs.getFluid()
                        .getName());
            tag.setInteger("fluidAmount", fs.amount);
            Object fluidChatArg = new ChatComponentTranslation(fs.getUnlocalizedName());
            player.addChatMessage(
                new ChatComponentTranslation(
                    "easytech.message.void_oil_card.recorded",
                    dimChatArg,
                    dimId,
                    chunk.xPosition,
                    chunk.zPosition,
                    fluidChatArg,
                    fs.amount));
        } else {
            tag.setString("fluidName", "");
            tag.setInteger("fluidAmount", 0);
            player.addChatMessage(
                new ChatComponentTranslation(
                    "easytech.message.void_oil_card.recorded_empty",
                    dimChatArg,
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
        if (!hasValidTarget(aStack) || tag == null) {
            aList.add(StatCollector.translateToLocal("easytech.tooltip.void_oil_card.empty"));
            return;
        }
        int dimId = tag.getInteger("dimId");
        String dimDisplay = ETHEntangledCard.resolveClientDimensionName(tag, dimId);
        aList.add(
            StatCollector.translateToLocalFormatted("easytech.tooltip.void_oil_card.dimension", dimDisplay, dimId));
        aList.add(
            StatCollector.translateToLocalFormatted(
                "easytech.tooltip.void_oil_card.chunk",
                tag.getInteger("posX"),
                tag.getInteger("posZ")));
        aList.add(
            StatCollector
                .translateToLocalFormatted("easytech.tooltip.void_oil_card.fluid", resolveClientFluidName(tag)));
        aList.add(
            StatCollector.translateToLocalFormatted(
                "easytech.tooltip.void_oil_card.amount",
                tag.hasKey("fluidAmount") ? tag.getInteger("fluidAmount")
                    : StatCollector.translateToLocal("easytech.tooltip.unknown")));
    }

    private static String resolveClientFluidName(NBTTagCompound tag) {
        if (tag.hasKey("fluidName")) {
            String regName = tag.getString("fluidName");
            if (regName == null || regName.isEmpty()) {
                return StatCollector.translateToLocal("easytech.tooltip.void_oil_card.no_fluid");
            }
            return localizeFluidByRegistryName(regName);
        }
        // Backward compatibility for legacy cards that baked translated strings into "fluid"
        if (tag.hasKey("fluid")) {
            String legacyFluid = tag.getString("fluid");
            if (!tag.hasKey("fluidAmount") || legacyFluid.isEmpty() || "No underground fluid".equals(legacyFluid)) {
                return StatCollector.translateToLocal("easytech.tooltip.void_oil_card.no_fluid");
            }
            Fluid byName = FluidRegistry.getFluid(legacyFluid);
            if (byName != null) {
                return localizeFluid(byName);
            }
            return legacyFluid;
        }
        return StatCollector.translateToLocal("easytech.tooltip.void_oil_card.no_fluid");
    }

    private static String localizeFluidByRegistryName(String regName) {
        Fluid fluid = FluidRegistry.getFluid(regName);
        if (fluid != null) {
            return localizeFluid(fluid);
        }
        String key = "fluid." + regName;
        String translated = StatCollector.translateToLocal(key);
        return !key.equals(translated) ? translated : regName;
    }

    private static String localizeFluid(Fluid fluid) {
        String registryName = FluidRegistry.getFluidName(fluid);
        if (registryName != null) {
            String key = "fluid." + registryName;
            String translated = StatCollector.translateToLocal(key);
            if (!key.equals(translated)) {
                return translated;
            }
        }
        return fluid.getLocalizedName(new FluidStack(fluid, 1));
    }
}
