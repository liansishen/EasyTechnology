package com.hepdd.easytech.api.metatileentity.implementations;

import static com.hepdd.easytech.loaders.preload.ETHStatics.AuthorEasyTechForItem;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.render.TextureFactory;

public class ETHPrimitiveHatchInputBus extends MTEHatchInputBus {

    public ETHPrimitiveHatchInputBus(int id, String name, String nameRegional, int tier) {
        super(
            id,
            name,
            nameRegional,
            tier,
            1,
            new String[] { "Item Input for Multiblocks", "Capacity: 1 stack", AuthorEasyTechForItem });
    }

    private int texturePage = 0;
    private int textureIndex = 0;

    public ETHPrimitiveHatchInputBus(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHPrimitiveHatchInputBus(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {

        ITexture background;
        if (texturePage > 0 || textureIndex > 0) {
            background = Textures.BlockIcons.casingTexturePages[texturePage][textureIndex];
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
    public boolean allowSelectCircuit() {
        return false;
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        getBaseMetaTileEntity().add1by1Slot(builder);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        texturePage = aNBT.getInteger("texturePage");
        textureIndex = aNBT.getInteger("textureIndex");

        myupdateTexture(texturePage << 7 | textureIndex);
    }

    @Override
    public void onDescriptionPacket(NBTTagCompound data) {
        super.onDescriptionPacket(data);
        texturePage = data.getInteger("texturePage");
        textureIndex = data.getInteger("textureIndex");
    }

    public final void myupdateTexture(int id) {
        texturePage = id >> 7;
        textureIndex = id & 127;

        updateTexture(id);
    }
}
