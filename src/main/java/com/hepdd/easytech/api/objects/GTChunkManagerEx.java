package com.hepdd.easytech.api.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.hepdd.easytech.EasyTechnology;

import gregtech.api.enums.GTValues;
import gregtech.api.util.GTLog;

public class GTChunkManagerEx
    implements ForgeChunkManager.OrderedLoadingCallback, ForgeChunkManager.PlayerOrderedLoadingCallback {

    private final Map<TileEntity, ForgeChunkManager.Ticket> registeredTickets = new HashMap<>();
    public static GTChunkManagerEx instance = new GTChunkManagerEx();

    public static void init() {
        ForgeChunkManager.setForcedChunkLoadingCallback(EasyTechnology.instance, instance);
    }

    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {}

    /**
     * Determines if tickets should be kept. Based on if the ticket is a machine or a working-chunk ticket.
     * Working-chunk tickets are tossed and recreated when the machine reactivates.
     * Machine tickets are kept only if the config {@code alwaysReloadChunkloaders} is true.
     * Otherwise, machine chunks are tossed and recreated only when the machine reactivates,
     * similarly to a Passive Anchor.
     *
     * @param tickets        The tickets that you will want to select from.
     *                       The list is immutable and cannot be manipulated directly. Copy it first.
     * @param world          The world
     * @param maxTicketCount The maximum number of tickets that will be allowed.
     * @return list of tickets
     */

    @Override
    public List<ForgeChunkManager.Ticket> ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world,
        int maxTicketCount) {
        // Working-target tickets are transient and are reacquired when their source machine resumes.
        return new ArrayList<>();
    }

    /**
     * Determines if player tickets should be kept. This is where a ticket list per-player would be created and
     * maintained. When a player joins, an event occurs, their name/UUID/etc is compared against tickets on this list
     * and those tickets are reactivated.
     * Since that info would be maintained/dealt with on a per-player startup, the list returned back to Forge is empty.
     *
     * @param tickets The tickets that you will want to select from.
     *                The list is immutable and cannot be manipulated directly. Copy it first.
     * @param world   The world
     * @return the list of string-ticket paris
     */
    @Override
    public ListMultimap<String, ForgeChunkManager.Ticket> playerTicketsLoaded(
        ListMultimap<String, ForgeChunkManager.Ticket> tickets, World world) {
        // Not currently used, so just return an empty list.
        return ArrayListMultimap.create();
    }

    /**
     * Requests a chunk to be loaded for this machine. May pass a {@code null} chunk to load just the machine itself if
     * {@code alwaysReloadChunkloaders} is enabled in config.
     *
     * @param owner   owner of the TileEntity
     * @param chunkXZ chunk coordinates
     * @param player  player
     * @return if the chunk was loaded successfully
     */
    public static boolean requestPlayerChunkLoad(TileEntity owner, ChunkCoordIntPair chunkXZ, String player) {
        if (!GTValues.enableChunkloaders) return false;
        if (!GTValues.alwaysReloadChunkloaders && chunkXZ == null) return false;
        if (GTValues.debugChunkloaders && chunkXZ != null)
            GTLog.out.println("GTChunkManager: Chunk request: (" + chunkXZ.chunkXPos + ", " + chunkXZ.chunkZPos + ")");
        if (instance.registeredTickets.containsKey(owner)) {
            ForgeChunkManager.Ticket ticket = instance.registeredTickets.get(owner);
            if (ticket.world != owner.getWorldObj()) {
                releaseTicket(owner);
                return requestPlayerChunkLoad(owner, chunkXZ, player);
            }
            ForgeChunkManager.forceChunk(ticket, chunkXZ);
        } else {
            ForgeChunkManager.Ticket ticket;
            if (player.isEmpty()) ticket = ForgeChunkManager
                .requestTicket(EasyTechnology.instance, owner.getWorldObj(), ForgeChunkManager.Type.NORMAL);
            else ticket = ForgeChunkManager.requestPlayerTicket(
                EasyTechnology.instance,
                player,
                owner.getWorldObj(),
                ForgeChunkManager.Type.NORMAL);
            if (ticket == null) {
                if (GTValues.debugChunkloaders)
                    GTLog.out.println("GTChunkManager: ForgeChunkManager.requestTicket failed");
                return false;
            }
            if (GTValues.debugChunkloaders) GTLog.out.println(
                "GTChunkManager: ticket issued for machine at: (" + owner.xCoord
                    + ", "
                    + owner.yCoord
                    + ", "
                    + owner.zCoord
                    + ")");
            NBTTagCompound tag = ticket.getModData();
            tag.setInteger("OwnerX", owner.xCoord);
            tag.setInteger("OwnerY", owner.yCoord);
            tag.setInteger("OwnerZ", owner.zCoord);
            tag.setInteger("ETHOwnerDim", owner.getWorldObj().provider.dimensionId);
            tag.setInteger("ETHTargetDim", owner.getWorldObj().provider.dimensionId);
            tag.setInteger("ETHSchema", 1);
            tag.setString("ETHTicketType", "working_target");
            tag.setString(
                "OwnerType",
                owner.getClass()
                    .getSimpleName());
            ForgeChunkManager.forceChunk(ticket, chunkXZ);
            instance.registeredTickets.put(owner, ticket);
        }
        return true;
    }

    public static boolean requestPlayerChunkLoad(TileEntity owner, ChunkCoordIntPair chunkXZ, String player,
        int dimId) {
        if (!GTValues.enableChunkloaders) return false;
        if (!GTValues.alwaysReloadChunkloaders && chunkXZ == null) return false;
        World world = DimensionManager.getWorld(dimId);
        if (world == null) return false;
        if (GTValues.debugChunkloaders && chunkXZ != null)
            GTLog.out.println("GTChunkManager: Chunk request: (" + chunkXZ.chunkXPos + ", " + chunkXZ.chunkZPos + ")");
        if (instance.registeredTickets.containsKey(owner)) {
            ForgeChunkManager.Ticket ticket = instance.registeredTickets.get(owner);
            if (ticket.world != world) {
                releaseTicket(owner);
                return requestPlayerChunkLoad(owner, chunkXZ, player, dimId);
            }
            ForgeChunkManager.forceChunk(ticket, chunkXZ);
        } else {
            ForgeChunkManager.Ticket ticket;
            if (player.isEmpty())
                ticket = ForgeChunkManager.requestTicket(EasyTechnology.instance, world, ForgeChunkManager.Type.NORMAL);
            else ticket = ForgeChunkManager
                .requestPlayerTicket(EasyTechnology.instance, player, world, ForgeChunkManager.Type.NORMAL);
            if (ticket == null) {
                if (GTValues.debugChunkloaders)
                    GTLog.out.println("GTChunkManager: ForgeChunkManager.requestTicket failed");
                return false;
            }
            if (GTValues.debugChunkloaders) GTLog.out.println(
                "GTChunkManager: ticket issued for machine at: (" + owner.xCoord
                    + ", "
                    + owner.yCoord
                    + ", "
                    + owner.zCoord
                    + ")");
            NBTTagCompound tag = ticket.getModData();
            tag.setInteger("OwnerX", owner.xCoord);
            tag.setInteger("OwnerY", owner.yCoord);
            tag.setInteger("OwnerZ", owner.zCoord);
            tag.setInteger("ETHOwnerDim", owner.getWorldObj().provider.dimensionId);
            tag.setInteger("ETHTargetDim", dimId);
            tag.setInteger("ETHSchema", 1);
            tag.setString("ETHTicketType", "working_target");
            tag.setString(
                "OwnerType",
                owner.getClass()
                    .getSimpleName());
            ForgeChunkManager.forceChunk(ticket, chunkXZ);
            instance.registeredTickets.put(owner, ticket);
        }
        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean requestChunkLoad(TileEntity owner, ChunkCoordIntPair chunkXZ) {
        return requestPlayerChunkLoad(owner, chunkXZ, "");
    }

    public static void releaseChunk(TileEntity owner, ChunkCoordIntPair chunkXZ) {
        ForgeChunkManager.Ticket ticket = instance.registeredTickets.get(owner);
        if (ticket != null && chunkXZ != null) {
            if (GTValues.debugChunkloaders) GTLog.out
                .println("GTChunkManager: Chunk release: (" + chunkXZ.chunkXPos + ", " + chunkXZ.chunkZPos + ")");
            ForgeChunkManager.unforceChunk(ticket, chunkXZ);
        }
    }

    public static void releaseTicket(TileEntity owner) {
        ForgeChunkManager.Ticket ticket = instance.registeredTickets.remove(owner);
        if (ticket != null) {
            if (GTValues.debugChunkloaders) {
                GTLog.out.println(
                    "GTChunkManager: ticket released by machine at: (" + owner.xCoord
                        + ", "
                        + owner.yCoord
                        + ", "
                        + owner.zCoord
                        + ")");
                for (ChunkCoordIntPair chunk : ticket.getChunkList()) GTLog.out
                    .println("GTChunkManager: Chunk release: (" + chunk.chunkXPos + ", " + chunk.chunkZPos + ")");
            }
            ForgeChunkManager.releaseTicket(ticket);
        }
    }

    public static void onServerStopped() {
        instance.registeredTickets.clear();
    }

    public static void printTickets() {
        GTLog.out.println("GTChunkManager: Start forced chunks dump:");
        instance.registeredTickets.forEach((machine, ticket) -> {
            GTLog.out.print(
                "GTChunkManager: Chunks forced by the machine at (" + machine.xCoord
                    + ", "
                    + machine.yCoord
                    + ", "
                    + machine.zCoord
                    + ")");
            if (ticket.isPlayerTicket()) GTLog.out.print(" Owner: " + ticket.getPlayerName());
            GTLog.out.print(" :");
            for (ChunkCoordIntPair c : ticket.getChunkList()) {
                GTLog.out.print("(");
                GTLog.out.print(c.chunkXPos);
                GTLog.out.print(", ");
                GTLog.out.print(c.chunkZPos);
                GTLog.out.print("), ");
            }
        });
        GTLog.out.println("GTChunkManager: End forced chunks dump:");
    }
}
