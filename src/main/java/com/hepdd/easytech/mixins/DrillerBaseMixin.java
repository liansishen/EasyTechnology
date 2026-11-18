package com.hepdd.easytech.mixins;

import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hepdd.easytech.api.objects.GTChunkManagerEx;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHVoidOilLocationCard;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.common.tileentities.machines.multi.MTEDrillerBase;
import gregtech.common.tileentities.machines.multi.MTEDrillerBase.WorkState;
import gregtech.common.tileentities.machines.multi.MTEOilDrillBase;

@Mixin(value = MTEDrillerBase.class, remap = false)
public abstract class DrillerBaseMixin extends MTEEnhancedMultiBlockBase<MTEDrillerBase> {

    @Shadow
    private int yDrill;

    @Shadow
    private int yHead;

    @Shadow
    protected WorkState workState;

    @Shadow
    protected boolean mWorkChunkNeedsReload;

    @Shadow
    protected abstract void setElectricityStats();

    protected DrillerBaseMixin(String aName) {
        super(aName);
    }

    @Unique
    private boolean easyTechnology$isVoidOilCardActive() {
        return ((Object) this) instanceof MTEOilDrillBase
            && ETHVoidOilLocationCard.hasValidTarget(this.getControllerSlot());
    }

    @Inject(method = "requiresMiningPipes", at = @At("HEAD"), cancellable = true)
    private void easyTechnology$onRequiresMiningPipes(CallbackInfoReturnable<Boolean> cir) {
        if (!easyTechnology$isVoidOilCardActive()) {
            return;
        }
        // Allow retracting any legacy pipes deployed before the location card was inserted
        if ((this.workState == WorkState.UPWARD || this.workState == WorkState.ABORT) && this.yHead < this.yDrill) {
            return;
        }
        cir.setReturnValue(false);
    }

    @Inject(
        method = "checkProcessing",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/common/tileentities/machines/multi/MTEDrillerBase;requiresMiningPipes()Z"))
    private void easyTechnology$enterBottomWhenUsingVoidCard(CallbackInfoReturnable<CheckRecipeResult> cir) {
        if (easyTechnology$isVoidOilCardActive() && this.workState == WorkState.DOWNWARD && this.yHead == this.yDrill) {
            this.workState = WorkState.AT_BOTTOM;
            this.setElectricityStats();
        }
    }

    @Inject(method = "onRemoval", at = @At("HEAD"))
    private void easyTechnology$onRemoval(CallbackInfo ci) {
        easyTechnology$releaseCustomChunkTicket();
    }

    @Inject(method = "onAbort", at = @At("HEAD"))
    private void easyTechnology$onAbort(CallbackInfo ci) {
        if (easyTechnology$releaseCustomChunkTicket()) {
            this.mWorkChunkNeedsReload = true;
        }
    }

    @Inject(method = "onPostTick", at = @At("TAIL"))
    private void easyTechnology$onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick, CallbackInfo ci) {
        if (aBaseMetaTileEntity != null && aBaseMetaTileEntity.isServerSide()
            && !aBaseMetaTileEntity.isAllowedToWork()) {
            if (GTChunkManagerEx.releaseTicket((TileEntity) aBaseMetaTileEntity)) {
                this.mWorkChunkNeedsReload = true;
            }
        }
    }

    @Override
    public void inValidate() {
        easyTechnology$releaseCustomChunkTicket();
        super.inValidate();
    }

    @Unique
    protected boolean easyTechnology$releaseCustomChunkTicket() {
        IGregTechTileEntity base = this.getBaseMetaTileEntity();
        if (base instanceof TileEntity tileEntity) {
            return GTChunkManagerEx.releaseTicket(tileEntity);
        }
        return false;
    }
}
