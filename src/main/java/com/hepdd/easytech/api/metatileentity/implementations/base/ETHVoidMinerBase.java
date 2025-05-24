package com.hepdd.easytech.api.metatileentity.implementations.base;

import static com.hepdd.easytech.loaders.preload.ETHStatics.DimMap;
import static com.hepdd.easytech.loaders.preload.ETHStatics.getDimMap;
import static gregtech.api.enums.HatchElement.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import bwcrossmod.galacticgreg.MTEVoidMinerBase;
import bwcrossmod.galacticgreg.VoidMinerUtility;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.objects.XSTR;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import gregtech.api.util.shutdown.SimpleShutDownReason;
import gregtech.common.WorldgenGTOreLayer;
import gregtech.common.tileentities.machines.multi.MTEDrillerBase;
import gtneioreplugin.plugin.item.ItemDimensionDisplay;
import gtneioreplugin.util.DimensionHelper;

public abstract class ETHVoidMinerBase extends MTEVoidMinerBase {

    private VoidMinerUtility.DropMap dropMap = null;
    private VoidMinerUtility.DropMap extraDropMap = null;
    private float totalWeight;
    private int multiplier;
    private int oreType;
    private boolean mBlacklist = false;
    private String dimensionName = "";

    public ETHVoidMinerBase(int aID, String aName, String aNameRegional, int tier) {
        super(aID, aName, aNameRegional, tier);
    }

    public ETHVoidMinerBase(String aName, int tier) {
        super(aName, tier);
    }

    @Override
    @NotNull
    public CheckRecipeResult checkProcessing() {
        final boolean wasSuccessful;
        setElectricityStats();
        wasSuccessful = workingAtBottom();
        if (wasSuccessful) return SimpleCheckRecipeResult.ofSuccess("drilling");
        return SimpleCheckRecipeResult.ofFailure("none");
    }

    public void setMultiplier(int tier) {
        this.multiplier = tier;
    }

    public void setOreType(int type) {
        this.oreType = type;
    }

    protected boolean workingAtBottom() {
        // if the dropMap has never been initialised or if the dropMap is empty
        if (this.dropMap == null || this.totalWeight == 0) this.calculateDropMap();

        if (this.mInventory[1] != null) {
            Item item = this.mInventory[1].getItem();
            if ((!Objects.equals(this.dimensionName, "") && !(item instanceof ItemDimensionDisplay))
                || ((item instanceof ItemDimensionDisplay)
                    && !(Objects.equals(this.dimensionName, this.mInventory[1].getDisplayName())))) {
                this.calculateDropMap();
            } else if (Objects.equals(this.dimensionName, "") && item instanceof ItemDimensionDisplay) {
                this.calculateDropMap();
            }
        } else if (!Objects.equals(this.dimensionName, "")) {
            this.calculateDropMap();
        }

        if (this.totalWeight != 0.f) {
            this.handleOutputs();
            return true;
        } else {
            this.stopMachine(SimpleShutDownReason.ofNormal("无可采集物"));
            return false;
        }
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        return checkPiece(STRUCTURE_PIECE_MAIN, 1, 6, 0) && checkHatches();
    }

    private ItemStack nextOre() {
        float currentWeight = 0.f;
        while (true) {
            float randomNumber = XSTR.XSTR_INSTANCE.nextFloat() * this.totalWeight;
            for (Map.Entry<GTUtility.ItemId, Float> entry : this.dropMap.getInternalMap()
                .entrySet()) {
                currentWeight += entry.getValue();
                if (randomNumber < currentWeight) return entry.getKey()
                    .getItemStack();
            }
            for (Map.Entry<GTUtility.ItemId, Float> entry : this.extraDropMap.getInternalMap()
                .entrySet()) {
                currentWeight += entry.getValue();
                if (randomNumber < currentWeight) return entry.getKey()
                    .getItemStack();
            }
        }
    }

    /**
     * Handles the ores added manually with {@link VoidMinerUtility#addMaterialToDimensionList}
     *
     * @param id the specified dim id
     */
    private void handleExtraDrops(int id) {
        if (VoidMinerUtility.extraDropsDimMap.containsKey(id)) {
            extraDropMap = VoidMinerUtility.extraDropsDimMap.get(id);
        }
    }

    /**
     * Gets the DropMap of the dim for the specified dim id
     *
     * @param id the dim number
     */
    private void handleModDimDef(int id) {
        if (VoidMinerUtility.dropMapsByDimId.containsKey(id)) {
            this.dropMap = this.oreType == 0 ? getRawOreDropMapVanilla(id) : VoidMinerUtility.dropMapsByDimId.get(id);
        } else {
            String chunkProviderName = ((ChunkProviderServer) this.getBaseMetaTileEntity()
                .getWorld()
                .getChunkProvider()).currentChunkProvider.getClass()
                    .getName();

            if (VoidMinerUtility.dropMapsByChunkProviderName.containsKey(chunkProviderName)) {
                this.dropMap = VoidMinerUtility.dropMapsByChunkProviderName.get(chunkProviderName);
            }
        }
    }

    private void handleModDimDef(String chunkProviderName) {
        if (VoidMinerUtility.dropMapsByChunkProviderName.containsKey(chunkProviderName)) {
            this.dropMap = VoidMinerUtility.dropMapsByChunkProviderName.get(chunkProviderName);
        }
    }

