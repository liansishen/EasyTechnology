package com.hepdd.easytech.mixins;

import com.hepdd.easytech.api.objects.GTChunkManagerEx;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHVoidOilLocationCard;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.objects.GTChunkManager;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.ValidationResult;
import gregtech.api.util.ValidationType;
import gregtech.common.tileentities.machines.multi.MTEDrillerBase;
import gregtech.common.tileentities.machines.multi.MTEOilDrillBase;
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

import java.util.ArrayList;

import static gregtech.common.UndergroundOil.undergroundOilReadInformation;

@Mixin(value = MTEOilDrillBase.class, remap = false)
public abstract class OilDrillBaseMixin extends DrillerBaseMixin {

    @Shadow
    protected abstract float computeSpeed();

    @Shadow
    protected abstract ValidationResult<FluidStack> tryPumpOil(float speed);
    @Shadow
    private final ArrayList<Chunk> mOilFieldChunks = new ArrayList<>();
    @Shadow
    private Fluid mOil = null;
    @Unique
    private Chunk easyTechnology$workChunk;
    @Unique
    private World easyTechnology$workDim;
    private int mOilFlow = 0;

    @Inject(method = "workingAtBottom", at=@At("HEAD"),cancellable = true)
    private void onWorkAtbottom(ItemStack aStack, int xDrill, int yDrill, int zDrill, int xPipe, int zPipe, int yHead, int oldYHead, CallbackInfoReturnable<Boolean> cir) {
        int dimID = 0;
        IGregTechTileEntity gregTechTile = ((MTEDrillerBase)(Object)this).getBaseMetaTileEntity();
        setElectricityStats();
        ItemStack is = ((MTEDrillerBase)(Object)this).getStackInSlot(1);
        if (is != null) {
            if (is.getItem() instanceof ETHVoidOilLocationCard) {
                NBTTagCompound tag = is.getTagCompound();
                if (tag != null) {
                    dimID = tag.getInteger("dimId");
                    int posX = tag.getInteger("posX");
                    int posZ = tag.getInteger("posZ");
                    easyTechnology$workDim = DimensionManager.getWorld(dimID);
                    easyTechnology$workChunk = easyTechnology$workDim.getChunkFromChunkCoords(posX, posZ);
                } else {
                    easyTechnology$workDim = gregTechTile.getWorld();
                    easyTechnology$workChunk = gregTechTile.getWorld()
                        .getChunkFromBlockCoords(gregTechTile.getXCoord(), gregTechTile.getZCoord());
                }
            }
        } else {
            easyTechnology$workDim = gregTechTile.getWorld();
            easyTechnology$workChunk = gregTechTile.getWorld()
                .getChunkFromBlockCoords(gregTechTile.getXCoord(), gregTechTile.getZCoord());
        }

        if (easyTechnology$onTryFillChunkList()) {
            if (mWorkChunkNeedsReload) {
                mCurrentChunk = new ChunkCoordIntPair(xDrill >> 4, zDrill >> 4);
                GTChunkManagerEx.requestPlayerChunkLoad((TileEntity) gregTechTile, null,"");
                mWorkChunkNeedsReload = false;
            }

            float speed = this.computeSpeed();
            ValidationResult<FluidStack> pumpResult = this.tryPumpOil(speed);
            if (pumpResult.getType() != ValidationType.VALID) {
                this.setRuntimeFailureReason(CheckRecipeResultRegistry.FLUID_OUTPUT_FULL);
                cir.setReturnValue(false);
                return;
            }
            FluidStack tFluid = pumpResult.getResult();
            if (tFluid != null && tFluid.amount > ((MTEDrillerBase)(Object)this).getTotalConfigValue()) {
                ((MTEDrillerBase)(Object)this).mOutputFluids = new FluidStack[] { tFluid };
                cir.setReturnValue(true);
                return;
            }
        }
        GTChunkManagerEx.releaseTicket((TileEntity) gregTechTile);
        workState = 2;
        this.setShutdownReason(StatCollector.translateToLocal("GT5U.gui.text.drill_exhausted"));
        cir.setReturnValue(true);
    }


    @Unique
    private boolean easyTechnology$onTryFillChunkList() {
        FluidStack tFluid, tOil;
        if (mOil == null) {
            tFluid = undergroundOilReadInformation(easyTechnology$workChunk);
            if (tFluid == null) {
                return false;
            }
            mOil = tFluid.getFluid();
        }

        tOil = new FluidStack(mOil, 0);

        if (mOilFieldChunks.isEmpty()) {
            Chunk tChunk = easyTechnology$workChunk;
            int range = 1;
            int xChunk = Math.floorDiv(tChunk.xPosition, range) * range; // Java was written by idiots. For negative
            // values, / returns rounded towards zero.
            // Fucking morons.
            int zChunk = Math.floorDiv(tChunk.zPosition, range) * range;

            for (int i = 0; i < range; i++) {
                for (int j = 0; j < range; j++) {

                    tChunk = easyTechnology$workDim.getChunkFromChunkCoords(xChunk + i, zChunk + j);
                    tFluid = undergroundOilReadInformation(tChunk);

                    if (tOil.isFluidEqual(tFluid) && tFluid.amount > 0) {
                        mOilFieldChunks.add(tChunk);

                    }
                }
            }
        }
        return !mOilFieldChunks.isEmpty();
    }
}
