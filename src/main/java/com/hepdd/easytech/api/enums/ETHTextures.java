package com.hepdd.easytech.api.enums;

import static gregtech.api.enums.Mods.GregTech;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.IIconContainer;

public class ETHTextures {

    public enum BlockIcons {

        MACHINE_CASING_COKEOVEN_INACTIVE,
        MACHINE_CASING_COKEOVEN_ACTIVE,
        MACHINE_CASING_COKEOVEN;

        public static class CustomIcon implements IIconContainer, Runnable {

            protected IIcon mIcon;
            protected String mIconName;

            public CustomIcon(String aIconName) {
                mIconName = !aIconName.contains(":") ? GregTech.getResourcePath(aIconName) : aIconName;
                GregTechAPI.sGTBlockIconload.add(this);
            }

            @Override
            public void run() {
                mIcon = GregTechAPI.sBlockIcons.registerIcon(mIconName);
            }

            @Override
            public IIcon getIcon() {
                return mIcon;
            }

            @Override
            public IIcon getOverlayIcon() {
                return null;
            }

            @Override
            public ResourceLocation getTextureFile() {
                return TextureMap.locationBlocksTexture;
            }
        }
    }
}
