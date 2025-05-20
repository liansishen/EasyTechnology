package com.hepdd.easytech.common.tileentities.machines.multi;

import bwcrossmod.galacticgreg.VoidMinerUtility;
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
import com.gtnewhorizons.modularui.common.widget.*;
import com.hepdd.easytech.api.metatileentity.implementations.base.ETHFuelMultiBase;
import com.hepdd.easytech.api.metatileentity.implementations.base.ETHNonConsumMultiBase;
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
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchDataAccess;
import gregtech.api.objects.GTChunkManager;
import gregtech.api.objects.XSTR;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IGTHatchAdder;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.WorldgenGTOreLayer;
import gregtech.common.WorldgenGTOreSmallPieces;
import gregtech.common.tileentities.machines.multi.MTEDrillerBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static gregtech.api.enums.GTValues.VN;
import static gregtech.api.enums.GTValues.W;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;
import static gregtech.api.util.GTUtility.validMTEList;

public class ETHPrimitiveVoidMiner extends ETHFuelMultiBase<ETHPrimitiveVoidMiner>
    implements IChunkLoader, ISurvivalConstructable {

    protected static final String STRUCTURE_PIECE_MAIN = "main";
    protected static final ClassValue<IStructureDefinition<ETHPrimitiveVoidMiner>> STRUCTURE_DEFINITION = new ClassValue<>() {

        @Override
        protected IStructureDefinition<ETHPrimitiveVoidMiner> computeValue(Class<?> type) {
            return StructureDefinition.<ETHPrimitiveVoidMiner>builder()
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
                        t -> buildHatchAdder(ETHPrimitiveVoidMiner.class).atLeastList(t.getAllowedHatches())
                            .adder(ETHPrimitiveVoidMiner::addToMachineList)
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

    protected final byte TIER_MULTIPLIER;
    private boolean mBlacklist = false;

    private Block casingBlock;
    private int casingMeta;
    private int frameMeta;
    protected int casingTextureIndex;
    protected int workState;
    protected static final int STATE_DOWNWARD = 0, STATE_AT_BOTTOM = 1, STATE_UPWARD = 2, STATE_ABORT = 3;

    protected boolean mChunkLoadingEnabled = true;
    protected ChunkCoordIntPair mCurrentChunk = null;
    protected boolean mWorkChunkNeedsReload = true;

    private final Map<ResultRegistryKey, CheckRecipeResult> resultRegistry = new HashMap<>();

    /** Allows inheritors to supply custom runtime failure messages. */
    private CheckRecipeResult runtimeFailure = null;
    private CheckRecipeResult lastRuntimeFailure = null;

    /** Allows inheritors to supply custom shutdown failure messages. */
    private @NotNull String shutdownReason = "";

    /** Allows inheritors to suppress wiping the last error if the machine is forcibly turned off. */
    protected boolean suppressErrorWipe = false;

    protected ItemList getCasingBlockItem() {
        return ItemList.Casing_UV;
    }

    protected Materials getFrameMaterial() {
        return Materials.Europium;
    }

    protected int getCasingTextureIndex() {
        return 8;
    }

    public ETHPrimitiveVoidMiner(String aName) {
        super(aName);
        initFields();
        this.TIER_MULTIPLIER = 1;
    }

    public ETHPrimitiveVoidMiner(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        initFields();
        this.TIER_MULTIPLIER = 1;
    }


    @Override
    @NotNull
    public CheckRecipeResult checkProcessing() {
        final boolean wasSuccessful;
        setElectricityStats();
        wasSuccessful = workingAtBottom();
        return resultRegistry.getOrDefault(
            new ResultRegistryKey(workState, wasSuccessful),
            SimpleCheckRecipeResult.ofFailure("no_mining_pipe"));
    }


    protected boolean workingAtBottom() {
        // if the dropMap has never been initialised or if the dropMap is empty
        if (this.dropMap == null || this.totalWeight == 0) this.calculateDropMap();

        if (this.totalWeight != 0.f) {
            this.handleFluidConsumption();
            this.handleOutputs();
            return true;
        } else {
            this.stopMachine(ShutDownReasonRegistry.NONE);
            return false;
        }
    }

    protected void setElectricityStats() {
        try {
            this.mEUt = Math.toIntExact(GTValues.V[0]);
        } catch (ArithmeticException e) {
            e.printStackTrace();
            this.mEUt = Integer.MAX_VALUE - 7;
        }
        this.mOutputItems = new ItemStack[0];
        this.mProgresstime = 0;
        this.mMaxProgresstime = 1;
        this.mEfficiency = this.getCurrentEfficiency(null);
        this.mEfficiencyIncrease = 10000;
        this.mEUt = this.mEUt > 0 ? -this.mEUt : this.mEUt;
    }

    protected void addOperatingMessages() {
        // Inheritors can overwrite these to add custom operating messages.
        addResultMessage(STATE_DOWNWARD, true, "deploying_pipe");
        addResultMessage(STATE_DOWNWARD, false, "extracting_pipe");
        addResultMessage(STATE_AT_BOTTOM, true, "drilling");
        addResultMessage(STATE_AT_BOTTOM, false, "no_mining_pipe");
        addResultMessage(STATE_UPWARD, true, "retracting_pipe");
        addResultMessage(STATE_UPWARD, false, "drill_generic_finished");
        addResultMessage(STATE_ABORT, true, "retracting_pipe");
        addResultMessage(STATE_ABORT, false, "drill_retract_pipes_finished");
    }

    private void initFields() {
        casingBlock = getCasingBlockItem().getBlock();
        casingMeta = getCasingBlockItem().get(0)
            .getItemDamage();
        int frameId = 4096 + getFrameMaterial().mMetaItemSubID;
        frameMeta = GregTechAPI.METATILEENTITIES[frameId] != null
            ? GregTechAPI.METATILEENTITIES[frameId].getTileEntityBaseType()
            : W;
        casingTextureIndex = getCasingTextureIndex();
        workState = STATE_DOWNWARD;

        addOperatingMessages();

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
    public final IStructureDefinition<ETHPrimitiveVoidMiner> getStructureDefinition() {
        return STRUCTURE_DEFINITION.get(getClass());
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        return checkPiece(STRUCTURE_PIECE_MAIN, 1, 6, 0) && checkHatches()
            && GTUtility.getTier(getMaxInputVoltage()) >= 0
            && mMaintenanceHatches.size() == 1;
    }

    private FakePlayer mFakePlayer = null;

    protected FakePlayer getFakePlayer(IGregTechTileEntity aBaseTile) {
        if (mFakePlayer == null) mFakePlayer = GTUtility.getFakePlayer(aBaseTile);
        mFakePlayer.setWorld(aBaseTile.getWorld());
        mFakePlayer.setPosition(aBaseTile.getXCoord(), aBaseTile.getYCoord(), aBaseTile.getZCoord());
        return mFakePlayer;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    protected boolean checkHatches() {
        return true;
    }

    public int getTotalConfigValue() {
        int config = 0;
        ArrayList<ItemStack> tCircuitList = getDataItems(1);
        for (ItemStack tCircuit : tCircuitList) config += tCircuit.getItemDamage();
        return config;
    }

    public ArrayList<MTEHatchDataAccess> mDataAccessHatches = new ArrayList<>();

    /**
     * @param state using bitmask, 1 for IntegratedCircuit, 2 for DataStick, 4 for DataOrb
     */
    private boolean isCorrectDataItem(ItemStack aStack, int state) {
        if ((state & 1) != 0 && ItemList.Circuit_Integrated.isStackEqual(aStack, true, true)) return true;
        if ((state & 2) != 0 && ItemList.Tool_DataStick.isStackEqual(aStack, false, true)) return true;
        return (state & 4) != 0 && ItemList.Tool_DataOrb.isStackEqual(aStack, false, true);
    }

    /**
     * @param state using bitmask, 1 for IntegratedCircuit, 2 for DataStick, 4 for DataOrb
     */
    public ArrayList<ItemStack> getDataItems(int state) {
        ArrayList<ItemStack> rList = new ArrayList<>();
        if (GTUtility.isStackValid(mInventory[1]) && isCorrectDataItem(mInventory[1], state)) {
            rList.add(mInventory[1]);
        }
        for (MTEHatchDataAccess tHatch : validMTEList(mDataAccessHatches)) {
            for (int i = 0; i < tHatch.getBaseMetaTileEntity()
                .getSizeInventory(); i++) {
                if (tHatch.getBaseMetaTileEntity()
                    .getStackInSlot(i) != null && isCorrectDataItem(
                    tHatch.getBaseMetaTileEntity()
                        .getStackInSlot(i),
                    state))
                    rList.add(
                        tHatch.getBaseMetaTileEntity()
                            .getStackInSlot(i));
            }
        }
        return rList;
    }

    @Override
    public boolean addToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        return super.addToMachineList(aTileEntity, aBaseCasingIndex)
            || addDataAccessToMachineList(aTileEntity, aBaseCasingIndex);
    }

    public boolean addDataAccessToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) return false;
        IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
        if (aMetaTileEntity == null) return false;
        if (aMetaTileEntity instanceof MTEHatchDataAccess) {
            ((MTEHatch) aMetaTileEntity).updateTexture((byte) aBaseCasingIndex);
            return mDataAccessHatches.add((MTEHatchDataAccess) aMetaTileEntity);
        }
        return false;
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
    protected boolean showRecipeTextInGUI() {
        return false;
    }


    @Override
    protected ITexture getFrontOverlay() {
        return null;
    }

    @Override
    protected ITexture getFrontOverlayActive() {
        return null;
    }

    @Override
    public String getMachineType() {
        return "";
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
     * Method used to check the current gat and its corresponding multiplier
     *
     * @return the noble gas in the hatch. returns null if there is no noble gas found.
     */
    private FluidStack getNobleGasInputAndSetMultiplier() {
        for (FluidStack s : this.getStoredFluids()) {
            for (int i = 0; i < VoidMinerUtility.NOBLE_GASSES.length; i++) {
                FluidStack ng = VoidMinerUtility.NOBLE_GASSES[i];
                if (ng.isFluidEqual(s)) {
                    this.multiplier = this.TIER_MULTIPLIER * VoidMinerUtility.NOBEL_GASSES_MULTIPLIER[i];
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * method used to decrement the quantity of gas in the hatch
     *
     * @param gasToConsume the fluid stack in the hatch
     * @return if yes or no it was able to decrement the quantity of the fluidStack
     */
    private boolean consumeNobleGas(FluidStack gasToConsume) {
        for (FluidStack s : this.getStoredFluids()) {
            if (s.isFluidEqual(gasToConsume) && s.amount >= 1) {
                s.amount -= 1;
                this.updateSlots();
                return true;
            }
        }
        return false;
    }

    /**
     * handler for the fluid consumption
     */
    private void handleFluidConsumption() {
        FluidStack storedNobleGas = this.getNobleGasInputAndSetMultiplier();
        if (storedNobleGas == null || !this.consumeNobleGas(storedNobleGas)) this.multiplier = this.TIER_MULTIPLIER;
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
            //this.dropMap = VoidMinerUtility.dropMapsByDimId.get(id);
            this.dropMap = getDropMapVanilla(id);
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
            .filter(GTUtility::isOre)
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

    private static VoidMinerUtility.DropMap getDropMapVanilla(int dimId) {
        VoidMinerUtility.DropMap dropMap = new VoidMinerUtility.DropMap();

        // Ore Veins
        Predicate<WorldgenGTOreLayer> oreLayerPredicate = makeOreLayerPredicate(dimId);
        WorldgenGTOreLayer.sList.stream()
            .filter(gt_worldgen -> gt_worldgen.mEnabled && oreLayerPredicate.test(gt_worldgen))
            .forEach(element -> {
                dropMap.addDrop(GTOreDictUnificator.get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mPrimaryMeta % 1000)],1),element.mWeight);
                dropMap.addDrop(GTOreDictUnificator.get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mSecondaryMeta % 1000)],1),element.mWeight);
                dropMap.addDrop(GTOreDictUnificator.get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mSporadicMeta % 1000)],1),element.mWeight);
                dropMap.addDrop(GTOreDictUnificator.get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mBetweenMeta % 1000)],1),element.mWeight);
