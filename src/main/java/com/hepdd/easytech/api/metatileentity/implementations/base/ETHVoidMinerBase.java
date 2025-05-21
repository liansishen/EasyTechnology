package com.hepdd.easytech.api.metatileentity.implementations.base;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static gregtech.api.enums.GTValues.VN;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.NotNull;

import com.github.bsideup.jabel.Desugar;
import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import bwcrossmod.galacticgreg.VoidMinerUtility;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.gui.widgets.LockedWhileActiveButton;
import gregtech.api.interfaces.IChunkLoader;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.objects.GTChunkManager;
import gregtech.api.objects.XSTR;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.WorldgenGTOreLayer;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.GTPPMultiBlockBase;

public abstract class ETHVoidMinerBase extends GTPPMultiBlockBase<ETHVoidMinerBase>
    implements IChunkLoader, ISurvivalConstructable {

    public ETHVoidMinerBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        initFields();
    }

    public ETHVoidMinerBase(String aName) {
        super(aName);
        initFields();
    }

    protected static final String STRUCTURE_PIECE_MAIN = "main";
    protected static final ClassValue<IStructureDefinition<ETHVoidMinerBase>> STRUCTURE_DEFINITION = new ClassValue<>() {

        @Override
        protected IStructureDefinition<ETHVoidMinerBase> computeValue(Class<?> type) {
            return StructureDefinition.<ETHVoidMinerBase>builder()
                .addShape(
                    STRUCTURE_PIECE_MAIN,
                    transpose(
                        new String[][] { { "   ", " f ", "   " }, { "   ", " f ", "   " }, { "   ", " f ", "   " },
                            { " f ", "fcf", " f " }, { " f ", "fcf", " f " }, { " f ", "fcf", " f " },
                            { "b~b", "bbb", "bbb" }, }))
                .addElement('f', lazy(t -> ofFrame(t.getFrameMaterial())))
                .addElement(
                    'c',
                    lazy(
                        t -> ofBlock(
                            t.getCasingBlockItem()
                                .getBlock(),
                            t.getCasingBlockItem()
                                .get(0)
                                .getItemDamage())))
                .addElement(
                    'b',
                    lazy(
                        t -> buildHatchAdder(ETHVoidMinerBase.class).atLeastList(t.getAllowedHatches())
                            .adder(ETHVoidMinerBase::addToMachineList)
                            .casingIndex(t.casingTextureIndex)
                            .dot(1)
                            .buildAndChain(
                                t.getCasingBlockItem()
                                    .getBlock(),
                                t.getCasingBlockItem()
                                    .get(0)
                                    .getItemDamage())))
                .build();
        }
    };
    private VoidMinerUtility.DropMap dropMap = null;
    private VoidMinerUtility.DropMap extraDropMap = null;
    private float totalWeight;
    private int multiplier = 1;
    private int oreType = 0; // 0: rawOre 1: OreBlock

    private boolean mBlacklist = false;

    protected int casingTextureIndex;
    protected int workState;
    protected static final int STATE_AT_BOTTOM = 1, STATE_ABORT = 3;

    protected boolean mChunkLoadingEnabled = true;
    protected ChunkCoordIntPair mCurrentChunk = null;
    protected boolean mWorkChunkNeedsReload = true;

    private final Map<ResultRegistryKey, CheckRecipeResult> resultRegistry = new HashMap<>();

    /** Allows inheritors to supply custom shutdown failure messages. */
    private @NotNull String shutdownReason = "";

    @Override
    @NotNull
    public CheckRecipeResult checkProcessing() {
        final boolean wasSuccessful;
        setElectricityStats();
        wasSuccessful = workingAtBottom();
        if (wasSuccessful) workState = STATE_AT_BOTTOM;
        return resultRegistry
            .getOrDefault(new ResultRegistryKey(workState, wasSuccessful), SimpleCheckRecipeResult.ofFailure("none"));
    }

    public void setMultiplier(int tier) {
        this.multiplier = tier;
    }

    public void setOreType(int type) {
        this.oreType = type;
    }

    public abstract Materials getFrameMaterial();

    public abstract ItemList getCasingBlockItem();

    public abstract void setElectricityStats();

    public abstract int getCasingTextureIndex();

    protected boolean workingAtBottom() {
        // if the dropMap has never been initialised or if the dropMap is empty
        if (this.dropMap == null || this.totalWeight == 0) this.calculateDropMap();

        if (this.totalWeight != 0.f) {
            this.handleOutputs();
            return true;
        } else {
            this.stopMachine(ShutDownReasonRegistry.NONE);
            return false;
        }
    }

    private void initFields() {
        casingTextureIndex = getCasingTextureIndex();

        addOperatingMessages();
    }

    protected void addOperatingMessages() {
        // Inheritors can overwrite these to add custom operating messages.
        addResultMessage(STATE_AT_BOTTOM, true, "drilling");
        addResultMessage(STATE_AT_BOTTOM, false, "no_mining_pipe");
        addResultMessage(STATE_ABORT, true, "retracting_pipe");
        addResultMessage(STATE_ABORT, false, "drill_retract_pipes_finished");
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection sideDirection,
        ForgeDirection facingDirection, int colorIndex, boolean active, boolean redstoneLevel) {
        if (sideDirection == facingDirection) {
            if (active) return new ITexture[] { getCasingTextureForId(casingTextureIndex), TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_ORE_DRILL_ACTIVE)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ORE_DRILL_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { getCasingTextureForId(casingTextureIndex), TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_ORE_DRILL)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ORE_DRILL_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { getCasingTextureForId(casingTextureIndex) };
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("workState", workState);
        aNBT.setBoolean("chunkLoadingEnabled", mChunkLoadingEnabled);
        aNBT.setBoolean("isChunkloading", mCurrentChunk != null);
        if (mCurrentChunk != null) {
            aNBT.setInteger("loadedChunkXPos", mCurrentChunk.chunkXPos);
            aNBT.setInteger("loadedChunkZPos", mCurrentChunk.chunkZPos);
        }
        aNBT.setBoolean("mBlacklist", this.mBlacklist);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        workState = aNBT.getInteger("workState");
        if (aNBT.hasKey("chunkLoadingEnabled")) mChunkLoadingEnabled = aNBT.getBoolean("chunkLoadingEnabled");
        if (aNBT.getBoolean("isChunkloading")) {
            mCurrentChunk = new ChunkCoordIntPair(
                aNBT.getInteger("loadedChunkXPos"),
                aNBT.getInteger("loadedChunkZPos"));
        }
        this.mBlacklist = aNBT.getBoolean("mBlacklist");
    }

    @Override
    public boolean onSolderingToolRightClick(ForgeDirection side, ForgeDirection wrenchingSide,
        EntityPlayer entityPlayer, float aX, float aY, float aZ) {
        if (side == getBaseMetaTileEntity().getFrontFacing()) {
            mChunkLoadingEnabled = !mChunkLoadingEnabled;
            GTUtility.sendChatToPlayer(
                entityPlayer,
                mChunkLoadingEnabled ? GTUtility.trans("502", "Mining chunk loading enabled")
                    : GTUtility.trans("503", "Mining chunk loading disabled"));
            return true;
        }
        return super.onSolderingToolRightClick(side, wrenchingSide, entityPlayer, aX, aY, aZ);
    }

    @Override
    public void onRemoval() {
        if (mChunkLoadingEnabled) GTChunkManager.releaseTicket((TileEntity) getBaseMetaTileEntity());
        super.onRemoval();
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        if (aBaseMetaTileEntity.isServerSide() && mCurrentChunk != null
            && !mWorkChunkNeedsReload
            && !aBaseMetaTileEntity.isAllowedToWork()) {
            // if machine has stopped, stop chunkloading
            GTChunkManager.releaseTicket((TileEntity) aBaseMetaTileEntity);
            mWorkChunkNeedsReload = true;
        }
    }

    protected void onAbort() {}

    protected void abortDrilling() {
        if (workState != STATE_ABORT) {
            workState = STATE_ABORT;
            onAbort();
            setShutdownReason("");

            if (!isAllowedToWork()) {
                enableWorking();
            }
        }
    }

    @Override
    public void enableWorking() {
        super.enableWorking();
        shutdownReason = "";
    }

    protected void setShutdownReason(@NotNull String newReason) {
        shutdownReason = newReason;
    }

    @Override
    public boolean isRotationChangeAllowed() {
        return false;
    }

    @Override
    protected IAlignmentLimits getInitialAlignmentLimits() {
        return (d, r, f) -> (d.flag & (ForgeDirection.UP.flag | ForgeDirection.DOWN.flag)) == 0 && r.isNotRotated()
            && !f.isVerticallyFliped();
    }

    @Override
    public final IStructureDefinition<ETHVoidMinerBase> getStructureDefinition() {
        return STRUCTURE_DEFINITION.get(getClass());
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        return checkPiece(STRUCTURE_PIECE_MAIN, 1, 6, 0) && checkHatches()
            && GTUtility.getTier(getMaxInputVoltage()) >= 0;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    protected boolean checkHatches() {
        return true;
    }

    @Override
    public ChunkCoordIntPair getActiveChunk() {
        return mCurrentChunk;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, 1, 6, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivialBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 1, 6, 0, elementBudget, env, false, true);
    }

    @Override
    protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
        super.drawTexts(screenElements, inventorySlot);
        screenElements.widget(
            TextWidget.dynamicString(() -> shutdownReason)
                .setSynced(false)
                .setTextAlignment(Alignment.CenterLeft)
                .setEnabled(widget -> !(getBaseMetaTileEntity().isActive() || shutdownReason.isEmpty())))
            .widget(new FakeSyncWidget.StringSyncer(() -> shutdownReason, newString -> shutdownReason = newString));
    }

    @Override
    public String getMachineType() {
        return "Void Miner";
    }

    @Override
    public int getMaxParallelRecipes() {
        return 0;
    }

    protected List<ButtonWidget> getAdditionalButtons(ModularWindow.Builder builder, UIBuildContext buildContext) {
        return ImmutableList.of();
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);
        final int BUTTON_Y_LEVEL = 91;

        builder.widget(
            new LockedWhileActiveButton(this.getBaseMetaTileEntity(), builder)
                .setOnClick((clickData, widget) -> mChunkLoadingEnabled = !mChunkLoadingEnabled)
                .setPlayClickSound(true)
                .setBackground(() -> {
                    if (mChunkLoadingEnabled) {
                        return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
                            GTUITextures.OVERLAY_BUTTON_CHUNK_LOADING };
                    }
                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
                        GTUITextures.OVERLAY_BUTTON_CHUNK_LOADING_OFF };
                })
                .attachSyncer(
                    new FakeSyncWidget.BooleanSyncer(
                        () -> mChunkLoadingEnabled,
                        newBoolean -> mChunkLoadingEnabled = newBoolean),
                    builder,
                    (widget, val) -> widget.notifyTooltipChange())
                .dynamicTooltip(
                    () -> ImmutableList.of(
                        StatCollector.translateToLocal(
                            mChunkLoadingEnabled ? "GT5U.gui.button.chunk_loading_on"
                                : "GT5U.gui.button.chunk_loading_off")))
                .setTooltipShowUpDelay(TOOLTIP_DELAY)
                .setPos(new Pos2d(80, BUTTON_Y_LEVEL))
                .setSize(16, 16))
            .widget(
                new ButtonWidget().setOnClick((clickData, widget) -> abortDrilling())
                    .setPlayClickSound(true)
                    .setBackground(() -> {
                        if (workState == STATE_ABORT) {
                            return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
                                GTUITextures.OVERLAY_BUTTON_RETRACT_PIPE, GTUITextures.OVERLAY_BUTTON_LOCKED };
                        }
                        return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
                            GTUITextures.OVERLAY_BUTTON_RETRACT_PIPE };
                    })
                    .attachSyncer(
                        new FakeSyncWidget.IntegerSyncer(() -> workState, (newInt) -> workState = newInt),
                        builder,
                        (widget, integer) -> widget.notifyTooltipChange())
                    .dynamicTooltip(
                        () -> ImmutableList.of(
                            StatCollector.translateToLocalFormatted(
                                workState == STATE_ABORT ? "GT5U.gui.button.drill_retract_pipes_active"
                                    : "GT5U.gui.button.drill_retract_pipes")))
                    .setTooltipShowUpDelay(TOOLTIP_DELAY)
                    .setPos(new Pos2d(174, 112))
                    .setSize(16, 16));

        int left = 98;
        for (ButtonWidget button : getAdditionalButtons(builder, buildContext)) {
            button.setPos(new Pos2d(left, BUTTON_Y_LEVEL))
                .setSize(16, 16);
            builder.widget(button);
            left += 18;
        }
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

    /**
     * Computes first the ores related to the dim the VM is in, then the ores added manually, then it computes the
     * totalWeight for normalisation
     */
    private void calculateDropMap() {
        this.dropMap = new VoidMinerUtility.DropMap();
        this.extraDropMap = new VoidMinerUtility.DropMap();
        int id = this.getBaseMetaTileEntity()
            .getWorld().provider.dimensionId;
        this.handleModDimDef(id);
        this.handleExtraDrops(id);
        this.totalWeight = dropMap.getTotalWeight() + extraDropMap.getTotalWeight();
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

    protected List<IHatchElement<? super ETHVoidMinerBase>> getAllowedHatches() {
        return ImmutableList.of(InputBus, OutputBus, Maintenance, Energy);
    }

    protected void addResultMessage(final int state, @NotNull final CheckRecipeResult result) {
        resultRegistry.put(new ResultRegistryKey(state, result.wasSuccessful()), result);
    }

    /**
     * Sets or overrides the {@link CheckRecipeResult} for a given work state and operation success type.
     *
     * @param state         A work state like {@link #STATE_AT_BOTTOM}.
     * @param wasSuccessful Whether the operation was successful.
     * @param resultKey     An I18N key for the message.
     */
    protected void addResultMessage(final int state, final boolean wasSuccessful, @NotNull final String resultKey) {
        addResultMessage(
            state,
            wasSuccessful ? SimpleCheckRecipeResult.ofSuccess(resultKey)
                : SimpleCheckRecipeResult.ofFailure(resultKey));
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        String casings = this.getCasingBlockItem()
            .get(0)
            .getDisplayName();

        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Miner")
            .addInfo("Consumes " + GTValues.V[0] + "EU/t")
            .addInfo(
                "Can be supplied with 2L/s of Neon(x4), Krypton(x8), Xenon(x16) or Oganesson(x64) for higher outputs.")
            .addInfo("Put the Ore into the input bus to set the Whitelist/Blacklist")
            .addInfo("Use a screwdriver to toggle Whitelist/Blacklist")
            .addInfo(
                "Blacklist or non Whitelist Ore will be " + EnumChatFormatting.DARK_RED
                    + "VOIDED"
                    + EnumChatFormatting.RESET
                    + ".")
            .beginStructureBlock(3, 7, 3, false)
            .addController("Front bottom")
            .addOtherStructurePart(casings, "form the 3x1x3 Base")
            .addOtherStructurePart(casings, "1x3x1 pillar above the center of the base (2 minimum total)")
            .addOtherStructurePart(
                this.getFrameMaterial().mName + " Frame Boxes",
                "Each pillar's side and 1x3x1 on top")
            .addEnergyHatch(VN[0] + "+, Any base casing")
            .addMaintenanceHatch("Any base casing")
            .addInputBus("Mining Pipes or Ores, optional, any base casing")
            .addInputHatch("Optional noble gas, any base casing")
            .addOutputBus("Any base casing")
            .toolTipFinisher();
        return tt;
    }

    @Desugar
    private record ResultRegistryKey(int state, boolean successful) {

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ResultRegistryKey other)) {
                return false;
            }

            return (state == other.state && successful == other.successful);
        }

    }
}
