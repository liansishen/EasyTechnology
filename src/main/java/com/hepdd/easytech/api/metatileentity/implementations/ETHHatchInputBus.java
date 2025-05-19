package com.hepdd.easytech.api.metatileentity.implementations;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.render.TextureFactory;

public class ETHHatchInputBus extends MTEHatchInputBus {

    public ETHHatchInputBus(int id, String name, String nameRegional, int tier) {
        super(id, name, nameRegional, tier);
    }

    public ETHHatchInputBus(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHHatchInputBus(mName, mTier, mDescriptionArray, mTextures);
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
        return aIndex != getCircuitSlot();
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return aIndex != getCircuitSlot() && (mRecipeMap == null || disableFilter || mRecipeMap.containsInput(aStack))
            && (disableLimited || limitedAllowPutStack(aIndex, aStack));
    }

    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {}

    @Override
    public boolean allowSelectCircuit() {
        return false;
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {

    }
}
