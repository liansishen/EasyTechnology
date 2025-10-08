package com.hepdd.easytech.api.enums;

import java.util.Arrays;

import com.hepdd.easytech.network.PacketOpenCraftingStation;

import gregtech.api.net.GTPacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum ETHPacketTypes {

    OPEN_PORTABLE_CRAFTING_STATION(0, new PacketOpenCraftingStation());

    static {
        // Validate no duplicate IDs
        final ETHPacketTypes[] types = values();
        final Int2ObjectOpenHashMap<GTPacket> foundIds = new Int2ObjectOpenHashMap<>(types.length);
        for (ETHPacketTypes type : types) {
            final GTPacket previous = foundIds.get(type.id);
            if (previous != null) {
                throw new IllegalStateException(
                    "Duplicate packet IDs defined: " + type.id
                        + " for "
                        + type.getClass()
                        + " and "
                        + previous.getClass());
            }
            foundIds.put(type.id, type.referencePacket);
        }
    }

    public final byte id;
    public final GTPacket referencePacket;

    ETHPacketTypes(int id, GTPacket referencePacket) {
        if (((int) (byte) id) != id) {
            throw new IllegalArgumentException("Value outside of byte normal range: " + id);
        }
        this.id = (byte) id;
        this.referencePacket = referencePacket;
    }

    public static GTPacket[] referencePackets() {
        return Arrays.stream(values())
            .map(p -> p.referencePacket)
            .toArray(GTPacket[]::new);
    }
}