//
//                dropMap.addDrop(element.mPrimaryMeta, element.mWeight, false);
//                dropMap.addDrop(element.mSecondaryMeta, element.mWeight, false);
//                dropMap.addDrop(element.mSporadicMeta, element.mWeight / 8f, false);
//                dropMap.addDrop(element.mBetweenMeta, element.mWeight / 8f, false);
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
//    @Override
//    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {
//        this.mBlacklist = !this.mBlacklist;
//        GTUtility.sendChatToPlayer(aPlayer, "Mode: " + (this.mBlacklist ? "Blacklist" : "Whitelist"));
//    }
    protected List<IHatchElement<? super ETHPrimitiveVoidMiner>> getAllowedHatches() {
        return ImmutableList.of(InputHatch, InputBus, OutputBus, Maintenance, Energy);
    }

    protected enum DataHatchElement implements IHatchElement<ETHPrimitiveVoidMiner> {

        DataAccess;

        @Override
        public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
            return Collections.singletonList(MTEHatchDataAccess.class);
        }

        @Override
        public IGTHatchAdder<ETHPrimitiveVoidMiner> adder() {
            return ETHPrimitiveVoidMiner::addDataAccessToMachineList;
        }

        @Override
        public long count(ETHPrimitiveVoidMiner t) {
            return t.mDataAccessHatches.size();
        }
    }

    protected void addResultMessage(final int state, @NotNull final CheckRecipeResult result) {
        resultRegistry.put(new ResultRegistryKey(state, result.wasSuccessful()), result);
    }

    /**
     * Sets or overrides the {@link CheckRecipeResult} for a given work state and operation success type.
     *
     * @param state         A work state like {@link #STATE_DOWNWARD}.
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
            .addInfo(
                "Will output " + 2 * this.TIER_MULTIPLIER
                    + " Ores per Second depending on the Dimension it is build in")
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

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHPrimitiveVoidMiner(this.mName);
    }

    private final static class ResultRegistryKey {

        private final int state;
        private final boolean successful;

        public ResultRegistryKey(final int state, final boolean successful) {
            this.state = state;
            this.successful = successful;
        }

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

        @Override
        public int hashCode() {
            return Objects.hash(state, successful);
        }
    }
}
