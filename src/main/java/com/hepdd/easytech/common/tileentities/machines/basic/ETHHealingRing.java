package com.hepdd.easytech.common.tileentities.machines.basic;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import gregtech.api.items.GTGenericItem;

public class ETHHealingRing extends GTGenericItem implements IBauble {

    private static final String TEXTURE = "easytechnology:healing_ring";
    private static final int FEED_INTERVAL_TICKS = 40;

    public ETHHealingRing(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
        super(aUnlocalized, aEnglish, aEnglishTooltip);
        setMaxStackSize(1);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        mIcon = iconRegister.registerIcon(TEXTURE);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.RING;
    }

    @Override
    public void onWornTick(ItemStack itemStack, EntityLivingBase wearer) {
        if (!(wearer instanceof EntityPlayer player)) return;
        if (player.worldObj.isRemote || player.worldObj.getTotalWorldTime() % FEED_INTERVAL_TICKS != 0) return;
        player.getFoodStats()
            .addStats(1, 0.2F);
    }

    @Override
    public void onEquipped(ItemStack itemStack, EntityLivingBase wearer) {}

    @Override
    public void onUnequipped(ItemStack itemStack, EntityLivingBase wearer) {}

    @Override
    public boolean canEquip(ItemStack itemStack, EntityLivingBase wearer) {
        return true;
    }

    @Override
    public boolean canUnequip(ItemStack itemStack, EntityLivingBase wearer) {
        return true;
    }

    @Override
    protected void addAdditionalToolTips(List<String> aList, ItemStack aStack, EntityPlayer aPlayer) {
        super.addAdditionalToolTips(aList, aStack, aPlayer);
        aList.add(StatCollector.translateToLocal("easytech.tooltip.healing_ring.usage"));
        aList.add(StatCollector.translateToLocal("easytech.tooltip.healing_ring.effect"));
    }
}
