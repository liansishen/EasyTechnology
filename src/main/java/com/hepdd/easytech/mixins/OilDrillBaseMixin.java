package com.hepdd.easytech.mixins;

import static gregtech.common.UndergroundOil.undergroundOil;
import static gregtech.common.UndergroundOil.undergroundOilReadInformation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hepdd.easytech.api.objects.GTChunkManagerEx;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHVoidOilLocationCard;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ValidationResult;
import gregtech.api.util.ValidationType;
import gregtech.common.tileentities.machines.multi.MTEDrillerBase;
import gregtech.common.tileentities.machines.multi.MTEOilDrillBase;

@Mixin(value = MTEOilDrillBase.class, remap = false)
public abstract class OilDrillBaseMixin extends DrillerBaseMixin {

    @Shadow
    protected abstract float computeSpeed();

    @Shadow
    protected abstract FluidStack adjustPumpedOil(FluidStack oil);

    @Shadow
    private final ArrayList<ChunkCoordIntPair> mOilFieldChunks = new ArrayList<>();
    @Shadow
    private final Set<Long> activeOilFieldChunkKeys = new HashSet<>();
    @Shadow
    private Fluid mOil = null;
    @Shadow
    private int mOilFlow;
    @Unique
    private ChunkCoordIntPair easyTechnology$workChunk;
    @Unique
    private World easyTechnology$workDim;
    @Unique
    private ItemStack easyTechnology$workLocationCard;

    @Inject(method = "workingAtBottom", at = @At("HEAD"), cancellable = true)
    private void onWorkAtbottom(ItemStack aStack, int xDrill, int yDrill, int zDrill, int xPipe, int zPipe, int yHead,
        int oldYHead, CallbackInfoReturnable<Boolean> cir) {
        IGregTechTileEntity gregTechTile = ((MTEDrillerBase) (Object) this).getBaseMetaTileEntity();
        setElectricityStats();
        ItemStack is = ((MTEDrillerBase) (Object) this).getStackInSlot(1);
        if (GTUtility.isStackValid(is) && is.getItem() instanceof ETHVoidOilLocationCard) {
            if (easyTechnology$workLocationCard == null
                || !ItemStack.areItemStackTagsEqual(is, easyTechnology$workLocationCard)) {
                easyTechnology$workLocationCard = is.copy();
                NBTTagCompound tag = is.getTagCompound();
                if (tag != null) {
                    int dimID = tag.getInteger("dimId");
                    int posX = tag.getInteger("posX");
                    int posZ = tag.getInteger("posZ");
                    easyTechnology$workDim = DimensionManager.getWorld(dimID);
                    easyTechnology$workChunk = new ChunkCoordIntPair(posX, posZ);
                    mOil = null;
                    easyTechnology$clearOilFieldChunks();
                }
            }
        } else {
            if (easyTechnology$workLocationCard != null) {
                easyTechnology$workLocationCard = null;
                easyTechnology$workDim = null;
                mOil = null;
                easyTechnology$clearOilFieldChunks();
            }
        }
        if (easyTechnology$workDim == null) {
            easyTechnology$workDim = gregTechTile.getWorld();
            easyTechnology$workChunk = new ChunkCoordIntPair(
                gregTechTile.getXCoord() >> 4,
                gregTechTile.getZCoord() >> 4);
        }

        if (easyTechnology$onTryFillChunkList()) {
            if (mWorkChunkNeedsReload) {
                mCurrentChunk = new ChunkCoordIntPair(xDrill >> 4, zDrill >> 4);
                GTChunkManagerEx.requestPlayerChunkLoad(
                    (TileEntity) gregTechTile,
                    easyTechnology$workChunk,
                    "",
                    easyTechnology$workDim.provider.dimensionId);
                mWorkChunkNeedsReload = false;
            }

            float speed = this.computeSpeed();
            ValidationResult<FluidStack> pumpResult = easyTechnology$tryPumpOil(speed);
            if (pumpResult.getType() != ValidationType.VALID) {
                this.setRuntimeFailureReason(CheckRecipeResultRegistry.FLUID_OUTPUT_FULL);
                cir.setReturnValue(false);
                return;
            }
            FluidStack tFluid = pumpResult.getResult();
            if (tFluid != null && tFluid.amount > ((MTEDrillerBase) (Object) this).getTotalConfigValue()) {
                ((MTEDrillerBase) (Object) this).mOutputFluids = new FluidStack[] { tFluid };
                cir.setReturnValue(true);
                return;
            }
        }
        GTChunkManagerEx.releaseTicket((TileEntity) gregTechTile);
        setWorkState(2);
        this.setShutdownReason(StatCollector.translateToLocal("GT5U.gui.text.drill_exhausted"));
        cir.setReturnValue(true);
    }

