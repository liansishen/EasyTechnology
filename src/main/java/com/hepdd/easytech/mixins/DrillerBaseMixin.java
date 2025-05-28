package com.hepdd.easytech.mixins;

import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.Fluid;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import gregtech.common.tileentities.machines.multi.MTEDrillerBase;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Map;

@Mixin(value = MTEDrillerBase.class, remap = false)
public abstract class DrillerBaseMixin {


    @Shadow
    protected boolean mWorkChunkNeedsReload;
    @Shadow
    protected ChunkCoordIntPair mCurrentChunk;

    @Shadow
    protected abstract void setElectricityStats();

    @Shadow
    protected abstract boolean isEnergyEnough();
    @Shadow
    private int xDrill, yDrill, zDrill;
    @Shadow
    protected abstract boolean workingAtBottom(ItemStack aStack, int xDrill, int yDrill, int zDrill, int xPipe, int zPipe, int yHead, int oldYHead);

    @Shadow
    protected int workState;

    @Shadow
    protected abstract void setRuntimeFailureReason(@NotNull CheckRecipeResult newFailureReason);

    @Shadow
    protected abstract void setShutdownReason(@NotNull String newReason);

    @Shadow
    private CheckRecipeResult runtimeFailure;

    @Shadow
    private CheckRecipeResult lastRuntimeFailure;

    @Inject(method = "checkProcessing", at=@At("HEAD"), cancellable = true)
    public void onCheckProcessing(CallbackInfoReturnable<CheckRecipeResult> cir) {
        this.setElectricityStats();
        if (!this.isEnergyEnough()) {
            cir.setReturnValue(SimpleCheckRecipeResult.ofFailure("not_enough_energy"));
            cir.cancel();
        }
        this.workState = 1;
        boolean wasSuccessful = workingAtBottom(null,xDrill,yDrill,zDrill,0,0,0,0);
        if (this.runtimeFailure == null) {
            if (wasSuccessful) {
                this.lastRuntimeFailure = null;
            }

            cir.setReturnValue(SimpleCheckRecipeResult.ofSuccess("Drilling"));
        } else {
            final CheckRecipeResult result;
            result = lastRuntimeFailure = runtimeFailure;
            runtimeFailure = null;
            cir.setReturnValue(result);
        }
    }

    @Inject(
        method = "addUIWidgets",
        at = @At(
            value = "INVOKE",
            target = "Lcom/gtnewhorizons/modularui/api/screen/ModularWindow$Builder;widget(Lcom/gtnewhorizons/modularui/api/widget/Widget;)Lcom/gtnewhorizons/modularui/api/widget/IWidgetBuilder;",
            ordinal = 0  // 第一个 widget() 调用
        ),
        cancellable = true
    )
    private void skipWidgetBlock(CallbackInfo ci) {
        ci.cancel(); // 取消原方法执行
        // 这里可以添加你想保留的逻辑
    }

}
