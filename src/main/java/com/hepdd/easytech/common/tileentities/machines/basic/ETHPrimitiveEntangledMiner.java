package com.hepdd.easytech.common.tileentities.machines.basic;

import static gregtech.api.enums.Mods.GregTech;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.modularui.api.drawable.FallbackableUITexture;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.ProgressBar;
import com.gtnewhorizons.modularui.common.widget.ProgressBar.Direction;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import gregtech.api.enums.SteamVariant;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.gui.modularui.GUITextureSet;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.BasicUIProperties;

public class ETHPrimitiveEntangledMiner extends ETHAbstractEntangledMiner {

    private static final int EU_PER_BURN_TICK = 10;
    private static final int BURN_SCALE_NUMERATOR = 640;
    private static final int BURN_SCALE_DENOMINATOR = 1600;

    private static final FallbackableUITexture PROGRESSBAR_TEXTURE = GTUITextures.fallbackableProgressbar(
        "miner",
        UITexture.fullImage(GregTech.ID, "gui/progressbar/arrow_2_primitive"));

    public ETHPrimitiveEntangledMiner(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 0, 1, 2,
            new String[] { "Requires an Entangled Card", "Mines real ore blocks at the recorded target" });
    }

    public ETHPrimitiveEntangledMiner(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures, 1, 2);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aBaseTileEntity) {
        return new ETHPrimitiveEntangledMiner(mName, mTier, mDescriptionArray, mTextures);
    }

    // ==================== Inventory ====================

    private ItemStack fuelSlot;
    private int maxBurnTicks = 0;

    @Override
    protected boolean allowPutStackValidated(IGregTechTileEntity aBaseMetaTileEntity, int aIndex,
        net.minecraftforge.common.util.ForgeDirection aSide, ItemStack aStack) {
        if (aIndex == getInputSlot()) return aStack != null && aStack.getItem() instanceof ETHEntangledCard;
        return false;
    }

    public boolean isFuelItem(ItemStack aStack) {
        return aStack != null && TileEntityFurnace.getItemBurnTime(aStack) > 0;
    }

    // ==================== Energy ====================

    @Override
    protected boolean hasEnoughEnergy(IGregTechTileEntity aBaseMetaTileEntity, int aRequiredEU) {
        while ((long) burnTime * EU_PER_BURN_TICK < aRequiredEU) {
            int fuelValue = TileEntityFurnace.getItemBurnTime(fuelSlot);
            if (fuelValue <= 0) return false;
            fuelValue = fuelValue * BURN_SCALE_NUMERATOR / BURN_SCALE_DENOMINATOR;
            if (fuelValue <= 0) fuelValue = 1;
            fuelSlot.stackSize--;
            if (fuelSlot.stackSize <= 0) fuelSlot = null;
            burnTime += fuelValue;
            maxBurnTicks = burnTime;
        }
        return true;
    }

    @Override
    protected void consumeEnergy(IGregTechTileEntity aBaseMetaTileEntity, int aEU) {
        burnTime -= aEU / EU_PER_BURN_TICK;
    }

    @Override
    protected long getAvailableEnergy(IGregTechTileEntity aBaseMetaTileEntity) {
        return (long) burnTime * EU_PER_BURN_TICK;
    }

    @Override
    protected String getEnergyDisplayString() {
        return StatCollector.translateToLocalFormatted("easytech.machine.entangled_miner.fuel", burnTime / 20);
    }

    // ==================== NBT ====================

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("ETHMaxBurnTicks", maxBurnTicks);
        if (fuelSlot != null) {
            NBTTagCompound fuelTag = new NBTTagCompound();
            fuelSlot.writeToNBT(fuelTag);
            aNBT.setTag("ETHFuelSlot", fuelTag);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        maxBurnTicks = aNBT.getInteger("ETHMaxBurnTicks");
        if (aNBT.hasKey("ETHFuelSlot")) {
            fuelSlot = ItemStack.loadItemStackFromNBT(aNBT.getCompoundTag("ETHFuelSlot"));
        }
    }

    // ==================== GUI ====================

    @Override
    public GUITextureSet getGUITextureSet() {
        return GUITextureSet.STEAM.apply(SteamVariant.PRIMITIVE);
    }

    @Override
    protected BasicUIProperties getUIProperties() {
        return super.getUIProperties().toBuilder()
            .progressBarTexture(PROGRESSBAR_TEXTURE)
            .build();
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder aBuilder, UIBuildContext aContext) {
        super.addUIWidgets(aBuilder, aContext);

        // Fuel progress bar (flame, top to bottom, 16x16)
        aBuilder.widget(
            new ProgressBar()
                .setProgress(() -> {
                    if (burnTime <= 0) return 0f;
                    int maxFuel = maxBurnTicks;
                    return maxFuel > 0 ? Math.min(1.0f, (float) burnTime / maxFuel) : 0f;
                })
                .setTexture(UITexture.fullImage(GregTech.ID, "gui/progressbar/fuel"), 16)
                .setDirection(Direction.UP)
                .setPos(new Pos2d(7, 25))
                .setSize(new Size(16, 16)));

        IDrawable[] background = new IDrawable[] { getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_FURNACE };

        // Fuel slot (left of card slot, 15px gap)
        aBuilder.widget(
            new SlotWidget(new FuelSlotHandler(this), 0)
                .setPos(new Pos2d(28, 24))
                .setSize(new Size(18, 18))
                .setBackground(background));

    }

    // ==================== Fuel Slot Handler ====================
    @Desugar
    private record FuelSlotHandler(ETHPrimitiveEntangledMiner machine) implements IItemHandlerModifiable {

        @Override
            public ItemStack getStackInSlot(int aSlot) {
                if (aSlot != 0) return null;
                return machine.fuelSlot;
            }

            @Override
            public void setStackInSlot(int aSlot, ItemStack aStack) {
                if (aSlot != 0) return;
                machine.fuelSlot = aStack;
            }

            @Override
            public int getSlots() {
                return 1;
            }

            @Override
            public ItemStack insertItem(int aSlot, ItemStack aStack, boolean aSimulate) {
                if (aSlot != 0 || aStack == null) return aStack;
                if (!machine.isFuelItem(aStack)) return aStack;
                ItemStack existing = machine.fuelSlot;
                if (existing == null) {
                    if (!aSimulate) {
                        machine.fuelSlot = aStack.copy();
                    }
                    return null;
                }
                if (!aStack.isItemEqual(existing) || !ItemStack.areItemStackTagsEqual(aStack, existing)) return aStack;
                int space = existing.getMaxStackSize() - existing.stackSize;
                if (space <= 0) return aStack;
                int toAdd = Math.min(aStack.stackSize, space);
                if (!aSimulate) {
                    existing.stackSize += toAdd;
                }
                if (toAdd >= aStack.stackSize) return null;
                ItemStack remainder = aStack.copy();
                remainder.stackSize -= toAdd;
                return remainder;
            }

            @Override
            public ItemStack extractItem(int aSlot, int aAmount, boolean aSimulate) {
                if (aSlot != 0 || aAmount <= 0) return null;
                ItemStack existing = machine.fuelSlot;
                if (existing == null) return null;
                int toExtract = Math.min(aAmount, existing.stackSize);
                ItemStack extracted = existing.copy();
                extracted.stackSize = toExtract;
                if (!aSimulate) {
                    existing.stackSize -= toExtract;
                    if (existing.stackSize <= 0) machine.fuelSlot = null;
                }
                return extracted;
            }

            @Override
            public int getSlotLimit(int aSlot) {
                return 64;
            }
        }
}
