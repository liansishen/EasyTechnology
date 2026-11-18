package com.hepdd.easytech.mixins;

import static gregtech.common.UndergroundOil.undergroundOil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hepdd.easytech.api.objects.GTChunkManagerEx;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHVoidOilLocationCard;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.ValidationResult;
import gregtech.api.util.ValidationType;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.tileentities.machines.multi.MTEDrillerBase;
import gregtech.common.tileentities.machines.multi.MTEOilDrillBase;

@Mixin(value = MTEOilDrillBase.class, remap = false)
public abstract class OilDrillBaseMixin extends MTEDrillerBase {

    @Shadow
    protected int batchMultiplier;

    @Shadow
    @Final
    private ArrayList<ChunkCoordIntPair> mOilFieldChunks;

    @Shadow
    @Final
    private Set<Long> activeOilFieldChunkKeys;

    @Shadow
    private Fluid mOil;

    @Shadow
    private int chunkRangeConfig;

    @Shadow
    protected abstract float computeSpeed();

    @Shadow
    private ValidationResult<FluidStack> tryPumpOil(float speed) {
        throw new AssertionError();
    }

    @Shadow
    private static long packChunkKey(int chunkX, int chunkZ) {
        throw new AssertionError();
    }

    @Shadow
    public abstract void syncWorkAreaData();

    @Unique
    private boolean easyTechnology$usesLocationCard = false;

    @Unique
    private int easyTechnology$workDimId = Integer.MIN_VALUE;

    @Unique
    private int easyTechnology$cardChunkX = Integer.MIN_VALUE;

    @Unique
    private int easyTechnology$cardChunkZ = Integer.MIN_VALUE;

    protected OilDrillBaseMixin(String aName) {
        super(aName);
    }

