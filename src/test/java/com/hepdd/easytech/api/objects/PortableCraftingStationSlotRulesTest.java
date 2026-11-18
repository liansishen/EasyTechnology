package com.hepdd.easytech.api.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PortableCraftingStationSlotRulesTest {

    @Test
    public void testLockedSlotMapping() {
        // Hotbar 0..8 -> Container slots 37..45
        for (int hotbar = 0; hotbar < 9; hotbar++) {
            assertEquals(hotbar + 37, PortableCraftingStationSlotRules.computeLockedSlotNumber(hotbar));
        }
        // Main inventory 9..35 -> Container slots 10..36
        for (int inv = 9; inv < 36; inv++) {
            assertEquals(inv + 1, PortableCraftingStationSlotRules.computeLockedSlotNumber(inv));
        }
        // Out of bounds -> -1
        assertEquals(-1, PortableCraftingStationSlotRules.computeLockedSlotNumber(-1));
        assertEquals(-1, PortableCraftingStationSlotRules.computeLockedSlotNumber(36));
    }

    @Test
    public void testBlocksAllInteractionsTargetingLockedMainInventorySlot() {
        int openerSlot = 15; // Main inventory
        int lockedSlot = PortableCraftingStationSlotRules.computeLockedSlotNumber(openerSlot);
        assertEquals(16, lockedSlot);

        // Hovering over locked slot in main inventory and pressing hotbar swap key 0..8 (mode == 2) MUST be blocked
        for (int key = 0; key < 9; key++) {
            assertTrue(
                PortableCraftingStationSlotRules.shouldBlockSlotClick(lockedSlot, key, 2, openerSlot, lockedSlot));
        }
        // Pickup, shift-click, throw, drag, pickup-all on locked slot MUST be blocked
        for (int mode : new int[] { 0, 1, 3, 4, 5, 6 }) {
            assertTrue(
                PortableCraftingStationSlotRules.shouldBlockSlotClick(lockedSlot, 0, mode, openerSlot, lockedSlot));
        }
        // Swapping two unlocked slots when station is in main inventory is allowed
        assertFalse(PortableCraftingStationSlotRules.shouldBlockSlotClick(10, 2, 2, openerSlot, lockedSlot));
    }

    @Test
    public void testBlocksHotbarSwapWhenStationInHotbar() {
        int openerSlot = 3; // Hotbar slot 3
        int lockedSlot = PortableCraftingStationSlotRules.computeLockedSlotNumber(openerSlot);
        assertEquals(40, lockedSlot);

        // Hovering over crafting grid slot 5 and pressing hotbar key 3 (mode == 2) MUST be blocked
        assertTrue(PortableCraftingStationSlotRules.shouldBlockSlotClick(5, 3, 2, openerSlot, lockedSlot));
        // Hovering over crafting grid slot 5 and pressing hotbar key 4 is allowed
        assertFalse(PortableCraftingStationSlotRules.shouldBlockSlotClick(5, 4, 2, openerSlot, lockedSlot));
        // Clicking directly on hotbar locked slot 40 in any mode MUST be blocked
        assertTrue(PortableCraftingStationSlotRules.shouldBlockSlotClick(40, 0, 0, openerSlot, lockedSlot));
        assertTrue(PortableCraftingStationSlotRules.shouldBlockSlotClick(40, 1, 4, openerSlot, lockedSlot));
    }
}
