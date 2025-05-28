package com.hepdd.easytech.mixins;

import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.common.tileentities.machines.multi.MTEDrillerBase;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MTEDrillerBase.class)
public abstract class DrillerBaseMixin {


    @Shadow
    protected abstract void setElectricityStats();

    @Shadow
    protected abstract boolean workingAtBottom(ItemStack aStack, int xDrill, int yDrill, int zDrill, int xPipe, int zPipe, int yHead, int oldYHead);

    @Shadow
    public CheckRecipeResult checkProcessing() {
        setElectricityStats();
        boolean wasSuccessful = workingAtBottom(null,0,0,0,0,0,0,0);
        return SimpleCheckRecipeResult.ofSuccess("Drilling");
    }

}
