package com.hepdd.easytech.common.tileentities.machines.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import com.gtnewhorizons.modularui.api.drawable.FallbackableUITexture;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.recipe.BasicUIProperties;
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

import com.hepdd.easytech.api.objects.GTChunkManagerEx;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.SoundResource;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.misc.IDrillingLogicDelegateOwner;
import gregtech.common.ores.OreManager;

import static gregtech.api.enums.Mods.GregTech;

public abstract class ETHAbstractEntangledMiner extends MTEBasicMachine implements IDrillingLogicDelegateOwner {

    protected static final int[] RADIUS = { 16, 16, 16, 24, 32 };
    protected static final int[] SPEED = { 160, 160, 160, 80, 40 };
    protected static final int[] ENERGY = { 10, 16, 32, 128, 512 };

    protected final ArrayList<ChunkPosition> oreBlockPositions = new ArrayList<>();
    protected int mSpeed;
    protected int radiusConfig;

    protected ChunkCoordIntPair targetChunk;
    protected int targetDimId;
    protected int targetY;
    protected int currentScanY = 256;
    protected String lastTargetKey = "";
    protected ChunkCoordIntPair loadedChunk;
    protected final Random miningRng = new Random();
    protected int burnTime;

    protected ETHAbstractEntangledMiner(int aID, String aName, String aNameRegional, int aTier, int aInputSlots,
        int aOutputSlots, String[] aDescription) {
        super(aID, aName, aNameRegional, aTier, 1, aDescription, 1, aOutputSlots);
        mSpeed = SPEED[aTier];
        radiusConfig = RADIUS[aTier];
    }

    protected ETHAbstractEntangledMiner(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures,
        int aInputSlots, int aOutputSlots) {
        super(aName, aTier, 1, aDescription, aTextures, 1, aOutputSlots);
        mSpeed = SPEED[aTier];
        radiusConfig = RADIUS[aTier];
    }

    // ==================== IDrillingLogicDelegateOwner ====================

    @Override
    public int getMachineTier() {
        return mTier;
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
    public boolean pushOutputs(ItemStack aStack, int aAmount, boolean aAlsoPushToInputs, boolean aSimulate) {
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
        for (int i = getOutputSlot(); i < getOutputSlot() + 2; i++) {
            if (mInventory[i] != null) {
                return false;
            }
        }
        return true;
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
            mMaxProgresstime = 0;
            mProgresstime = 0;
            currentScanY = 256;
            lastTargetKey = "";
            return;
        }

        if (!aBaseMetaTileEntity.isAllowedToWork()) {
            mMaxProgresstime = 0;
            if (GTValues.debugBlockMiner) GTLog.out.println("MINER: Disabled");
            return;
        }

        if (!hasFreeSpace()) {
            mMaxProgresstime = 0;
            if (GTValues.debugBlockMiner) GTLog.out.println("MINER: No free space");
            return;
        }

        int requiredEU = ENERGY[mTier] * (mSpeed - mProgresstime);
        if (!hasEnoughEnergy(aBaseMetaTileEntity, requiredEU)) {
            mMaxProgresstime = 0;
            if (GTValues.debugBlockMiner) {
                GTLog.out.println("MINER: Not enough energy yet, want " + (ENERGY[mTier] * mSpeed) + " have "
                    + getAvailableEnergy(aBaseMetaTileEntity));
            }
            return;
        }

        if (currentScanY <= 0 && oreBlockPositions.isEmpty()) {
            aBaseMetaTileEntity.disableWorking();
            return;
        }

        mMaxProgresstime = mSpeed;
        consumeEnergy(aBaseMetaTileEntity, ENERGY[mTier]);

        if (mProgresstime == mSpeed - 1) {
            if (oreBlockPositions.isEmpty()) {
                currentScanY--;
                fillOreList();
            }

            if (oreBlockPositions.isEmpty()) return;

            ChunkPosition pos = oreBlockPositions.remove(0);
            int worldX = targetChunk.chunkXPos * 16 + pos.chunkPosX;
            int worldY = pos.chunkPosY;
            int worldZ = targetChunk.chunkZPos * 16 + pos.chunkPosZ;

            World world = loadTargetChunk(worldX, worldZ);
            if (world != null) {
                Block block = world.getBlock(worldX, worldY, worldZ);
                int meta = world.getBlockMetadata(worldX, worldY, worldZ);
                if (GTUtility.isOre(block, meta)) {
                    List<ItemStack> drops = OreManager.mineBlock(
                        miningRng, world, worldX, worldY, worldZ, false, mTier, true, true);
                    if (drops != null) {
                        for (ItemStack drop : drops) {
                            pushOutputs(drop.copy(), drop.stackSize, true, false);
                        }
                    }
                    OreManager.mineBlock(miningRng, world, worldX, worldY, worldZ, false, mTier, false, true);
                }
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
                if (radiusConfig < 0) radiusConfig = RADIUS[mTier];
            } else {
                if (radiusConfig <= RADIUS[mTier]) radiusConfig++;
                if (radiusConfig > RADIUS[mTier]) radiusConfig = 0;
            }
            GTUtility.sendChatTrans(
                aPlayer,
                "GT5U.machines.workareaset.s",
                radiusConfig * 2 + 1,
                radiusConfig * 2 + 1);
            fillOreList();
        }
    }

