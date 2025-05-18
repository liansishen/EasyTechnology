package com.hepdd.easytech.common.tileentities.machines.multi;

import static gregtech.api.util.GTWaila.getMachineProgressString;
import static mods.railcraft.common.blocks.machine.alpha.EnumMachineAlpha.COKE_OVEN;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import gregtech.GTMod;
import gregtech.api.enums.SteamVariant;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ISecondaryDescribable;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.MultiblockTooltipBuilder;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class ETHBigCokeOven extends ETHCokeOven implements ISecondaryDescribable {

    private static final ITexture[] FACING_SIDE = {
        TextureFactory.of(new Textures.BlockIcons.CustomIcon("iconsets/MACHINE_CASING_COKEOVEN")) };
    private static final ITexture[] FACING_FRONT = {
        TextureFactory.of(new Textures.BlockIcons.CustomIcon("iconsets/MACHINE_CASING_COKEOVEN_INACTIVE")) };
    private static final ITexture[] FACING_ACTIVE = {
        TextureFactory.of(new Textures.BlockIcons.CustomIcon("iconsets/MACHINE_CASING_COKEOVEN_ACTIVE")) };
    private MultiblockTooltipBuilder tooltipBuilder;

    public ETHBigCokeOven(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public ETHBigCokeOven(String aName) {
        super(aName);
    }

    @Override
    public String[] getDescription() {
        return getCurrentDescription();
    }

    // @Override
    // public boolean isDisplaySecondaryDescription() {
    // return ISecondaryDescribable.super.isDisplaySecondaryDescription();
    // }

    public String[] getPrimaryDescription() {
        return getTooltip().getInformation();
    }

    public String[] getSecondaryDescription() {
        return getTooltip().getStructureInformation();
    }

    protected MultiblockTooltipBuilder getTooltip() {
        if (tooltipBuilder == null) {
            tooltipBuilder = new MultiblockTooltipBuilder();
            tooltipBuilder.addMachineType("Blast Furnace, BBF")
                .addInfo("Usable for Steel and general Pyrometallurgy")
                .addInfo("Has a useful interface, unlike other gregtech multis")
                .addPollutionAmount(GTMod.gregtechproxy.mPollutionPrimitveBlastFurnacePerSecond)
                .beginStructureBlock(7, 6, 7, false)
                .addController("Front center")
                .toolTipFinisher();
        }
        return tooltipBuilder;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {
        if (side == aFacing) {
            return aActive ? FACING_ACTIVE : FACING_FRONT;
        }
        return FACING_SIDE;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHBigCokeOven(this.mName);
    }

    @Override
    protected Block getCasingBlock() {
        return COKE_OVEN.getBlock();
    }

    @Override
    protected int getCasingMetaID() {
        return 7;
    }

    @Override
    public String getName() {
        return "大型焦炉";
    }

    @Override
    public SteamVariant getSteamVariant() {
        return SteamVariant.PRIMITIVE;
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        if (!this.getBaseMetaTileEntity()
            .isInvalidTileEntity()) {
            NBTTagCompound nbt = accessor.getNBTData();
            currenttip.add(
                getMachineProgressString(
                    this.getBaseMetaTileEntity()
                        .isActive(),
                    nbt.getInteger("mMaxProgressTime"),
                    nbt.getInteger("mProgressTime")));
            int level = nbt.getInteger("mLevel");
            currenttip.add("最大并行数：" + (level + 4));
        }
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        if (!this.getBaseMetaTileEntity()
            .isInvalidTileEntity()) {
            tag.setInteger("mProgressTime", this.getProgresstime());
            tag.setInteger("mMaxProgressTime", this.maxProgresstime());
            tag.setInteger("mLevel", this.getLevel());
        }
    }

    @Override
    public String[] getStructureDescription(ItemStack stackSize) {
        return getTooltip().getStructureHint();
    }
}
