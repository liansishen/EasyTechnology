package com.hepdd.easytech.common.tileentities.machines.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.hepdd.easytech.api.objects.GTChunkManagerEx;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.Textures;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.api.objects.XSTR;
import gregtech.api.recipe.BasicUIProperties;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.misc.IDrillingLogicDelegateOwner;
import gregtech.common.ores.OreManager;

@IMetaTileEntity.SkipGenerateDescription
public abstract class ETHAbstractEntangledMiner extends MTEBasicMachine implements IDrillingLogicDelegateOwner {

    protected static final int[] RADIUS = { 8, 8, 16, 24, 32 };
    protected static final int[] SPEED = { 240, 240, 160, 80, 40 };
    protected static final int[] ENERGY = { 2, 4, 32, 128, 512 };
    private static final int SCAN_BLOCKS_PER_TICK = 64;

    protected final ArrayList<ChunkPosition> oreBlockPositions = new ArrayList<>();
    protected final int minerTier;
    protected final int rangeTier;
    protected int mSpeed;
    protected int radiusConfig;

    protected ChunkCoordIntPair targetChunk;
    protected int targetDimId;
    protected int targetX;
    protected int targetY;
    protected int targetZ;
    protected int currentScanY = 256;
    protected String lastTargetKey = "";
    protected ChunkCoordIntPair loadedChunk;
    protected final XSTR miningRng = new XSTR();
    protected int burnTime;
    protected int scanIndex;
    protected boolean scanPlaneActive;

    protected ETHAbstractEntangledMiner(int aID, String aName, String aNameRegional, int aTier, int aMinerTier,
        int aRangeTier, int aInputSlots, int aOutputSlots) {
        super(
            aID,
            aName,
            aNameRegional,
            aTier,
            1,
            new String[] { StatCollector.translateToLocal("easytech.tooltip.entangled_miner.require_card"),
                StatCollector.translateToLocal("easytech.tooltip.entangled_miner.mine_ores") },
            1,
            aOutputSlots,
            createMinerOverlays());
        minerTier = aMinerTier;
        rangeTier = aRangeTier;
        mSpeed = SPEED[minerTier];
        radiusConfig = RADIUS[rangeTier];
    }

    protected ETHAbstractEntangledMiner(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures,
        int aMinerTier, int aRangeTier, int aInputSlots, int aOutputSlots) {
        super(aName, aTier, 1, aDescription, aTextures, 1, aOutputSlots);
        minerTier = aMinerTier;
        rangeTier = aRangeTier;
        mSpeed = SPEED[minerTier];
        radiusConfig = RADIUS[rangeTier];
    }

