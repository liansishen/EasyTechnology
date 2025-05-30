package com.hepdd.easytech.common.tileentities.machines.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.hepdd.easytech.api.enums.ETHTextures.*;
import static com.hepdd.easytech.loaders.preload.ETHStatics.AuthorEasyTech;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.hepdd.easytech.api.metatileentity.implementations.ETHPrimitiveHatchInputBus;
import com.hepdd.easytech.api.metatileentity.implementations.ETHPrimitiveHatchOutputBus;
import com.hepdd.easytech.api.metatileentity.implementations.base.ETHNonConsumMultiBase;

import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.MultiblockTooltipBuilder;

public class ETHLargeBlastFurnace extends ETHNonConsumMultiBase<ETHLargeBlastFurnace>
    implements ISurvivalConstructable {

    private static final String STRUCTURE_PIECE_TOP = "top";
    private static final String STRUCTURE_PIECE_TOP_LAYER = "topLayer";
    private static final String STRUCTURE_PIECE_LAYER = "layer";
    private static final String STRUCTURE_PIECE_BOTTOM_LAYER = "BottomLayer";
    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final String STRUCTURE_PIECE_BOTTOM = "bottom";
    private static final int MAX_LEVEL = 12;
    private int mLevel = 0;

    private static final ClassValue<IStructureDefinition<ETHLargeBlastFurnace>> STRUCTURE_DEFINITION = new ClassValue<>() {

        @Override
        protected IStructureDefinition<ETHLargeBlastFurnace> computeValue(Class<?> type) {
            return IStructureDefinition.<ETHLargeBlastFurnace>builder()
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
                            { "    a~a    ", "   scccs   ", "  sc   cs  ", " sc     cs ", "ac       ca", "ac       ca",
                                "ac       ca", " sc     cs ", "  sc   cs  ", "   scccs   ", "    aaa    ", } }))
                .addShape(
                    STRUCTURE_PIECE_BOTTOM,
                    transpose(
                        new String[][] {
                            { "   saaas   ", " sssssssss ", " sssssssss ", "sssssssssss", "asssssssssa", "asssssssssa",
                                "asssssssssa", "sssssssssss", " sssssssss ", " sssssssss ", "   saaas   ", } }))
                .addElement('c', ofBlock(GregTechAPI.sBlockCasings4, 15))
                .addElement(
                    'a',
                    buildHatchAdder(ETHLargeBlastFurnace.class).atLeast(InputBus, OutputBus)
                        .casingIndex(MACHINE_CASING_PRIMITIVE_BLASE_FURNACE.ID)
                        .dot(1)
                        .buildAndChain(GregTechAPI.sBlockCasings4, 15))
                .addElement('s', ofBlock(Blocks.stonebrick, 0))
                .build();
        }
    };

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ETHLargeBlastFurnace(this.mName);
    }

    public ETHLargeBlastFurnace(String aName) {
        super(aName);
    }

    public ETHLargeBlastFurnace(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return RecipeMaps.primitiveBlastRecipes;
    }

    @Override
    protected int getCasingTextureIndex() {
        return MACHINE_CASING_PRIMITIVE_BLASE_FURNACE.ID;
    }

    @Override
    protected ITexture getFrontOverlay() {
        return Textures.BlockIcons.getCasingTextureForId(MACHINE_CASING_PRIMITIVE_BLASE_FURNACE_INACTIVE.ID);
    }

    @Override
    protected ITexture getFrontOverlayActive() {
        return Textures.BlockIcons.getCasingTextureForId(MACHINE_CASING_PRIMITIVE_BLASE_FURNACE_ACTIVE.ID);
    }

    @Override
    public String getMachineType() {
        return "Bricked Blast Furnace";
    }

    @Override
    public int getMaxParallelRecipes() {
        return mLevel + 4;
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
        intLevel = Math.min(stackSize.stackSize, MAX_LEVEL);
        this.mLevel = intLevel;
        for (int i = 0; i < intLevel; i++) {
            buildPiece(STRUCTURE_PIECE_LAYER, stackSize, hintsOnly, 5, mHeight++, 0);
        }
        buildPiece(STRUCTURE_PIECE_TOP_LAYER, stackSize, hintsOnly, 5, mHeight++, 0);
        buildPiece(STRUCTURE_PIECE_TOP, stackSize, hintsOnly, 5, mHeight, 0);
    }

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
        intLevel = Math.min(stackSize.stackSize, MAX_LEVEL);
        for (int i = 0; i < intLevel; i++) {
            build = survivialBuildPiece(STRUCTURE_PIECE_LAYER, stackSize, 5, mHeight++, 0, elementBudget, env, false);
            if (build >= 0) return build;
        }
        build = survivialBuildPiece(STRUCTURE_PIECE_TOP_LAYER, stackSize, 5, mHeight++, 0, elementBudget, env, false);
        if (build >= 0) return build;
        return survivialBuildPiece(STRUCTURE_PIECE_TOP, stackSize, 5, mHeight, 0, elementBudget, env, false);
    }

    @Override
    public IStructureDefinition<ETHLargeBlastFurnace> getStructureDefinition() {
        return STRUCTURE_DEFINITION.get(getClass());
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tooltipBuilder = new MultiblockTooltipBuilder();
        tooltipBuilder.addMachineType("Blast Furnace, BBF")
            .addInfo("Usable for Steel and general Pyrometallurgy")
            .addInfo("Has a useful interface, unlike other gregtech multis")
            .addPollutionAmount(GTMod.gregtechproxy.mPollutionPrimitveBlastFurnacePerSecond)
            .beginStructureBlock(7, 6, 7, false)
            .addController("Front center")
            .toolTipFinisher(AuthorEasyTech);

        return tooltipBuilder;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
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

        return checkHatches();
    }

    private boolean checkHatches() {
        if (!mInputBusses.isEmpty() && !mOutputBusses.isEmpty()
            && mOutputHatches.isEmpty()
            && mInputHatches.isEmpty()) {
            for (MetaTileEntity ht : mInputBusses) {
                if (!(ht instanceof ETHPrimitiveHatchInputBus)) return false;
            }
            for (MetaTileEntity ht : mOutputBusses) {
                if (!(ht instanceof ETHPrimitiveHatchOutputBus)) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String[] getInfoData() {
        ArrayList<String> info = new ArrayList<>(Arrays.asList(super.getInfoData()));
        info.add("Parallel: " + EnumChatFormatting.YELLOW + getMaxParallelRecipes());
        return info.toArray(new String[0]);
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        tag.setInteger("parallel", getMaxParallelRecipes());
        tag.setInteger("level", mLevel);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("mLevel", mLevel);
    }

    @Override
    public void loadNBTData(final NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        mLevel = aNBT.getInteger("mLevel");
    }

    @Override
    protected IAlignmentLimits getInitialAlignmentLimits() {
        // don't rotate a washer, water will flow out.
        return (d, r, f) -> d.offsetY == 0 && r.isNotRotated();
    }
}
