package com.hepdd.easytech.api.metatileentity.implementations;

import static com.hepdd.easytech.loaders.preload.ETHStatics.AuthorEasyTechForItem;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.enums.Textures;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.render.TextureFactory;

public class ETHPrimitiveHatchOutput extends MTEHatchOutput {

    public ETHPrimitiveHatchOutput(int aID, String aName, String aNameRegional, int aTier) {
        super(
            aID,
            aName,
            aNameRegional,
            aTier,
            new String[] { "Fluid Output for Multiblocks", "Capacity: 64000L", AuthorEasyTechForItem },
            1);
    }

    public ETHPrimitiveHatchOutput(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHPrimitiveHatchOutput(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return aIndex == 1;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return aIndex == 0;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {}

    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {}

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {
        int texturePointer = getUpdateData(); // just to be sure, from my testing the 8th bit cannot be
        // set clientside
        int textureIndex = texturePointer | (getmTexturePage() << 7); // Shift seven since one page is 128 textures!

        ITexture background;
        if (textureIndex > 0) {
            background = Textures.BlockIcons.casingTexturePages[getmTexturePage()][texturePointer];
        } else {
            background = TextureFactory.of(Blocks.stonebrick);
        }

        if (side != aFacing) {
            return new ITexture[] { background };
        } else {
            if (aActive) {
                return getTexturesActive(background);
            } else {
                return getTexturesInactive(background);
            }
        }
    }

    @Override
    public int getCapacity() {
        return 64 * 1000;
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        builder.widget(
            new DrawableWidget().setDrawable(GTUITextures.PICTURE_SCREEN_BLACK)
                .setPos(7, 16)
                .setSize(71, 45))
            .widget(
                new DrawableWidget().setDrawable(GTUITextures.PICTURE_GAUGE)
                    .setPos(79, 34)
                    .setSize(18, 18))
            .widget(
                new SlotWidget(inventoryHandler, getInputSlot())
                    .setBackground(getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_IN)
                    .setPos(79, 16))
            .widget(
                new SlotWidget(inventoryHandler, getOutputSlot()).setAccess(true, false)
                    .setBackground(getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_OUT)
                    .setPos(79, 52))
            .widget(
                createFluidSlot().setBackground(GTUITextures.TRANSPARENT)
                    .setPos(58, 41))
            .widget(
                new TextWidget("Liquid Amount").setDefaultColor(COLOR_TEXT_WHITE.get())
                    .setPos(10, 20))
            .widget(
                new TextWidget().setStringSupplier(() -> numberFormat.format(mFluid != null ? mFluid.amount : 0))
                    .setDefaultColor(COLOR_TEXT_WHITE.get())
                    .setPos(10, 30));
    }
}
