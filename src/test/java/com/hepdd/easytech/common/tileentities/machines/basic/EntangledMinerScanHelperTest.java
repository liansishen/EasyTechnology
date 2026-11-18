package com.hepdd.easytech.common.tileentities.machines.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class EntangledMinerScanHelperTest {

    private static long packBlockXZ(int x, int z) {
        return (((long) x) << 32) | (z & 0xffffffffL);
    }

    @Test
    public void testFullPlaneCoverageAcrossRadiiAndNegativeCoordinates() {
        int[] radii = { 0, 1, 8, 16, 24, 32 };
        int[][] centers = { { 0, 0 }, { -1, -1 }, { 15, 16 }, { -16, -17 }, { 123, -456 }, { -1024, 2047 } };

        for (int radius : radii) {
            int side = radius * 2 + 1;
            int expectedBlocks = side * side;
            for (int[] center : centers) {
                int tx = center[0];
                int tz = center[1];
                for (boolean reverse : new boolean[] { false, true }) {
                    verifyBatchedScan(tx, tz, radius, reverse, expectedBlocks);
                }
            }
        }
    }

    private static void verifyBatchedScan(int targetX, int targetZ, int radius, boolean reverseOrder,
        int expectedBlocks) {
        int totalChunks = EntangledMinerScanHelper.totalChunks(targetX, targetZ, radius);
        assertTrue(totalChunks >= 1);

        int scanChunkCursor = 0;
        int scanBlockInChunkCursor = 0;
        Set<Long> visitedBlocks = new HashSet<>(expectedBlocks * 2);

        boolean hasPrevChunk = false;
        int prevChunkX = 0;
        int prevChunkZ = 0;
        int chunkSwitches = 0;

        // Simulate 64-blocks-per-tick batched execution as in ETHAbstractEntangledMiner#scanOreBatch
        while (scanChunkCursor < totalChunks) {
            int scannedThisTick = 0;
            while (scanChunkCursor < totalChunks && scannedThisTick < 64) {
                long packedChunk = EntangledMinerScanHelper
                    .chunkAtCursor(targetX, targetZ, radius, scanChunkCursor, reverseOrder);
                int chunkX = EntangledMinerScanHelper.unpackChunkX(packedChunk);
                int chunkZ = EntangledMinerScanHelper.unpackChunkZ(packedChunk);

                if (!hasPrevChunk) {
                    hasPrevChunk = true;
                    prevChunkX = chunkX;
                    prevChunkZ = chunkZ;
                } else if (chunkX != prevChunkX || chunkZ != prevChunkZ) {
                    int manhattan = Math.abs(chunkX - prevChunkX) + Math.abs(chunkZ - prevChunkZ);
                    assertEquals("Adjacent chunks in 2D snake order must share an edge", 1, manhattan);
                    chunkSwitches++;
                    prevChunkX = chunkX;
                    prevChunkZ = chunkZ;
                }

                int cMinX = EntangledMinerScanHelper.clampMinInChunk(targetX, radius, chunkX);
                int cMaxX = EntangledMinerScanHelper.clampMaxInChunk(targetX, radius, chunkX);
                int cMinZ = EntangledMinerScanHelper.clampMinInChunk(targetZ, radius, chunkZ);
                int cMaxZ = EntangledMinerScanHelper.clampMaxInChunk(targetZ, radius, chunkZ);
                int spanZ = cMaxZ - cMinZ + 1;
                int blocksInChunk = (cMaxX - cMinX + 1) * spanZ;

                while (scanBlockInChunkCursor < blocksInChunk && scannedThisTick < 64) {
                    int worldX = cMinX + (scanBlockInChunkCursor / spanZ);
                    int worldZ = cMinZ + (scanBlockInChunkCursor % spanZ);

                    assertEquals(chunkX, worldX >> 4);
                    assertEquals(chunkZ, worldZ >> 4);
                    assertTrue(worldX >= targetX - radius && worldX <= targetX + radius);
                    assertTrue(worldZ >= targetZ - radius && worldZ <= targetZ + radius);
                    assertTrue(
                        "Duplicate block visited: " + worldX + "," + worldZ,
                        visitedBlocks.add(packBlockXZ(worldX, worldZ)));

                    scanBlockInChunkCursor++;
                    scannedThisTick++;
                }

                if (scanBlockInChunkCursor >= blocksInChunk) {
                    scanChunkCursor++;
                    scanBlockInChunkCursor = 0;
                }
            }
        }

        assertEquals(expectedBlocks, visitedBlocks.size());
        assertEquals(totalChunks - 1, chunkSwitches);
    }

    @Test
    public void testZeroCrossLayerSwitchWhenTogglingReverseOrder() {
        int targetX = -19;
        int targetZ = 37;
        int radius = 32; // HV miner (65x65)
        int totalChunks = EntangledMinerScanHelper.totalChunks(targetX, targetZ, radius);
        assertTrue(totalChunks <= 25);

        long lastOfEvenLayer = EntangledMinerScanHelper.chunkAtCursor(targetX, targetZ, radius, totalChunks - 1, false);
        long firstOfOddLayer = EntangledMinerScanHelper.chunkAtCursor(targetX, targetZ, radius, 0, true);
        assertEquals(
            "Transition from even layer to odd layer should stay in the same chunk",
            lastOfEvenLayer,
            firstOfOddLayer);

        long lastOfOddLayer = EntangledMinerScanHelper.chunkAtCursor(targetX, targetZ, radius, totalChunks - 1, true);
        long firstOfNextEvenLayer = EntangledMinerScanHelper.chunkAtCursor(targetX, targetZ, radius, 0, false);
        assertEquals(
            "Transition from odd layer to next even layer should stay in the same chunk",
            lastOfOddLayer,
            firstOfNextEvenLayer);
    }

    @Test
    public void testReloadAtBedrockLayerZeroStillInProgressResumesScan() {
        // Case 1: New save while Y=0 scan is mid-plane or paused manually (ETHScanComplete=false)
        assertFalse(EntangledMinerScanHelper.isScanComplete(true, 0));
        int loadedY = EntangledMinerScanHelper.resolveLoadedScanY(true, 64, 0, true, false);
        assertEquals(0, loadedY);
        assertTrue(EntangledMinerScanHelper.shouldResumePlaneScan(true, loadedY, 64));
    }

    @Test
    public void testReloadWhenColumnScanCompletedStaysFinished() {
        // Case 2a: New save after Y=0 scan and ore queue are both exhausted (currentScanY = SCAN_COMPLETE_Y = -1)
        assertEquals(-1, EntangledMinerScanHelper.SCAN_COMPLETE_Y);
        assertTrue(EntangledMinerScanHelper.isScanComplete(true, EntangledMinerScanHelper.SCAN_COMPLETE_Y));

        int completedLoadedY = EntangledMinerScanHelper
            .resolveLoadedScanY(true, 64, EntangledMinerScanHelper.SCAN_COMPLETE_Y, true, true);
        assertEquals(EntangledMinerScanHelper.SCAN_COMPLETE_Y, completedLoadedY);
        assertFalse(EntangledMinerScanHelper.shouldResumePlaneScan(true, completedLoadedY, 64));

        // Case 2b: Defensive normalization when savedScanY is negative even if ETHScanComplete was false
        int negativeSavedY = EntangledMinerScanHelper.resolveLoadedScanY(true, 64, -5, true, false);
        assertEquals(EntangledMinerScanHelper.SCAN_COMPLETE_Y, negativeSavedY);
        assertFalse(EntangledMinerScanHelper.shouldResumePlaneScan(true, negativeSavedY, 64));
    }

    @Test
    public void testLegacySaveBackwardCompatibilityAtLayerZeroAndMidColumn() {
        // Case 3a: Ambiguous legacy save (no ETHScanComplete tag) at Y=0 with targetY > 0 keeps Y=0 so paused miners
        // rescan Y=0 upon re-enabling, while already-disabled miners remain idle until enabled
        int legacyLayerZeroY = EntangledMinerScanHelper.resolveLoadedScanY(true, 64, 0, false, false);
        assertEquals(0, legacyLayerZeroY);
        assertTrue(EntangledMinerScanHelper.shouldResumePlaneScan(true, legacyLayerZeroY, 64));

        // Case 3b: Legacy save mid-column (e.g. Y=12) resumes layer 12
        int legacyMidY = EntangledMinerScanHelper.resolveLoadedScanY(true, 64, 12, false, false);
        assertEquals(12, legacyMidY);
        assertTrue(EntangledMinerScanHelper.shouldResumePlaneScan(true, legacyMidY, 64));

        // Case 3c: Legacy save with card bound at Y <= 0 (nothing below targetY) migrates cleanly to complete
        int legacyTargetZeroY = EntangledMinerScanHelper.resolveLoadedScanY(true, 0, 0, false, false);
        assertEquals(EntangledMinerScanHelper.SCAN_COMPLETE_Y, legacyTargetZeroY);
        assertFalse(EntangledMinerScanHelper.shouldResumePlaneScan(true, legacyTargetZeroY, 0));

        // Case 3d: Legacy save right after card insertion (savedScanY == targetY) does not scan targetY itself
        int legacyFreshCardY = EntangledMinerScanHelper.resolveLoadedScanY(true, 64, 64, false, false);
        assertEquals(64, legacyFreshCardY);
        assertFalse(EntangledMinerScanHelper.shouldResumePlaneScan(true, legacyFreshCardY, 64));

        // Case 3e: Legacy save with negative savedScanY normalizes to complete
        int legacyNegativeY = EntangledMinerScanHelper.resolveLoadedScanY(true, 64, -1, false, false);
        assertEquals(EntangledMinerScanHelper.SCAN_COMPLETE_Y, legacyNegativeY);
        assertFalse(EntangledMinerScanHelper.shouldResumePlaneScan(true, legacyNegativeY, 64));
    }

    @Test
    public void testLegacyPausedMinerAtLayerZeroTransitionsToCompleteAfterRescan() {
        // Simulate upgrading a legacy save paused at Y=0, rescanning Y=0, and persisting the new completion marker
        int migratedY = EntangledMinerScanHelper.resolveLoadedScanY(true, 64, 0, false, false);
        assertEquals(0, migratedY);
        assertFalse(EntangledMinerScanHelper.isScanComplete(true, migratedY));
        assertTrue(EntangledMinerScanHelper.shouldResumePlaneScan(true, migratedY, 64));

        // Once Y=0 plane scan finishes and ore queue is empty, miner sets SCAN_COMPLETE_Y and writes ETHScanComplete
        int afterRescanY = EntangledMinerScanHelper.SCAN_COMPLETE_Y;
        boolean savedCompleteFlag = EntangledMinerScanHelper.isScanComplete(true, afterRescanY);
        assertTrue(savedCompleteFlag);

        int subsequentReloadY = EntangledMinerScanHelper
            .resolveLoadedScanY(true, 64, afterRescanY, true, savedCompleteFlag);
        assertEquals(EntangledMinerScanHelper.SCAN_COMPLETE_Y, subsequentReloadY);
        assertFalse(EntangledMinerScanHelper.shouldResumePlaneScan(true, subsequentReloadY, 64));
    }

    @Test
    public void testReloadRightAfterCardInsertOrWithoutTargetDoesNotScanTargetLayer() {
        // Case 4a: Card just inserted while paused/full (currentScanY == targetY, before first currentScanY--)
        assertFalse(EntangledMinerScanHelper.isScanComplete(true, 64));
        int freshCardY = EntangledMinerScanHelper.resolveLoadedScanY(true, 64, 64, true, false);
        assertEquals(64, freshCardY);
        assertFalse(EntangledMinerScanHelper.shouldResumePlaneScan(true, freshCardY, 64));

        // Card bound at Y=0 just inserted in new save
        int freshZeroCardY = EntangledMinerScanHelper.resolveLoadedScanY(true, 0, 0, true, false);
        assertEquals(0, freshZeroCardY);
        assertFalse(EntangledMinerScanHelper.shouldResumePlaneScan(true, freshZeroCardY, 0));

        // Case 4b: No target card present
        assertFalse(EntangledMinerScanHelper.isScanComplete(false, -1));
        int noTargetY = EntangledMinerScanHelper.resolveLoadedScanY(false, 0, 256, true, false);
        assertEquals(256, noTargetY);
        assertFalse(EntangledMinerScanHelper.shouldResumePlaneScan(false, noTargetY, 0));
    }
}
