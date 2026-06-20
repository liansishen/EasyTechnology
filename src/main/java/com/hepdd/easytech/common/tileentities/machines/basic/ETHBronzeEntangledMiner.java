package com.hepdd.easytech.common.tileentities.machines.basic;

import static gregtech.api.enums.Mods.GregTech;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizons.modularui.api.drawable.FallbackableUITexture;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;

import gregtech.api.enums.SteamVariant;
import gregtech.api.enums.Textures;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.gui.modularui.GUITextureSet;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.BasicUIProperties;
import gregtech.api.render.TextureFactory;

@IMetaTileEntity.SkipGenerateDescription
public class ETHBronzeEntangledMiner extends ETHAbstractEntangledMiner {

    private static final int STEAM_CAPACITY = 16000;
    private static final int STEAM_PER_EU = 2;
    private static final FallbackableUITexture PROGRESSBAR_TEXTURE = GTUITextures
        .fallbackableProgressbar("miner", UITexture.fullImage(GregTech.ID, "gui/progressbar/arrow_bronze"));

    public ETHBronzeEntangledMiner(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 1, 1, 1, 1, 2);
    }

    public ETHBronzeEntangledMiner(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures, 1, 1, 1, 2);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aBaseTileEntity) {
        return new ETHBronzeEntangledMiner(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    protected ITexture getCustomBaseTexture(ForgeDirection aSide, int aColorIndex) {
        return TextureFactory.of(switch (aSide) {
            case DOWN -> Textures.BlockIcons.MACHINE_BRONZE_BOTTOM;
            case UP -> Textures.BlockIcons.MACHINE_BRONZE_TOP;
            default -> Textures.BlockIcons.MACHINE_BRONZE_SIDE;
        });
    }

    @Override
    protected String getTooltipKey() {
        return "easytech.tooltip.entangled_miner.bronze";
    }

    @Override
    protected Object getTooltipEnergyUsage() {
        return ENERGY[minerTier] * STEAM_PER_EU * 20;
    }

    // ==================== Inventory ====================

    @Override
    protected boolean allowPutStackValidated(IGregTechTileEntity aBaseMetaTileEntity, int aIndex,
        net.minecraftforge.common.util.ForgeDirection aSide, ItemStack aStack) {
        if (aIndex == getInputSlot()) return aStack != null && aStack.getItem() instanceof ETHEntangledCard;
        return false;
    }

    // ==================== Energy (Steam via fluid tank) ====================

    @Override
    public boolean isFluidInputAllowed(FluidStack aFluid) {
        return aFluid != null && aFluid.getFluid() == FluidRegistry.getFluid("steam");
    }

    @Override
    public boolean canTankBeFilled() {
        return true;
    }

    @Override
    public boolean canTankBeEmptied() {
        return false;
    }

    @Override
    public int getCapacity() {
        return STEAM_CAPACITY;
    }

    @Override
    public net.minecraftforge.fluids.FluidTankInfo[] getTankInfo(net.minecraftforge.common.util.ForgeDirection from) {
        return new net.minecraftforge.fluids.FluidTankInfo[] {
            new net.minecraftforge.fluids.FluidTankInfo(mFluid, getCapacity()) };
    }

    @Override
    protected boolean hasEnoughEnergy(IGregTechTileEntity aBaseMetaTileEntity, int aRequiredEU) {
        return mFluid != null && mFluid.amount >= aRequiredEU * STEAM_PER_EU;
    }

    @Override
    protected void consumeEnergy(IGregTechTileEntity aBaseMetaTileEntity, int aEU) {
        if (mFluid != null) {
            mFluid.amount -= aEU * STEAM_PER_EU;
            if (mFluid.amount <= 0) mFluid = null;
        }
    }

    @Override
    protected long getAvailableEnergy(IGregTechTileEntity aBaseMetaTileEntity) {
        return mFluid != null ? mFluid.amount / STEAM_PER_EU : 0;
    }

    @Override
    protected String getEnergyDisplayString() {
        int stored = mFluid != null ? mFluid.amount : 0;
        return StatCollector.translateToLocalFormatted("easytech.machine.entangled_miner.steam", stored);
    }

    // ==================== GUI ====================

    @Override
    public GUITextureSet getGUITextureSet() {
        return GUITextureSet.STEAM.apply(SteamVariant.BRONZE);
    }

    @Override
    protected BasicUIProperties getUIProperties() {
        return super.getUIProperties().toBuilder()
            .progressBarTexture(PROGRESSBAR_TEXTURE)
            .maxFluidInputs(0)
            .maxFluidOutputs(0)
            .build();
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder aBuilder, UIBuildContext aContext) {
        super.addUIWidgets(aBuilder, aContext);

        // Steam fluid slot (left of card slot, 15px gap)
        aBuilder.widget(
            new FluidSlotWidget(getFluidTank()).setPos(new Pos2d(28, 24))
                .setSize(new Size(18, 18)));
    }
}
