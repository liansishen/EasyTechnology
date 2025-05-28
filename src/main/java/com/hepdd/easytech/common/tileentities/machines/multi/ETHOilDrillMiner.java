package com.hepdd.easytech.common.tileentities.machines.multi;

import static gregtech.api.enums.GTValues.debugDriller;
import static gregtech.common.UndergroundOil.undergroundOil;
import static gregtech.common.UndergroundOil.undergroundOilReadInformation;

import java.util.ArrayList;

import javax.annotation.Nonnegative;

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

import com.hepdd.easytech.api.objects.GTChunkManagerEx;
import com.hepdd.easytech.common.tileentities.machines.basic.ETHVoidOilLocationCard;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.GTLog;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.ValidationResult;
import gregtech.api.util.ValidationType;
import gregtech.common.tileentities.machines.multi.MTEOilDrillBase;

public class ETHOilDrillMiner extends MTEOilDrillBase {

    private final ArrayList<Chunk> mOilFieldChunks = new ArrayList<>();
    private Fluid mOil = null;
    private int mOilFlow = 0;
    private Chunk workChunk;
    private World workDim;

    public ETHOilDrillMiner(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public ETHOilDrillMiner(String aName) {
        super(aName);
    }

    @Override
    protected int getRangeInChunks() {
        return 1;
    }

    @Override
    protected ItemList getCasingBlockItem() {
        return ItemList.Casing_SolidSteel;
    }

    @Override
    protected Materials getFrameMaterial() {
        return Materials.Steel;
    }

    @Override
    protected int getCasingTextureIndex() {
        return 16;
    }

    @Override
    protected int getMinTier() {
        return 2;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        return createTooltip("I");
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHOilDrillMiner(mName);
    }

    @Override
    protected boolean workingAtBottom(ItemStack aStack, int xDrill, int yDrill, int zDrill, int xPipe, int zPipe,
        int yHead, int oldYHead) {
        setElectricityStats();
        int dimID = 0;
        if (getStoredInputs().get(0)
            .getItem() instanceof ETHVoidOilLocationCard card) {
            NBTTagCompound tag = getStoredInputs().get(0)
                .getTagCompound();
            if (tag != null) {
                dimID = tag.getInteger("dimId");
                int posX = tag.getInteger("posX");
                int posZ = tag.getInteger("posZ");
                workDim = DimensionManager.getWorld(dimID);
                workChunk = workDim.getChunkFromChunkCoords(1, 25);
            } else {
                workDim = getBaseMetaTileEntity().getWorld();
                workChunk = getBaseMetaTileEntity().getWorld()
                    .getChunkFromBlockCoords(getBaseMetaTileEntity().getXCoord(), getBaseMetaTileEntity().getZCoord());
            }
        } else {
            workDim = getBaseMetaTileEntity().getWorld();
            workChunk = getBaseMetaTileEntity().getWorld()
                .getChunkFromBlockCoords(getBaseMetaTileEntity().getXCoord(), getBaseMetaTileEntity().getZCoord());
        }

        if (tryFillChunkList()) {
            if (mWorkChunkNeedsReload) {
                mCurrentChunk = new ChunkCoordIntPair(xDrill >> 4, zDrill >> 4);
                GTChunkManagerEx.requestPlayerChunkLoad(
                    (TileEntity) getBaseMetaTileEntity(),
                    workChunk.getChunkCoordIntPair(),
                    "",
                    dimID);
                mWorkChunkNeedsReload = false;
            }
            float speed = computeSpeed();
            ValidationResult<FluidStack> pumpResult = tryPumpOil(speed);
            if (pumpResult.getType() != ValidationType.VALID) {
                mEUt = 0;
                mMaxProgresstime = 0;
                setRuntimeFailureReason(CheckRecipeResultRegistry.FLUID_OUTPUT_FULL);
                return false;
            }
            FluidStack tFluid = pumpResult.getResult();
            if (tFluid != null && tFluid.amount > getTotalConfigValue()) {
                this.mOutputFluids = new FluidStack[] { tFluid };
                return true;
            }
        }
        GTChunkManagerEx.releaseTicket((TileEntity) getBaseMetaTileEntity());
        workState = STATE_UPWARD;
        setShutdownReason(StatCollector.translateToLocal("GT5U.gui.text.drill_exhausted"));
        return true;
    }

    private boolean tryFillChunkList() {
        FluidStack tFluid, tOil;
        if (mOil == null) {
            tFluid = undergroundOilReadInformation(workChunk);
            if (tFluid == null) return false;
            mOil = tFluid.getFluid();
        }

        tOil = new FluidStack(mOil, 0);

        if (mOilFieldChunks.isEmpty()) {
            Chunk tChunk = workChunk;
            int range = 1;
            int xChunk = Math.floorDiv(tChunk.xPosition, range) * range; // Java was written by idiots. For negative
            // values, / returns rounded towards zero.
            // Fucking morons.
            int zChunk = Math.floorDiv(tChunk.zPosition, range) * range;

            for (int i = 0; i < range; i++) {
                for (int j = 0; j < range; j++) {

                    tChunk = workDim.getChunkFromChunkCoords(xChunk + i, zChunk + j);
                    tFluid = undergroundOilReadInformation(tChunk);

                    if (tOil.isFluidEqual(tFluid) && tFluid.amount > 0) {
                        mOilFieldChunks.add(tChunk);

                    }
                }
            }
        }
        return !mOilFieldChunks.isEmpty();
    }

    protected ValidationResult<FluidStack> tryPumpOil(float speed) {
        if (mOil == null) return null;
        if (debugDriller) {
            GTLog.out.println(" pump speed = " + speed);
        }

        // Even though it works fine without this check,
        // it can save tiny amount of CPU time when void protection is disabled
        if (protectsExcessFluid()) {
            FluidStack simulatedOil = pumpOil(speed, true);
            if (!canOutputAll(new FluidStack[] { simulatedOil })) {
                return ValidationResult.of(ValidationType.INVALID, null);
            }
        }

        FluidStack pumpedOil = pumpOil(speed, false);
        mOilFlow = pumpedOil.amount;
        return ValidationResult.of(ValidationType.VALID, pumpedOil.amount == 0 ? null : pumpedOil);
    }

    /**
     * @param speed    Speed to pump oil
     * @param simulate If true, it actually does not consume vein
     * @return Fluid pumped
     */
    protected FluidStack pumpOil(@Nonnegative float speed, boolean simulate) {
        if (speed < 0) {
            throw new IllegalArgumentException("Don't pass negative speed");
        }

        ArrayList<Chunk> emptyChunks = new ArrayList<>();
        FluidStack returnOil = new FluidStack(mOil, 0);

        for (Chunk tChunk : mOilFieldChunks) {
            FluidStack pumped = undergroundOil(tChunk, simulate ? -speed : speed);
            if (debugDriller) {
                GTLog.out.println(
                    " chunkX = " + tChunk.getChunkCoordIntPair().chunkXPos
                        + " chunkZ = "
                        + tChunk.getChunkCoordIntPair().chunkZPos);
                if (pumped != null) {
                    GTLog.out.println("     Fluid pumped = " + pumped.amount);
                } else {
                    GTLog.out.println("     No fluid pumped ");
                }
            }
            if (pumped == null || pumped.amount < 1) {
                emptyChunks.add(tChunk);
                continue;
            }
            if (returnOil.isFluidEqual(pumped)) {
                returnOil.amount += pumped.amount;
            }
        }
        for (Chunk tChunk : emptyChunks) {
            mOilFieldChunks.remove(tChunk);
        }
        return returnOil;
    }
}
