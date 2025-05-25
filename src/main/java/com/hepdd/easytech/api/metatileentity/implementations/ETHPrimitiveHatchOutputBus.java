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
import gregtech.api.metatileentity.implementations.MTEHatchOutputBus;
import gregtech.api.render.TextureFactory;

public class ETHPrimitiveHatchOutputBus extends MTEHatchOutputBus {

    public ETHPrimitiveHatchOutputBus(int aID, String aName, String aNameRegional, int aTier) {
        super(
            aID,
            aName,
            aNameRegional,
            aTier,
            new String[] { "Item Output for Multiblocks", "Capacity: 1 stack", AuthorEasyTechForItem },
            1);
    }

    public ETHPrimitiveHatchOutputBus(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHPrimitiveHatchOutputBus(mName, mTier, mDescriptionArray, mTextures);
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
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {}

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return true;
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        getBaseMetaTileEntity().add1by1Slot(builder);
    }
}