    // ==================== Ore scanning ====================

    protected void fillOreList() {
        if (!updateTargetFromCard()) return;

        oreBlockPositions.clear();
        World world = DimensionManager.getWorld(targetDimId);
        if (world == null) return;

        int baseX = targetChunk.chunkXPos * 16;
        int baseZ = targetChunk.chunkZPos * 16;

        for (int dx = -radiusConfig; dx <= radiusConfig; dx++) {
            for (int dz = -radiusConfig; dz <= radiusConfig; dz++) {
                int worldX = baseX + dx;
                int worldZ = baseZ + dz;
                Block block = world.getBlock(worldX, currentScanY, worldZ);
                int meta = world.getBlockMetadata(worldX, currentScanY, worldZ);
                if (GTUtility.isOre(block, meta)) {
                    oreBlockPositions.add(new ChunkPosition(dx, currentScanY, dz));
                }
            }
        }
    }

    // ==================== Entangled card target ====================

    protected boolean updateTargetFromCard() {
        ItemStack card = getInputAt(0);
        if (card == null || !(card.getItem() instanceof ETHEntangledCard)) return false;
        NBTTagCompound tag = card.getTagCompound();
        if (tag == null || !tag.hasKey("dimId")) return false;

        String key = tag.getInteger("dimId") + ":" + tag.getInteger("x") + ":" + tag.getInteger("y") + ":"
            + tag.getInteger("z");
        if (!Objects.equals(key, lastTargetKey)) {
            lastTargetKey = key;
            targetDimId = tag.getInteger("dimId");
            targetChunk = new ChunkCoordIntPair(tag.getInteger("x") >> 4, tag.getInteger("z") >> 4);
            targetY = tag.getInteger("y");
            currentScanY = targetY;
            oreBlockPositions.clear();
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
        if (world != null) {
            if (GTChunkManagerEx.requestPlayerChunkLoad(
                (TileEntity) getBaseMetaTileEntity(), chunk, "", targetDimId)) {
                loadedChunk = chunk;
            }
        }
        return world;
    }

    protected void releaseLoadedChunk() {
        if (loadedChunk != null && getBaseMetaTileEntity() instanceof TileEntity tileEntity) {
            GTChunkManagerEx.releaseTicket(tileEntity);
            loadedChunk = null;
        }
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        if (mTier == 1) {
            builder.widget(createSteamProgressBar(builder));
        }

        builder.widget(createItemAutoOutputButton());

        BasicUIProperties uiProperties = getUIProperties();
        addIOSlots(builder, uiProperties);

        if (mTier > 2) {
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
        aNBT.setInteger("ETHTargetChunkX", targetChunk != null ? targetChunk.chunkXPos : 0);
        aNBT.setInteger("ETHTargetChunkZ", targetChunk != null ? targetChunk.chunkZPos : 0);
        aNBT.setInteger("ETHTargetY", targetY);
        aNBT.setInteger("ETHCurrentScanY", currentScanY);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        burnTime = aNBT.getInteger("ETHBurnTime");
        if (aNBT.hasKey("radiusConfig")) {
            int saved = aNBT.getInteger("radiusConfig");
            if (saved >= 0 && saved <= RADIUS[mTier]) radiusConfig = saved;
        }
        lastTargetKey = aNBT.getString("ETHTargetKey");
        targetDimId = aNBT.getInteger("ETHTargetDim");
        targetChunk = new ChunkCoordIntPair(aNBT.getInteger("ETHTargetChunkX"), aNBT.getInteger("ETHTargetChunkZ"));
        targetY = aNBT.getInteger("ETHTargetY");
        currentScanY = aNBT.hasKey("ETHCurrentScanY") ? aNBT.getInteger("ETHCurrentScanY") : targetY;
    }
}