    @Inject(method = "workingAtBottom", at = @At("HEAD"), cancellable = true)
    private void easyTechnology$onWorkingAtBottom(ItemStack aStack, int xDrill, int yDrill, int zDrill, int xPipe,
        int zPipe, int yHead, int oldYHead, CallbackInfoReturnable<Boolean> cir) {
        ItemStack cardStack = this.getControllerSlot();
        if (!ETHVoidOilLocationCard.hasValidTarget(cardStack)) {
            if (this.easyTechnology$usesLocationCard) {
                easyTechnology$clearLocationCardTarget();
            }
            return;
        }

        IGregTechTileEntity base = this.getBaseMetaTileEntity();
        if (base == null) {
            return;
        }

        NBTTagCompound tag = cardStack.getTagCompound();
        int dimId = tag.getInteger("dimId");
        int targetChunkX = tag.getInteger("posX");
        int targetChunkZ = tag.getInteger("posZ");

        if (!this.easyTechnology$usesLocationCard || dimId != this.easyTechnology$workDimId
            || targetChunkX != this.easyTechnology$cardChunkX
            || targetChunkZ != this.easyTechnology$cardChunkZ) {
            GTChunkManagerEx.releaseTicket((TileEntity) base);
            this.easyTechnology$usesLocationCard = true;
            this.easyTechnology$workDimId = dimId;
            this.easyTechnology$cardChunkX = targetChunkX;
            this.easyTechnology$cardChunkZ = targetChunkZ;
            this.mWorkChunkNeedsReload = true;
            this.mOil = null;
            this.mOilFieldChunks.clear();
            this.activeOilFieldChunkKeys.clear();
            this.syncWorkAreaData();
        }

        World targetWorld = easyTechnology$resolveTargetWorld(dimId);
        if (targetWorld != null) {
            if (this.mChunkLoadingEnabled && this.mWorkChunkNeedsReload) {
                this.mCurrentChunk = new ChunkCoordIntPair(xDrill >> 4, zDrill >> 4);
                GTChunkManagerEx.requestPlayerChunkLoad(
                    (TileEntity) base,
                    new ChunkCoordIntPair(targetChunkX, targetChunkZ),
                    "",
                    dimId);
                this.mWorkChunkNeedsReload = false;
            }

            if (easyTechnology$tryFillChunkList(targetWorld, targetChunkX, targetChunkZ)) {
                ValidationResult<FluidStack> pumpResult = this.tryPumpOil(this.computeSpeed());
                if (pumpResult.getType() != ValidationType.VALID) {
                    this.mEUt = 0;
                    this.mMaxProgresstime = 0;
                    this.setRuntimeFailureReason(CheckRecipeResultRegistry.FLUID_OUTPUT_FULL);
                    cir.setReturnValue(false);
                    return;
                }
                FluidStack tFluid = pumpResult.getResult();
                if (tFluid != null && tFluid.amount > this.getTotalConfigValue()) {
                    this.mOutputFluids = new FluidStack[] { tFluid };
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        GTChunkManagerEx.releaseTicket((TileEntity) base);
        this.mWorkChunkNeedsReload = true;
        this.setShutdownReason(StatCollector.translateToLocal("GT5U.gui.text.drill_exhausted"));
        if (yHead < yDrill) {
            this.workState = WorkState.UPWARD;
            cir.setReturnValue(true);
        } else {
            this.workState = WorkState.DOWNWARD;
            this.stopMachine(ShutDownReasonRegistry.NONE);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "pumpOil", at = @At("HEAD"), cancellable = true)
    private void easyTechnology$onPumpOil(float speed, boolean simulate, CallbackInfoReturnable<FluidStack> cir) {
        if (!this.easyTechnology$usesLocationCard) {
            return;
        }
        if (speed < 0) {
            throw new IllegalArgumentException("Don't pass negative speed");
        }

        FluidStack returnOil = new FluidStack(this.mOil, 0);
        World targetWorld = easyTechnology$resolveTargetWorld(this.easyTechnology$workDimId);
        if (targetWorld == null) {
            cir.setReturnValue(returnOil);
            return;
        }

        final float coefficient = (simulate ? -speed : speed) * this.batchMultiplier;
        boolean workAreaChanged = false;

        for (Iterator<ChunkCoordIntPair> iterator = this.mOilFieldChunks.iterator(); iterator.hasNext();) {
            ChunkCoordIntPair tChunk = iterator.next();
            FluidStack pumped = undergroundOil(targetWorld, tChunk.chunkXPos, tChunk.chunkZPos, coefficient);
            if (pumped == null || pumped.amount < 1) {
                iterator.remove();
                this.activeOilFieldChunkKeys.remove(packChunkKey(tChunk.chunkXPos, tChunk.chunkZPos));
                workAreaChanged = true;
                continue;
            }
            if (returnOil.isFluidEqual(pumped)) {
                returnOil.amount += pumped.amount;
            }
        }

        if (workAreaChanged) {
            this.syncWorkAreaData();
        }

        cir.setReturnValue(returnOil);
    }

    @Override
    public void onUnload() {
        IGregTechTileEntity base = this.getBaseMetaTileEntity();
        if (base instanceof TileEntity tileEntity && GTChunkManagerEx.releaseTicket(tileEntity)) {
            this.mWorkChunkNeedsReload = true;
        }
        super.onUnload();
    }

    @Unique
    private boolean easyTechnology$tryFillChunkList(World targetWorld, int targetChunkX, int targetChunkZ) {
        FluidStack tFluid;
        if (this.mOil == null) {
            tFluid = undergroundOil(targetWorld, targetChunkX, targetChunkZ, -1);
            if (tFluid == null || tFluid.getFluid() == null) {
                return false;
            }
            this.mOil = tFluid.getFluid();
        }

        FluidStack tOil = new FluidStack(this.mOil, 0);
        if (this.mOilFieldChunks.isEmpty()) {
            int range = Math.max(1, this.chunkRangeConfig);
            int xChunk = Math.floorDiv(targetChunkX, range) * range;
            int zChunk = Math.floorDiv(targetChunkZ, range) * range;
            boolean addedAny = false;

            for (int i = 0; i < range; i++) {
                for (int j = 0; j < range; j++) {
                    int cx = xChunk + i;
                    int cz = zChunk + j;
                    tFluid = undergroundOil(targetWorld, cx, cz, -1);
                    if (tFluid != null && tOil.isFluidEqual(tFluid) && tFluid.amount > 0) {
                        this.mOilFieldChunks.add(new ChunkCoordIntPair(cx, cz));
                        this.activeOilFieldChunkKeys.add(packChunkKey(cx, cz));
                        addedAny = true;
                    }
                }
            }
            if (addedAny) {
                this.syncWorkAreaData();
            }
        }
        return !this.mOilFieldChunks.isEmpty();
    }

    @Unique
    private void easyTechnology$clearLocationCardTarget() {
        IGregTechTileEntity base = this.getBaseMetaTileEntity();
        if (base instanceof TileEntity tileEntity) {
            GTChunkManagerEx.releaseTicket(tileEntity);
        }
        this.easyTechnology$usesLocationCard = false;
        this.easyTechnology$workDimId = Integer.MIN_VALUE;
        this.easyTechnology$cardChunkX = Integer.MIN_VALUE;
        this.easyTechnology$cardChunkZ = Integer.MIN_VALUE;
        this.mWorkChunkNeedsReload = true;
        this.mOil = null;
        this.mOilFieldChunks.clear();
        this.activeOilFieldChunkKeys.clear();
        if (this.workState == WorkState.AT_BOTTOM) {
            this.workState = WorkState.DOWNWARD;
        }
        this.syncWorkAreaData();
    }

    @Unique
    private static World easyTechnology$resolveTargetWorld(int dimId) {
        World world = DimensionManager.getWorld(dimId);
        if (world == null && DimensionManager.isDimensionRegistered(dimId)) {
            DimensionManager.initDimension(dimId);
            world = DimensionManager.getWorld(dimId);
        }
        return world;
    }
}
