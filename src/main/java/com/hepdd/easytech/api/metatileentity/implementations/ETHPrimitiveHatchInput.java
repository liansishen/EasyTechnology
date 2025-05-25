package com.hepdd.easytech.api.metatileentity.implementations;

import static com.hepdd.easytech.loaders.preload.ETHStatics.AuthorEasyTechForItem;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.render.TextureFactory;

public class ETHPrimitiveHatchInput extends MTEHatchInput {

    public ETHPrimitiveHatchInput(int aID, int aSlot, String aName, String aNameRegional, int aTier) {
        super(
            aID,
            aSlot,
            aName,
            aNameRegional,
            aTier,
            new String[] { "Fluid Input for Multiblocks", "Capacity: 8000L", "Can hold 1 types of fluid.",
                AuthorEasyTechForItem });
    }

    public ETHPrimitiveHatchInput(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHPrimitiveHatchInput(mName, mTier, mDescriptionArray, mTextures);
    }

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
    public int getCapacity() {
        return 8 * 1000;
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);
    }
}
