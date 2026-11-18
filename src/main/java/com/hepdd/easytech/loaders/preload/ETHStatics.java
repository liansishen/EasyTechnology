package com.hepdd.easytech.loaders.preload;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import com.hepdd.easytech.common.ETHNetwork;

public class ETHStatics {

    public static ETHNetwork NW;

    public static final String AuthorEasyTech = "Author: " + EnumChatFormatting.BLUE
        + EnumChatFormatting.BOLD
        + "Easy"
        + EnumChatFormatting.AQUA
        + EnumChatFormatting.BOLD
        + "Technology";

    public static final String AuthorEasyTechForItem = StatCollector.translateToLocal("GT5U.MBTT.Mod") + ":"
        + EnumChatFormatting.BLUE
        + EnumChatFormatting.BOLD
        + "Easy"
        + EnumChatFormatting.AQUA
        + EnumChatFormatting.BOLD
        + "Technology";
}