    private static ITexture[] createMinerOverlays() {
        return new ITexture[] {
            TextureFactory.of(
                TextureFactory.of(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_SIDE_ACTIVE")),
                TextureFactory.builder()
                    .addIcon(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_SIDE_ACTIVE_GLOW"))
                    .glow()
                    .build()),
            TextureFactory.of(
                TextureFactory.of(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_SIDE")),
                TextureFactory.builder()
                    .addIcon(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_SIDE_GLOW"))
                    .glow()
                    .build()),
            TextureFactory.of(
                TextureFactory.of(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_FRONT_ACTIVE")),
                TextureFactory.builder()
                    .addIcon(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_FRONT_ACTIVE_GLOW"))
                    .glow()
                    .build()),
            TextureFactory.of(
                TextureFactory.of(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_FRONT")),
                TextureFactory.builder()
                    .addIcon(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_FRONT_GLOW"))
                    .glow()
                    .build()),
            TextureFactory.of(
                TextureFactory.of(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_TOP_ACTIVE")),
                TextureFactory.builder()
                    .addIcon(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_TOP_ACTIVE_GLOW"))
                    .glow()
                    .build()),
            TextureFactory.of(
                TextureFactory.of(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_TOP")),
                TextureFactory.builder()
                    .addIcon(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_TOP_GLOW"))
                    .glow()
                    .build()),
            TextureFactory.of(
                TextureFactory.of(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_BOTTOM_ACTIVE")),
                TextureFactory.builder()
                    .addIcon(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_BOTTOM_ACTIVE_GLOW"))
                    .glow()
                    .build()),
            TextureFactory.of(
                TextureFactory.of(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_BOTTOM")),
                TextureFactory.builder()
                    .addIcon(Textures.BlockIcons.customOptional("basicmachines/miner/OVERLAY_BOTTOM_GLOW"))
                    .glow()
                    .build()) };
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection aSide, ForgeDirection aFacing,
        int aColorIndex, boolean aActive, boolean aRedstone) {
        ITexture[] textures = super.getTexture(aBaseMetaTileEntity, aSide, aFacing, aColorIndex, aActive, aRedstone);
        ITexture baseTexture = getCustomBaseTexture(aSide, aColorIndex);
        if (baseTexture == null || textures.length == 0) return textures;

        ITexture[] replaced = Arrays.copyOf(textures, textures.length);
        replaced[0] = baseTexture;
        return replaced;
    }

    protected ITexture getCustomBaseTexture(ForgeDirection aSide, int aColorIndex) {
        return null;
    }

    protected int getOreFortuneTier() {
        return Math.max(1, mTier);
    }

    @Override
    public String[] getDescription() {
        String[] details = GTUtility.translateMultiline(
            getTooltipKey(),
            getTooltipEnergyUsage(),
            SPEED[minerTier] / 20,
            RADIUS[rangeTier] * 2 + 1,
            RADIUS[rangeTier] * 2 + 1,
            getOreFortuneTier());
        String[] description = Arrays.copyOf(mDescriptionArray, mDescriptionArray.length + details.length + 1);
        System.arraycopy(details, 0, description, mDescriptionArray.length, details.length);
        description[description.length - 1] = EnumChatFormatting.GRAY + "Add by: "
            + EnumChatFormatting.BLUE
            + EnumChatFormatting.BOLD
            + "Easy"
            + EnumChatFormatting.AQUA
            + EnumChatFormatting.BOLD
            + "Technology";;
        return description;
    }

    protected String getTooltipKey() {
        return "easytech.tooltip.entangled_miner.electric";
    }

    protected Object getTooltipEnergyUsage() {
        return ENERGY[minerTier];
    }

    protected static String formatTooltipDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    // ==================== IDrillingLogicDelegateOwner ====================

    @Override
    public int getMachineTier() {
        return getOreFortuneTier();
    }

    @Override
    public int getMachineSpeed() {
        return mSpeed;
    }

    @Override
    public boolean pullInputs(Item aItem, int aAmount, boolean aSimulate) {
        for (int i = 0; i < mInputSlotCount; i++) {
            ItemStack stack = getInputAt(i);
            if (stack != null && stack.getItem() == aItem && stack.stackSize >= aAmount) {
                if (aSimulate) return true;
                stack.stackSize -= aAmount;
                if (stack.stackSize <= 0) mInventory[getInputSlot() + i] = null;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean pushOutputs(ItemStack aStack, int aAmount, boolean aSimulate, boolean aAlsoPushToInputs) {
        if (aAlsoPushToInputs) {
            if (pushOutput(getInputSlot(), getInputSlot() + mInputSlotCount, aStack, aAmount, aSimulate)) return true;
        }
        return pushOutput(getOutputSlot(), getOutputSlot() + mOutputItems.length, aStack, aAmount, aSimulate);
    }

    private boolean pushOutput(int aStartIndex, int aEndIndex, ItemStack aStack, int aAmount, boolean aSimulate) {
        for (int i = aStartIndex; i < aEndIndex; i++) {
            ItemStack slotStack = mInventory[i];
            if (slotStack == null || slotStack.stackSize == 0) {
                if (!aSimulate) {
                    ItemStack copy = aStack.copy();
                    copy.stackSize = aAmount;
                    mInventory[i] = copy;
                }
                return true;
            }
            if (GTUtility.areStacksEqual(slotStack, aStack)
                && slotStack.stackSize + aAmount <= slotStack.getMaxStackSize()) {
                if (!aSimulate) slotStack.stackSize += aAmount;
                return true;
            }
        }
        return false;
    }

    // ==================== MTEBasicMachine overrides ====================

    @Override
    public int checkRecipe(boolean aForce) {
        return DID_NOT_FIND_RECIPE;
    }

    @Override
    public int getOutputSlot() {
        return getInputSlot() + mInputSlotCount;
    }

    @Override
    public boolean isEnetInput() {
        return false;
    }

    @Override
    public long maxEUStore() {
        return 0;
    }

    @Override
    protected boolean hasEnoughEnergyToCheckRecipe() {
        return true;
    }

    @Override
    protected boolean drainEnergyForProcess(long aEU) {
        return true;
    }

    public boolean hasFreeSpace() {
        for (int i = getOutputSlot(); i < getOutputSlot() + mOutputItems.length; i++) {
            ItemStack stack = mInventory[i];
            if (stack == null || stack.stackSize < stack.getMaxStackSize()) return true;
        }
        return false;
    }

    @Override
    public boolean isValidSlot(int aSlot) {
        return aSlot >= 0 && aSlot < mInventory.length;
    }

    @Override
    protected SoundResource getActivitySoundLoop() {
        return SoundResource.GTCEU_LOOP_MINER;
    }

    @Override
    public String[] getInfoData() {
        return new String[] {
            EnumChatFormatting.BLUE + GTUtility.translate("GT5U.machines.miner") + EnumChatFormatting.RESET,
            String.format(
                "%s: %s%d%s %s",
                GTUtility.translate("GT5U.machines.workarea"),
                EnumChatFormatting.GREEN,
                radiusConfig * 2 + 1,
                EnumChatFormatting.RESET,
                GTUtility.translate("GT5U.machines.blocks")),
            getEnergyDisplayString() };
    }

    // ==================== onPostTick (core logic from MTEMiner) ====================

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        if (!aBaseMetaTileEntity.isServerSide()) return;

        if (!updateTargetFromCard()) {
            releaseLoadedChunk();
            mMaxProgresstime = 0;
            mProgresstime = 0;
            currentScanY = 256;
            lastTargetKey = "";
            return;
        }

        if (!aBaseMetaTileEntity.isAllowedToWork()) {
            releaseLoadedChunk();
            mMaxProgresstime = 0;
            if (GTValues.debugBlockMiner) GTLog.out.println("MINER: Disabled");
            return;
        }

        if (!hasFreeSpace()) {
            releaseLoadedChunk();
            mMaxProgresstime = 0;
            if (GTValues.debugBlockMiner) GTLog.out.println("MINER: No free space");
            return;
        }

        if (oreBlockPositions.isEmpty()) {
            if (!scanPlaneActive) {
                if (currentScanY <= 0) {
                    releaseLoadedChunk();
                    aBaseMetaTileEntity.disableWorking();
                    return;
                }
                currentScanY--;
                resetScanPlane();
                scanPlaneActive = true;
            }

            boolean scanComplete = scanOreBatch();
            if (scanComplete) scanPlaneActive = false;
            if (oreBlockPositions.isEmpty()) {
                mMaxProgresstime = 0;
                if (scanComplete) releaseLoadedChunk();
                return;
            }
        }

        int requiredEU = ENERGY[minerTier] * (mSpeed - mProgresstime);
        if (!hasEnoughEnergy(aBaseMetaTileEntity, requiredEU)) {
            releaseLoadedChunk();
            mMaxProgresstime = 0;
            if (GTValues.debugBlockMiner) {
                GTLog.out.println(
                    "MINER: Not enough energy yet, want " + (ENERGY[minerTier] * mSpeed)
                        + " have "
                        + getAvailableEnergy(aBaseMetaTileEntity));
            }
            return;
        }

        mMaxProgresstime = mSpeed;
        consumeEnergy(aBaseMetaTileEntity, ENERGY[minerTier]);

        if (mProgresstime == mSpeed - 1) {
            ChunkPosition pos = oreBlockPositions.get(0);
            int worldX = targetX + pos.chunkPosX;
            int worldY = pos.chunkPosY;
            int worldZ = targetZ + pos.chunkPosZ;

            World world = loadTargetChunk(worldX, worldZ);
            if (world != null) {
                Block block = world.getBlock(worldX, worldY, worldZ);
                int meta = world.getBlockMetadata(worldX, worldY, worldZ);
                if (GTUtility.isOre(block, meta)) {
                    long seed = miningRng.getSeed();
                    List<ItemStack> drops = OreManager
                        .mineBlock(miningRng, world, worldX, worldY, worldZ, false, getOreFortuneTier(), true, true);

                    ItemStack[] plannedOutputs = planOutputInsertion(drops);
                    miningRng.setSeed(seed);
                    if (plannedOutputs == null) {
                        releaseLoadedChunk();
                        return;
                    }

                    OreManager
                        .mineBlock(miningRng, world, worldX, worldY, worldZ, false, getOreFortuneTier(), false, true);
                    commitOutputPlan(plannedOutputs);
                }
                oreBlockPositions.remove(0);
            }
        }
    }

    @Override
    public void onScrewdriverRightClick(net.minecraftforge.common.util.ForgeDirection aSide,
        net.minecraft.entity.player.EntityPlayer aPlayer, float aX, float aY, float aZ, ItemStack aStack) {
        super.onScrewdriverRightClick(aSide, aPlayer, aX, aY, aZ, aStack);
        if (aSide != getBaseMetaTileEntity().getFrontFacing() && aSide != mMainFacing) {
            if (aPlayer.isSneaking()) {
                if (radiusConfig >= 0) radiusConfig--;
                if (radiusConfig < 0) radiusConfig = RADIUS[rangeTier];
            } else {
                if (radiusConfig <= RADIUS[rangeTier]) radiusConfig++;
                if (radiusConfig > RADIUS[rangeTier]) radiusConfig = 0;
            }
            GTUtility.sendChatTrans(aPlayer, "GT5U.machines.workareaset.s", radiusConfig * 2 + 1, radiusConfig * 2 + 1);
            oreBlockPositions.clear();
            scanPlaneActive = false;
            resetScanPlane();
        }
    }

    // ==================== Ore scanning ====================

    protected boolean scanOreBatch() {
        int sideLength = radiusConfig * 2 + 1;
        int totalBlocks = sideLength * sideLength;
        int scanned = 0;

        while (scanIndex < totalBlocks && scanned < SCAN_BLOCKS_PER_TICK) {
            int dx = scanIndex / sideLength - radiusConfig;
            int dz = scanIndex % sideLength - radiusConfig;
            int worldX = targetX + dx;
            int worldZ = targetZ + dz;
            World world = loadTargetChunk(worldX, worldZ);
            if (world == null) return false;

            Block block = world.getBlock(worldX, currentScanY, worldZ);
            int meta = world.getBlockMetadata(worldX, currentScanY, worldZ);
            if (GTUtility.isOre(block, meta)) {
                oreBlockPositions.add(new ChunkPosition(dx, currentScanY, dz));
            }
            scanIndex++;
            scanned++;
        }
        return scanIndex >= totalBlocks;
    }

    protected void resetScanPlane() {
        scanIndex = 0;
    }

    // ==================== Entangled card target ====================

    protected boolean updateTargetFromCard() {
        ItemStack card = getInputAt(0);
        if (card == null || !(card.getItem() instanceof ETHEntangledCard)) return false;
        NBTTagCompound tag = card.getTagCompound();
        if (tag == null || !tag.hasKey("dimId")) return false;

        String key = tag.getInteger(
            "dimId") + ":" + tag.getInteger("x") + ":" + tag.getInteger("y") + ":" + tag.getInteger("z");
        if (!Objects.equals(key, lastTargetKey)) {
            lastTargetKey = key;
            targetDimId = tag.getInteger("dimId");
            targetX = tag.getInteger("x");
            targetY = tag.getInteger("y");
            targetZ = tag.getInteger("z");
            targetChunk = new ChunkCoordIntPair(targetX >> 4, targetZ >> 4);
            currentScanY = targetY;
            oreBlockPositions.clear();
            scanPlaneActive = false;
            resetScanPlane();
            releaseLoadedChunk();
            mMaxProgresstime = 0;
            mProgresstime = 0;
        }
        return true;
    }

    protected World loadTargetChunk(int worldX, int worldZ) {
        ChunkCoordIntPair chunk = new ChunkCoordIntPair(worldX >> 4, worldZ >> 4);
        if (chunk.equals(loadedChunk)) return DimensionManager.getWorld(targetDimId);
        releaseLoadedChunk();
        World world = DimensionManager.getWorld(targetDimId);
        if (world == null) return null;
        if (!GTChunkManagerEx.requestPlayerChunkLoad((TileEntity) getBaseMetaTileEntity(), chunk, "", targetDimId)) {
            return null;
        }
        loadedChunk = chunk;
        world.getChunkFromChunkCoords(chunk.chunkXPos, chunk.chunkZPos);
        return world;
    }

    protected void releaseLoadedChunk() {
        ChunkCoordIntPair oldLoadedChunk = loadedChunk;
        loadedChunk = null;
        if (oldLoadedChunk != null && getBaseMetaTileEntity() instanceof TileEntity tileEntity) {
            GTChunkManagerEx.releaseTicket(tileEntity);
        }
    }

    private ItemStack[] planOutputInsertion(List<ItemStack> drops) {
        ItemStack[] planned = new ItemStack[mOutputItems.length];
        for (int i = 0; i < planned.length; i++) {
            ItemStack existing = mInventory[getOutputSlot() + i];
            planned[i] = existing == null ? null : existing.copy();
        }
        if (drops == null) return planned;

        for (ItemStack drop : drops) {
            if (drop == null || drop.stackSize <= 0) continue;
            int remaining = drop.stackSize;
            for (ItemStack existing : planned) {
                if (existing == null || !GTUtility.areStacksEqual(existing, drop)) continue;
                int inserted = Math.min(remaining, existing.getMaxStackSize() - existing.stackSize);
                existing.stackSize += inserted;
                remaining -= inserted;
                if (remaining == 0) break;
            }
            for (int i = 0; i < planned.length && remaining > 0; i++) {
                if (planned[i] != null) continue;
                planned[i] = drop.copy();
                planned[i].stackSize = Math.min(remaining, drop.getMaxStackSize());
                remaining -= planned[i].stackSize;
            }
            if (remaining > 0) return null;
        }
        return planned;
    }

    private void commitOutputPlan(ItemStack[] plannedOutputs) {
        for (int i = 0; i < plannedOutputs.length; i++) {
            mInventory[getOutputSlot() + i] = plannedOutputs[i];
        }
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        if (minerTier == 1) {
            builder.widget(createSteamProgressBar(builder));
        }

        builder.widget(createItemAutoOutputButton());

        BasicUIProperties uiProperties = getUIProperties();
        addIOSlots(builder, uiProperties);

        if (minerTier > 1) {
            builder.widget(createChargerSlot(79, 62));
        }

        addProgressBar(builder, uiProperties);

        builder.widget(createMuffleButton());

        builder.widget(
            createErrorStatusArea(
                builder,
                isSteampowered() ? GTUITextures.PICTURE_STALLED_STEAM : GTUITextures.PICTURE_STALLED_ELECTRICITY));
    }

    // ==================== Energy (abstract, implemented by subclasses) ====================

    protected abstract boolean hasEnoughEnergy(IGregTechTileEntity aBaseMetaTileEntity, int aRequiredEU);

    protected abstract void consumeEnergy(IGregTechTileEntity aBaseMetaTileEntity, int aEU);

    protected abstract long getAvailableEnergy(IGregTechTileEntity aBaseMetaTileEntity);

    protected abstract String getEnergyDisplayString();

    // ==================== Lifecycle ====================

    @Override
    public void onRemoval() {
        releaseLoadedChunk();
        super.onRemoval();
    }

    @Override
    public void inValidate() {
        releaseLoadedChunk();
        super.inValidate();
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("ETHBurnTime", burnTime);
        aNBT.setInteger("radiusConfig", radiusConfig);
        if (lastTargetKey != null) aNBT.setString("ETHTargetKey", lastTargetKey);
        aNBT.setInteger("ETHTargetDim", targetDimId);
        aNBT.setInteger("ETHTargetX", targetX);
        aNBT.setInteger("ETHTargetY", targetY);
        aNBT.setInteger("ETHTargetZ", targetZ);
        aNBT.setInteger("ETHCurrentScanY", currentScanY);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        burnTime = aNBT.getInteger("ETHBurnTime");
        if (aNBT.hasKey("radiusConfig")) {
            int saved = aNBT.getInteger("radiusConfig");
            if (saved >= 0 && saved <= RADIUS[rangeTier]) radiusConfig = saved;
        }
        lastTargetKey = aNBT.getString("ETHTargetKey");
        targetDimId = aNBT.getInteger("ETHTargetDim");
        targetX = aNBT.getInteger("ETHTargetX");
        targetY = aNBT.getInteger("ETHTargetY");
        targetZ = aNBT.getInteger("ETHTargetZ");
        targetChunk = new ChunkCoordIntPair(targetX >> 4, targetZ >> 4);
        currentScanY = aNBT.hasKey("ETHCurrentScanY") ? aNBT.getInteger("ETHCurrentScanY") : targetY;
        resetScanPlane();
        scanPlaneActive = true;
    }
}