    /**
     * Computes first the ores related to the dim the VM is in, then the ores added manually, then it computes the
     * totalWeight for normalisation
     */
    private void calculateDropMap() {
        this.dropMap = new VoidMinerUtility.DropMap();
        this.extraDropMap = new VoidMinerUtility.DropMap();
        if (DimMap == null) getDimMap();
        int id = 0;
        String dimName = Optional.ofNullable(this.mInventory[1])
            .filter(s -> s.getItem() instanceof ItemDimensionDisplay)
            .map(ItemDimensionDisplay::getDimension)
            .orElse("None");
        if (!dimName.equals("None")) {
            dimName = DimensionHelper.getFullName(dimName);
            this.dimensionName = this.mInventory[1].getDisplayName();
            if (NumberUtils.isNumber(DimMap.get(dimName))) {
                id = Integer.parseInt(DimMap.get(dimName));
                this.handleModDimDef(id);
                this.handleExtraDrops(id);
            } else {
                this.handleModDimDef(DimMap.get(dimName));
            }
        } else {
            this.dimensionName = this.getBaseMetaTileEntity()
                .getWorld().provider.getDimensionName();
            id = this.getBaseMetaTileEntity()
                .getWorld().provider.dimensionId;
            this.handleModDimDef(id);
            this.handleExtraDrops(id);
        }
        this.totalWeight = dropMap.getTotalWeight() + extraDropMap.getTotalWeight();
    }

    @Override
    protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
        super.drawTexts(screenElements, inventorySlot);
        // screenElements.removeChild(screenElements.getChildren().size()-1);
        screenElements.widget(
            TextWidget.dynamicString(() -> "当前采集维度：" + this.dimensionName)
                .setSynced(true)
                .setTextAlignment(Alignment.CenterLeft)
                .setDefaultColor(COLOR_TEXT_WHITE.get())
                .setEnabled(true));
    }

    /**
     * Output logic of the VM
     */
    private void handleOutputs() {
        final List<ItemStack> inputOres = this.getStoredInputs()
            .stream()
            .filter(this::isOre)
            .collect(Collectors.toList());
        final ItemStack output = this.nextOre();
        output.stackSize = multiplier;
        if (inputOres.isEmpty() || this.mBlacklist && inputOres.stream()
            .noneMatch(is -> GTUtility.areStacksEqual(is, output))
            || !this.mBlacklist && inputOres.stream()
                .anyMatch(is -> GTUtility.areStacksEqual(is, output)))
            this.addOutput(output);
        this.updateSlots();
    }

    private static final Map<Integer, Boolean> sOreTable = new HashMap<>();

    private boolean isOre(ItemStack aStack) {
        int tItem = GTUtility.stackToInt(aStack);
        if (sOreTable.containsKey(tItem)) {
            return sOreTable.get(tItem);
        }
        for (int id : OreDictionary.getOreIDs(aStack)) {
            String aName = OreDictionary.getOreName(id);
            if (aName.startsWith("ore") || aName.startsWith("raw")) {
                sOreTable.put(tItem, true);
                return true;
            }
        }
        sOreTable.put(tItem, false);
        return false;
    }

    private static VoidMinerUtility.DropMap getRawOreDropMapVanilla(int dimId) {
        VoidMinerUtility.DropMap dropMap = new VoidMinerUtility.DropMap();

        // Ore Veins
        Predicate<WorldgenGTOreLayer> oreLayerPredicate = makeOreLayerPredicate(dimId);
        WorldgenGTOreLayer.sList.stream()
            .filter(gt_worldgen -> gt_worldgen.mEnabled && oreLayerPredicate.test(gt_worldgen))
            .forEach(element -> {
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mPrimaryMeta % 1000)], 1),
                    element.mWeight);
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mSecondaryMeta % 1000)], 1),
                    element.mWeight);
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mSporadicMeta % 1000)], 1),
                    element.mWeight);
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mBetweenMeta % 1000)], 1),
                    element.mWeight);
            });

        return dropMap;
    }

    /**
     * Makes a predicate for the GT normal ore veins worldgen
     *
     * @return the predicate
     */
    private static Predicate<WorldgenGTOreLayer> makeOreLayerPredicate(int dimensionId) {
        return switch (dimensionId) {
            case -1 -> gt_worldgen -> gt_worldgen.mNether;
            case 0 -> gt_worldgen -> gt_worldgen.mOverworld;
            case 1 -> gt_worldgen -> gt_worldgen.mEnd || gt_worldgen.mEndAsteroid;
            case 7 -> gt_worldgen -> gt_worldgen.twilightForest;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    protected List<IHatchElement<? super MTEDrillerBase>> getAllowedHatches() {
        return ImmutableList.of(InputBus, OutputBus, Maintenance, Energy);
    }

    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        this.mBlacklist = !this.mBlacklist;
        GTUtility.sendChatToPlayer(aPlayer, "Mode: " + (this.mBlacklist ? "Blacklist" : "Whitelist"));
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);

    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setString("mDimension", this.dimensionName);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        this.dimensionName = aNBT.getString("mDimension");
    }
}
