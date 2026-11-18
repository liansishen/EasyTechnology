package com.hepdd.easytech.api.objects;

final class PortableCraftingStationSlotRules {

    private PortableCraftingStationSlotRules() {}

    /**
     * Maps {@code InventoryPlayer} slot index ({@code 0..8} hotbar, {@code 9..35} main inventory) to the container
     * slot number in {@link PortableCraftingStationContainer} ({@code 0} result, {@code 1..9} crafting grid,
     * {@code 10..36} main inventory, {@code 37..45} hotbar).
     */
    static int computeLockedSlotNumber(int openerSlot) {
        if (openerSlot >= 0 && openerSlot < 9) {
            return openerSlot + 37;
        }
        if (openerSlot >= 9 && openerSlot < 36) {
            return openerSlot + 1;
        }
        return -1;
    }

    static boolean shouldBlockSlotClick(int slotId, int clickedButton, int mode, int openerSlot, int lockedSlotIndex) {
        if (lockedSlotIndex >= 0 && slotId == lockedSlotIndex) {
            return true;
        }
        return mode == 2 && openerSlot >= 0 && openerSlot < 9 && clickedButton == openerSlot;
    }
}