    @Unique
    private boolean easyTechnology$onTryFillChunkList() {
        FluidStack tFluid, tOil;
        if (mOil == null) {
            tFluid = undergroundOilReadInformation(easyTechnology$getWorkChunk());
            if (tFluid == null) {
                return false;
            }
            mOil = tFluid.getFluid();
        }

        tOil = new FluidStack(mOil, 0);

        if (mOilFieldChunks.isEmpty()) {
            int range = 1;
            int xChunk = Math.floorDiv(easyTechnology$workChunk.chunkXPos, range) * range; // Java was written by
                                                                                           // idiots. For negative
            // values, / returns rounded towards zero.
            // Fucking morons.
            int zChunk = Math.floorDiv(easyTechnology$workChunk.chunkZPos, range) * range;

            for (int i = 0; i < range; i++) {
                for (int j = 0; j < range; j++) {

                    ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(xChunk + i, zChunk + j);
                    Chunk tChunk = easyTechnology$workDim
                        .getChunkFromChunkCoords(chunkCoord.chunkXPos, chunkCoord.chunkZPos);
                    tFluid = undergroundOilReadInformation(tChunk);

                    if (tFluid != null && tOil.isFluidEqual(tFluid) && tFluid.amount > 0) {
                        mOilFieldChunks.add(chunkCoord);
                        activeOilFieldChunkKeys
                            .add(easyTechnology$packChunkKey(chunkCoord.chunkXPos, chunkCoord.chunkZPos));
                    }
                }
            }
        }
        return !mOilFieldChunks.isEmpty();
    }

    @Unique
    private ValidationResult<FluidStack> easyTechnology$tryPumpOil(float speed) {
        if (mOil == null) return ValidationResult.of(ValidationType.VALID, null);

        if (((MTEOilDrillBase) (Object) this).supportsVoidProtection()) {
            FluidStack simulatedOil = adjustPumpedOil(easyTechnology$pumpOil(speed, true));
            if (!easyTechnology$canOutputAll(new FluidStack[] { simulatedOil })) {
                return ValidationResult.of(ValidationType.INVALID, null);
            }
        }

        FluidStack pumpedOil = adjustPumpedOil(easyTechnology$pumpOil(speed, false));
        mOilFlow = pumpedOil.amount;
        return ValidationResult.of(ValidationType.VALID, pumpedOil.amount == 0 ? null : pumpedOil);
    }

    @Unique
    private FluidStack easyTechnology$pumpOil(float speed, boolean simulate) {
        if (speed < 0) {
            throw new IllegalArgumentException("Don't pass negative speed");
        }

        FluidStack result = new FluidStack(mOil, 0);
        for (ChunkCoordIntPair chunkCoord : mOilFieldChunks) {
            FluidStack pumped = undergroundOil(
                easyTechnology$workDim,
                chunkCoord.chunkXPos,
                chunkCoord.chunkZPos,
                simulate ? -speed : speed);
            if (pumped != null && pumped.isFluidEqual(result)) {
                result.amount += pumped.amount;
            }
        }
        return result;
    }

    @Unique
    private boolean easyTechnology$canOutputAll(FluidStack[] fluids) {
        try {
            Method method = MTEMultiBlockBase.class.getDeclaredMethod("canOutputAll", FluidStack[].class);
            method.setAccessible(true);
            return (boolean) method.invoke(this, (Object) fluids);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to check oil drill fluid output", e);
        }
    }

    @Unique
    private Chunk easyTechnology$getWorkChunk() {
        return easyTechnology$workDim
            .getChunkFromChunkCoords(easyTechnology$workChunk.chunkXPos, easyTechnology$workChunk.chunkZPos);
    }

    @Unique
    private void easyTechnology$clearOilFieldChunks() {
        mOilFieldChunks.clear();
        activeOilFieldChunkKeys.clear();
    }

    @Unique
    private static Long easyTechnology$packChunkKey(int chunkX, int chunkZ) {
        return (long) chunkX & 0xffffffffL | ((long) chunkZ & 0xffffffffL) << 32;
    }
}
