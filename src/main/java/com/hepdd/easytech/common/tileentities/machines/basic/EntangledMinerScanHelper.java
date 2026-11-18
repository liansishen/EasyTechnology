package com.hepdd.easytech.common.tileentities.machines.basic;

final class EntangledMinerScanHelper {

    static final int SCAN_COMPLETE_Y = -1;

    private EntangledMinerScanHelper() {}

    static boolean isScanComplete(boolean hasTarget, int currentScanY) {
        return hasTarget && currentScanY < 0;
    }

    static int resolveLoadedScanY(boolean hasTarget, int targetY, int savedScanY, boolean hasScanCompleteTag,
        boolean scanCompleteTag) {
        boolean scanFinished = hasTarget
            && (savedScanY < 0 || (hasScanCompleteTag ? scanCompleteTag : (savedScanY == 0 && targetY <= 0)));
        return scanFinished ? SCAN_COMPLETE_Y : savedScanY;
    }

    static boolean shouldResumePlaneScan(boolean hasTarget, int currentScanY, int targetY) {
        return hasTarget && currentScanY >= 0 && currentScanY < targetY;
    }

    static int totalChunks(int targetX, int targetZ, int radius) {
        int minChunkX = (targetX - radius) >> 4;
        int maxChunkX = (targetX + radius) >> 4;
        int minChunkZ = (targetZ - radius) >> 4;
        int maxChunkZ = (targetZ + radius) >> 4;
        return (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
    }

    /**
     * Maps {@code chunkCursor} in {@code [0, totalChunks)} to a 2D snake-ordered chunk coordinate
     * {@code (chunkX, chunkZ)} packed into a {@code long} ({@code chunkX} in high 32 bits, {@code chunkZ} in low 32
     * bits). Reversing {@code reverseOrder} between consecutive Y layers keeps the last chunk of layer Y identical to
     * the first chunk of layer Y-1.
     */
    static long chunkAtCursor(int targetX, int targetZ, int radius, int chunkCursor, boolean reverseOrder) {
        int minChunkX = (targetX - radius) >> 4;
        int maxChunkX = (targetX + radius) >> 4;
        int minChunkZ = (targetZ - radius) >> 4;
        int maxChunkZ = (targetZ + radius) >> 4;
        int spanX = maxChunkX - minChunkX + 1;
        int spanZ = maxChunkZ - minChunkZ + 1;
        int total = spanX * spanZ;

        int effectiveIdx = reverseOrder ? (total - 1 - chunkCursor) : chunkCursor;
        int rowX = effectiveIdx / spanZ;
        int colInRow = effectiveIdx % spanZ;
        int colZ = (rowX & 1) == 0 ? colInRow : (spanZ - 1 - colInRow);

        int chunkX = minChunkX + rowX;
        int chunkZ = minChunkZ + colZ;
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    static int unpackChunkX(long packedChunk) {
        return (int) (packedChunk >> 32);
    }

    static int unpackChunkZ(long packedChunk) {
        return (int) packedChunk;
    }

    static int clampMinInChunk(int targetCoord, int radius, int chunkCoord) {
        return Math.max(targetCoord - radius, chunkCoord << 4);
    }

    static int clampMaxInChunk(int targetCoord, int radius, int chunkCoord) {
        return Math.min(targetCoord + radius, (chunkCoord << 4) + 15);
    }
}
