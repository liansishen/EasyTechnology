package com.hepdd.easytech.common.tileentities.machines.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static com.gtnewhorizon.structurelib.util.XSTR.XSTR_INSTANCE;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.IAlignmentProvider;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.ProgressBar;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ParticleFX;
import gregtech.api.enums.SteamVariant;
import gregtech.api.gui.modularui.GTUIInfos;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.gui.modularui.GUITextureSet;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.modularui.IGetTitleColor;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.RecipeMapWorkable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.objects.GTItemStack;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.WorldSpawnedEventBuilder;
import gregtech.common.pollution.Pollution;

public abstract class ETHPrimitiveBlastFurnace extends MetaTileEntity
    implements IAlignment, ISurvivalConstructable, RecipeMapWorkable, IAddUIWidgets, IGetTitleColor {

    public static final int INPUT_SLOTS = 9, OUTPUT_SLOTS = 9;
    private static final String STRUCTURE_PIECE_TOP = "top";
    private static final String STRUCTURE_PIECE_TOP_LAYER = "topLayer";
    private static final String STRUCTURE_PIECE_LAYER = "layer";
    private static final String STRUCTURE_PIECE_BOTTOM_LAYER = "BottomLayer";
    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final String STRUCTURE_PIECE_BOTTOM = "bottom";
    private static final int MAX_LEVEL = 12;

    private static final ClassValue<IStructureDefinition<ETHPrimitiveBlastFurnace>> STRUCTURE_DEFINITION = new ClassValue<>() {

        @Override
        protected IStructureDefinition<ETHPrimitiveBlastFurnace> computeValue(Class<?> type) {
            return IStructureDefinition.<ETHPrimitiveBlastFurnace>builder()
                .addShape(
                    STRUCTURE_PIECE_TOP,
                    transpose(
                        new String[][] {
                            { "           ", "    ccc    ", "   c   c   ", "  c     c  ", " c       c ", " c       c ",
                                " c       c ", "  c     c  ", "   c   c   ", "    ccc    ", "           " } }))
                .addShape(
                    STRUCTURE_PIECE_TOP_LAYER,
                    transpose(
                        new String[][] {
                            { "           ", "    ccc    ", "   ccccc   ", "  ccccccc  ", " ccccccccc ", " cccc cccc ",
                                " ccccccccc ", "  ccccccc  ", "   ccccc   ", "    ccc    ", "           ", } }))
                .addShape(
                    STRUCTURE_PIECE_LAYER,
                    transpose(
                        new String[][] {
                            { "     s     ", "    ccc    ", "   c   c   ", "  c     c  ", " c       c ", "sc       cs",
                                " c       c ", "  c     c  ", "   c   c   ", "    ccc    ", "     s     ", } }))
                .addShape(
                    STRUCTURE_PIECE_BOTTOM_LAYER,
                    transpose(
                        new String[][] {
                            { "    sss    ", "   s   s   ", "  s     s  ", " s       s ", "s         s", "s         s",
                                "s         s", " s       s ", "  s     s  ", "   s   s   ", "    sss    ", } }))
                .addShape(
                    STRUCTURE_PIECE_MAIN,
                    transpose(
                        new String[][] {
                            { "    c~c    ", "   scccs   ", "  sc   cs  ", " sc     cs ", "cc       cc", "cc       cc",
                                "cc       cc", " sc     cs ", "  sc   cs  ", "   scccs   ", "    ccc    ", } }))
                .addShape(
                    STRUCTURE_PIECE_BOTTOM,
                    transpose(
                        new String[][] {
                            { "   scccs   ", " sssssssss ", " sssssssss ", "sssssssssss", "csssssssssc", "csssssssssc",
                                "csssssssssc", "sssssssssss", " sssssssss ", " sssssssss ", "   scccs   ", } }))
                .addElement('c', lazy(t -> ofBlock(t.getCasingBlock(), t.getCasingMetaID())))
                .addElement('s', ofBlock(Blocks.stonebrick, 0))
                .build();
        }
    };

    public int mMaxProgresstime = 0;
    private volatile boolean mUpdated;
    public int mUpdate = 5;
    public int mProgresstime = 0;
    public boolean mMachine = false;
    private int mLevel = 0;

    public ItemStack[] mOutputItems = new ItemStack[OUTPUT_SLOTS];

    public ETHPrimitiveBlastFurnace(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, INPUT_SLOTS + OUTPUT_SLOTS);
    }

    public ETHPrimitiveBlastFurnace(String aName) {
        super(aName, INPUT_SLOTS + OUTPUT_SLOTS);
    }

    @Override
    public boolean isTeleporterCompatible() {
        return false;
    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {
        return (facing.flag & (ForgeDirection.UP.flag | ForgeDirection.DOWN.flag)) == 0;
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {
        return true;
    }

    @Override
    public int getProgresstime() {
        return this.mProgresstime;
    }

    @Override
    public int maxProgresstime() {
        return this.mMaxProgresstime;
    }

    @Override
    public int increaseProgress(int aProgress) {
        this.mProgresstime += aProgress;
        return this.mMaxProgresstime - this.mProgresstime;
    }

    public int getLevel() {
        return this.mLevel;
    }

    @Override
    public boolean allowCoverOnSide(ForgeDirection side, GTItemStack aCoverID) {
        return (GregTechAPI.getCoverBehaviorNew(aCoverID.toStack())
            .isSimpleCover()) && (super.allowCoverOnSide(side, aCoverID));
    }

    @Override
    public abstract MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity);

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setInteger("mProgresstime", this.mProgresstime);
        aNBT.setInteger("mMaxProgresstime", this.mMaxProgresstime);
        if (this.mOutputItems != null) {
            for (int i = 0; i < mOutputItems.length; i++) {
                if (this.mOutputItems[i] != null) {
                    NBTTagCompound tNBT = new NBTTagCompound();
                    this.mOutputItems[i].writeToNBT(tNBT);
                    aNBT.setTag("mOutputItem" + i, tNBT);
                }
            }
        }
        aNBT.setInteger("mLevel", this.mLevel);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        this.mUpdate = 5;
        this.mProgresstime = aNBT.getInteger("mProgresstime");
        this.mMaxProgresstime = aNBT.getInteger("mMaxProgresstime");
        this.mOutputItems = new ItemStack[OUTPUT_SLOTS];
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            this.mOutputItems[i] = GTUtility.loadItem(aNBT, "mOutputItem" + i);
        }
        this.mLevel = aNBT.getInteger("mLevel");
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        GTUIInfos.openGTTileEntityUI(aBaseMetaTileEntity, aPlayer);
        return true;
    }

    @Override
    public ExtendedFacing getExtendedFacing() {
        return ExtendedFacing.of(getBaseMetaTileEntity().getFrontFacing());
    }

    @Override
    public void setExtendedFacing(ExtendedFacing alignment) {
        getBaseMetaTileEntity().setFrontFacing(alignment.getDirection());
    }

    @Override
    public IAlignmentLimits getAlignmentLimits() {
        return (d, r, f) -> (d.flag & (ForgeDirection.UP.flag | ForgeDirection.DOWN.flag)) == 0 && r.isNotRotated()
            && f.isNotFlipped();
    }

    private boolean checkMachine() {
        int mHeight = -1;
        this.mLevel = 0;
        if (!this.checkPiece(STRUCTURE_PIECE_BOTTOM, 5, mHeight++, 0)) return false;
        if (!this.checkPiece(STRUCTURE_PIECE_MAIN, 5, mHeight++, 0)) return false;

        for (int i = 0; i < 2; i++) {
            if (!this.checkPiece(STRUCTURE_PIECE_BOTTOM_LAYER, 5, mHeight++, 0)) return false;
        }
        for (int i = 0; i < MAX_LEVEL; i++) {
            if (!this.checkPiece(STRUCTURE_PIECE_LAYER, 5, mHeight++, 0)) {
                mHeight = mHeight - 1;
                break;
            }
            this.mLevel++;
        }

        if (!this.checkPiece(STRUCTURE_PIECE_TOP_LAYER, 5, mHeight++, 0)) return false;
        if (!this.checkPiece(STRUCTURE_PIECE_TOP, 5, mHeight, 0)) return false;

        return true;
    }

    protected boolean checkPiece(String piece, int horizontalOffset, int verticalOffset, int depthOffset) {
        return STRUCTURE_DEFINITION.get(this.getClass())
            .check(
                this,
                piece,
                getBaseMetaTileEntity().getWorld(),
                getExtendedFacing(),
                getBaseMetaTileEntity().getXCoord(),
                getBaseMetaTileEntity().getYCoord(),
                getBaseMetaTileEntity().getZCoord(),
                horizontalOffset,
                verticalOffset,
                depthOffset,
                !mMachine);
    }

    protected abstract Block getCasingBlock();

    protected abstract int getCasingMetaID();

    @Override
    public void onMachineBlockUpdate() {
        mUpdated = true;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {

        if (aBaseMetaTileEntity.isServerSide()) {
            if (mUpdated) {
                // duct tape fix for too many updates on an overloaded server, causing the structure check to not run
                if (mUpdate < 0) mUpdate = 5;
                mUpdated = false;
            }
            if (this.mUpdate-- == 0) {
                this.mMachine = checkMachine();
            }
            if (this.mMachine) {
                if (this.mMaxProgresstime > 0) {
                    if (++this.mProgresstime >= this.mMaxProgresstime) {
                        addOutputProducts();
                        this.mOutputItems = null;
                        this.mProgresstime = 0;
                        this.mMaxProgresstime = 0;
                        GTMod.achievements.issueAchievement(
                            aBaseMetaTileEntity.getWorld()
                                .getPlayerEntityByName(aBaseMetaTileEntity.getOwnerName()),
                            "steel");
                    }
                } else if (aBaseMetaTileEntity.isAllowedToWork()) {
                    checkRecipe();
                }
            }
            if (this.mMaxProgresstime > 0 && (aTimer % 20L == 0L)) {
                Pollution.addPollution(
                    this.getBaseMetaTileEntity(),
                    GTMod.gregtechproxy.mPollutionPrimitveBlastFurnacePerSecond * (this.mLevel + 4));
            }

            aBaseMetaTileEntity.setActive((this.mMaxProgresstime > 0) && (this.mMachine));
        }
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        if (aBaseMetaTileEntity.isClientSide())
            StructureLibAPI.queryAlignment((IAlignmentProvider) aBaseMetaTileEntity);
    }

    /**
     * Draws random flames and smoke particles in front of Primitive Blast Furnace when active
     *
     * @param aBaseMetaTileEntity The entity that will handle the {@link Block#randomDisplayTick}
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void onRandomDisplayTick(IGregTechTileEntity aBaseMetaTileEntity) {
        if (aBaseMetaTileEntity.isActive()) {

            final ForgeDirection frontFacing = aBaseMetaTileEntity.getFrontFacing();

            final double oX = aBaseMetaTileEntity.getOffsetX(frontFacing, 1) + 0.5D;
            final double oY = aBaseMetaTileEntity.getOffsetY(frontFacing, 1);
            final double oZ = aBaseMetaTileEntity.getOffsetZ(frontFacing, 1) + 0.5D;
            final double offset = -0.48D;
            final double horizontal = XSTR_INSTANCE.nextFloat() * 8D / 16D - 4D / 16D;

            final double x, y, z;

            y = oY + XSTR_INSTANCE.nextFloat() * 10D / 16D + 5D / 16D;

            if (frontFacing == ForgeDirection.WEST) {
                x = oX - offset;
                z = oZ + horizontal;
            } else if (frontFacing == ForgeDirection.EAST) {
                x = oX + offset;
                z = oZ + horizontal;
            } else if (frontFacing == ForgeDirection.NORTH) {
                x = oX + horizontal;
                z = oZ - offset;
            } else // if (frontFacing == ForgeDirection.SOUTH.ordinal())
            {
                x = oX + horizontal;
                z = oZ + offset;
            }

            WorldSpawnedEventBuilder.ParticleEventBuilder particleEventBuilder = (new WorldSpawnedEventBuilder.ParticleEventBuilder())
                .setMotion(0D, 0D, 0D)
                .setPosition(x, y, z)
                .setWorld(getBaseMetaTileEntity().getWorld());
            particleEventBuilder.setIdentifier(ParticleFX.SMOKE)
                .run();
            particleEventBuilder.setIdentifier(ParticleFX.FLAME)
                .run();
        }
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return RecipeMaps.primitiveBlastRecipes;
    }

    private void addOutputProducts() {
        if (this.mOutputItems == null) {
            return;
        }
        int limit = Math.min(mOutputItems.length, OUTPUT_SLOTS);
        for (int i = 0; i < limit; i++) {
            int absi = INPUT_SLOTS + i;
            if (this.mInventory[absi] == null) {
                this.mInventory[absi] = GTUtility.copyOrNull(this.mOutputItems[i]);
            } else if (GTUtility.areStacksEqual(this.mInventory[absi], this.mOutputItems[i])) {
                this.mInventory[absi].stackSize = Math.min(
                    this.mInventory[absi].getMaxStackSize(),
                    this.mInventory[absi].stackSize + this.mOutputItems[i].stackSize);
            }
        }
    }

    private int[] doOutput(ItemStack[] tempInv, ItemStack outputStack, int itemCount) {
        int outputIndex = -1;
        int[] ret = new int[2];
        for (int i = INPUT_SLOTS; i < INPUT_SLOTS + OUTPUT_SLOTS; i++) {
            if (tempInv[i] == null) {
                outputStack.stackSize = Math.min(itemCount, outputStack.getMaxStackSize());
                itemCount = Math.max(itemCount - outputStack.stackSize, 0);
                outputIndex = i - INPUT_SLOTS;
                ret[0] = itemCount;
                ret[1] = outputIndex;
                tempInv[i] = GTUtility.copy(outputStack);
                return ret;
            } else if (GTUtility.areStacksEqual(tempInv[i], outputStack)) {
                int left = tempInv[i].getMaxStackSize() - tempInv[i].stackSize;
                if (left > 0) {
                    outputStack.stackSize = Math.min(left, itemCount);
                    itemCount = Math.max(itemCount - outputStack.stackSize, 0);
                    outputIndex = i - INPUT_SLOTS;
                    ret[0] = itemCount;
                    ret[1] = outputIndex;
                    tempInv[i].stackSize += outputStack.stackSize;
                    return ret;
                }
            }
        }
        ret[0] = itemCount;
        ret[1] = outputIndex;
        return ret;
    }

    private boolean checkRecipe() {
        if (!this.mMachine) {
            return false;
        }
        ItemStack[] inputs = new ItemStack[INPUT_SLOTS];
        System.arraycopy(mInventory, 0, inputs, 0, INPUT_SLOTS);
        GTRecipe recipe = getRecipeMap().findRecipeQuery()
            .items(inputs)
            .find();
        if (recipe == null) {
            this.mOutputItems = null;
            return false;
        }

        int maxParallel = (int) recipe.maxParallelCalculatedByInputs(4 + this.mLevel, null, inputs);
        ItemStack[] newOutputs = new ItemStack[OUTPUT_SLOTS];
        maxParallel = tryOutput(maxParallel, recipe, newOutputs);
        if (maxParallel <= 0) {
            this.mOutputItems = null;
            return false;
        }

        if (!recipe.isRecipeInputEqual(true, false, maxParallel, null, inputs)) {
            this.mOutputItems = null;
            return false;
        }
        for (int i = 0; i < INPUT_SLOTS; i++) {
            if (mInventory[i] != null && mInventory[i].stackSize == 0) {
                mInventory[i] = null;
            }
        }

        this.mMaxProgresstime = recipe.mDuration;
        this.mOutputItems = newOutputs;
        return true;
    }

    private int tryOutput(int maxParallel, GTRecipe recipe, ItemStack[] newOutputs) {
        while (maxParallel > 0) {
            ItemStack[] tempInv = GTUtility.copyItemArray(this.mInventory);
            ItemStack[] tempOutput = GTUtility.copyItemArray(newOutputs);
            boolean isBreak = false;
            for (int i = 0; i < recipe.mOutputs.length; i++) {
                if (recipe.getOutput(i) != null) {
                    ItemStack itemStack = GTUtility.copy(recipe.getOutput(i));
                    int leftCount = itemStack.stackSize * maxParallel;
                    while (leftCount > 0) {
                        int[] ret = doOutput(tempInv, itemStack, leftCount);
                        leftCount = ret[0];
                        if (ret[1] < 0) {
                            maxParallel--;
                            isBreak = true;
                            break;
                        }
                        tempOutput[ret[1]] = GTUtility.copy(itemStack);
                    }
                    if (isBreak) break;
                }
            }
            if (!isBreak) {
                System.arraycopy(tempOutput, 0, newOutputs, 0, OUTPUT_SLOTS);
                break;
            }
        }
        return maxParallel;
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return aIndex > INPUT_SLOTS;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return !GTUtility.areStacksEqual(aStack, this.mInventory[0]);
    }

    @Override
    public byte getTileEntityBaseType() {
        return 0;
    }

    public abstract String getName();

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        int mHeight = -1;
        int build = survivialBuildPiece(STRUCTURE_PIECE_BOTTOM, stackSize, 5, mHeight++, 0, elementBudget, env, false);
        if (build >= 0) return build;
        build = survivialBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 5, mHeight++, 0, elementBudget, env, false);
        if (build >= 0) return build;
        for (int i = 0; i < 2; i++) {
            build = survivialBuildPiece(
                STRUCTURE_PIECE_BOTTOM_LAYER,
                stackSize,
                5,
                mHeight++,
                0,
                elementBudget,
                env,
                false);
            if (build >= 0) return build;
        }
        int intLevel = 0;
        NBTTagCompound tag = stackSize.getTagCompound();
        if (tag != null) {
            intLevel = tag.getCompoundTag("channels")
                .getInteger("stack_size");
        }
        intLevel = Math.min(intLevel, MAX_LEVEL);
        for (int i = 0; i < intLevel; i++) {
            build = survivialBuildPiece(STRUCTURE_PIECE_LAYER, stackSize, 5, mHeight++, 0, elementBudget, env, false);
            if (build >= 0) return build;
        }
        build = survivialBuildPiece(STRUCTURE_PIECE_TOP_LAYER, stackSize, 5, mHeight++, 0, elementBudget, env, false);
        if (build >= 0) return build;
        return survivialBuildPiece(STRUCTURE_PIECE_TOP, stackSize, 5, mHeight, 0, elementBudget, env, false);
    }

    protected final int survivialBuildPiece(String piece, ItemStack trigger, int horizontalOffset, int verticalOffset,
        int depthOffset, int elementsBudget, ISurvivalBuildEnvironment env, boolean check) {
        final IGregTechTileEntity tTile = getBaseMetaTileEntity();
        return STRUCTURE_DEFINITION.get(getClass())
            .survivalBuild(
                this,
                trigger,
                piece,
                tTile.getWorld(),
                getExtendedFacing(),
                tTile.getXCoord(),
                tTile.getYCoord(),
                tTile.getZCoord(),
                horizontalOffset,
                verticalOffset,
                depthOffset,
                elementsBudget,
                env,
                check);
    }

    @Override
    public IStructureDefinition<?> getStructureDefinition() {
        return STRUCTURE_DEFINITION.get(getClass());
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        int mHeight = -1;
        buildPiece(STRUCTURE_PIECE_BOTTOM, stackSize, hintsOnly, 5, mHeight++, 0);
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, 5, mHeight++, 0);
        for (int i = 0; i < 2; i++) {
            buildPiece(STRUCTURE_PIECE_BOTTOM_LAYER, stackSize, hintsOnly, 5, mHeight++, 0);
        }
        int intLevel = 0;
        NBTTagCompound tag = stackSize.getTagCompound();
        if (tag != null) {
            intLevel = tag.getCompoundTag("channels")
                .getInteger("stack_size");
        }
        intLevel = Math.min(intLevel, MAX_LEVEL);
        for (int i = 0; i < intLevel; i++) {
            buildPiece(STRUCTURE_PIECE_LAYER, stackSize, hintsOnly, 5, mHeight++, 0);
        }
        buildPiece(STRUCTURE_PIECE_TOP_LAYER, stackSize, hintsOnly, 5, mHeight++, 0);
        buildPiece(STRUCTURE_PIECE_TOP, stackSize, hintsOnly, 5, mHeight, 0);

    }

    protected final boolean buildPiece(String piece, ItemStack trigger, boolean hintOnly, int horizontalOffset,
        int verticalOffset, int depthOffset) {
        final IGregTechTileEntity tTile = getBaseMetaTileEntity();
        return STRUCTURE_DEFINITION.get(getClass())
            .buildOrHints(
                this,
                trigger,
                piece,
                tTile.getWorld(),
                getExtendedFacing(),
                tTile.getXCoord(),
                tTile.getYCoord(),
                tTile.getZCoord(),
                horizontalOffset,
                verticalOffset,
                depthOffset,
                hintOnly);
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        int posX, posY;
        posX = 15;
        posY = 15;
        for (int i = 0; i < INPUT_SLOTS; i++) {
            if (i > 0 && i % 3 == 0) {
                posY += 18;
                posX = 15;
            }
            if (i % 3 > 0) posX += 18;

            builder.widget(
                new SlotWidget(inventoryHandler, i).setBackground(getGUITextureSet().getItemSlot())
                    .setPos(posX, posY));
        }
        posX = 99;
        posY = 15;
        for (int i = INPUT_SLOTS; i < INPUT_SLOTS + OUTPUT_SLOTS; i++) {
            if (i > INPUT_SLOTS && i % 3 == 0) {
                posY += 18;
                posX = 99;
            }
            if (i % 3 > 0) posX += 18;

            builder.widget(
                new SlotWidget(inventoryHandler, i).setBackground(getGUITextureSet().getItemSlot())
                    .setPos(posX, posY));
        }

        builder.widget(
            new ProgressBar().setTexture(GTUITextures.PROGRESSBAR_ARROW_2_STEAM.get(getSteamVariant()), 20)
                .setProgress(() -> (float) mProgresstime / mMaxProgresstime)
                .setNEITransferRect(
                    getRecipeMap().getFrontend()
                        .getUIProperties().neiTransferRectId)
                .setPos(74, 24)
                .setSize(20, 18));
    }

    @Override
    public GUITextureSet getGUITextureSet() {
        return GUITextureSet.STEAM.apply(getSteamVariant());
    }

    @Override
    public int getTitleColor() {
        return getSteamVariant() == SteamVariant.BRONZE ? COLOR_TITLE.get() : COLOR_TITLE_WHITE.get();
    }
}
