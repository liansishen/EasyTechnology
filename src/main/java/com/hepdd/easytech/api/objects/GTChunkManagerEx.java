package com.hepdd.easytech.api.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.hepdd.easytech.EasyTechnology;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregtech.api.enums.GTValues;

public class GTChunkManagerEx
    implements ForgeChunkManager.OrderedLoadingCallback, ForgeChunkManager.PlayerOrderedLoadingCallback {

    private final Map<TileEntity, ForgeChunkManager.Ticket> registeredTickets = new HashMap<>();
    public static GTChunkManagerEx instance = new GTChunkManagerEx();

    public static void init() {
        ForgeChunkManager.setForcedChunkLoadingCallback(EasyTechnology.instance, instance);
        MinecraftForge.EVENT_BUS.register(instance);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world == null || event.world.isRemote) return;
        Iterator<Map.Entry<TileEntity, ForgeChunkManager.Ticket>> it = registeredTickets.entrySet()
            .iterator();
        while (it.hasNext()) {
            Map.Entry<TileEntity, ForgeChunkManager.Ticket> entry = it.next();
            TileEntity owner = entry.getKey();
            ForgeChunkManager.Ticket ticket = entry.getValue();
            if (ticket == null) {
                it.remove();
                continue;
            }
            if (ticket.world == event.world || (owner != null && owner.getWorldObj() == event.world)) {
                if (ticket.world != event.world) {
                    ForgeChunkManager.releaseTicket(ticket);
                }
                it.remove();
            }
        }
    }

    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {}

    @Override
    public List<ForgeChunkManager.Ticket> ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world,
        int maxTicketCount) {
        // Working-target tickets are transient and are reacquired when their source machine resumes.
        return new ArrayList<>();
    }

    @Override
    public ListMultimap<String, ForgeChunkManager.Ticket> playerTicketsLoaded(
        ListMultimap<String, ForgeChunkManager.Ticket> tickets, World world) {
        return ArrayListMultimap.create();
    }

    public static boolean requestPlayerChunkLoad(TileEntity owner, ChunkCoordIntPair chunkXZ, String player) {
        if (owner == null || owner.getWorldObj() == null) return false;
        return requestPlayerChunkLoad(owner, chunkXZ, player, owner.getWorldObj().provider.dimensionId);
    }

    public static boolean requestPlayerChunkLoad(TileEntity owner, ChunkCoordIntPair chunkXZ, String player,
        int dimId) {
        if (owner == null) return false;
        if (!GTValues.enableChunkloaders) return false;
        if (!GTValues.alwaysReloadChunkloaders && chunkXZ == null) return false;
        World world = resolveWorld(dimId);
        if (world == null) return false;
        if (GTValues.debugChunkloaders && chunkXZ != null) {
            EasyTechnology.LOG
                .debug("GTChunkManager: Chunk request: (" + chunkXZ.chunkXPos + ", " + chunkXZ.chunkZPos + ")");
        }
        ForgeChunkManager.Ticket ticket = instance.registeredTickets.get(owner);
        if (ticket != null && ticket.world != world) {
            releaseTicket(owner);
            ticket = null;
        }
        if (ticket == null) {
            ticket = createTicket(owner, world, player, dimId);
            if (ticket == null) return false;
            instance.registeredTickets.put(owner, ticket);
        }
        if (chunkXZ != null) {
            ForgeChunkManager.forceChunk(ticket, chunkXZ);
        }
        return true;
    }

    /**
     * Switches the forced target chunk on a single reusable ticket: forces {@code (newChunkX, newChunkZ)} first and
     * then unforces {@code (oldChunkX, oldChunkZ)} without releasing and recreating the ticket.
     */
    public static boolean switchChunkLoad(TileEntity owner, int newChunkX, int newChunkZ, int oldChunkX, int oldChunkZ,
        boolean hasOldChunk, int dimId) {
        if (owner == null || !GTValues.enableChunkloaders) return false;
        World world = resolveWorld(dimId);
        if (world == null) return false;

        ForgeChunkManager.Ticket ticket = instance.registeredTickets.get(owner);
        if (ticket != null && ticket.world != world) {
            releaseTicket(owner);
            ticket = null;
            hasOldChunk = false;
        }
        if (ticket != null && hasOldChunk && oldChunkX == newChunkX && oldChunkZ == newChunkZ) {
            return true;
        }
        if (ticket == null) {
            ticket = createTicket(owner, world, "", dimId);
            if (ticket == null) return false;
            instance.registeredTickets.put(owner, ticket);
            hasOldChunk = false;
        }

        ChunkCoordIntPair newChunk = new ChunkCoordIntPair(newChunkX, newChunkZ);
        ForgeChunkManager.forceChunk(ticket, newChunk);
        if (hasOldChunk && (oldChunkX != newChunkX || oldChunkZ != newChunkZ)) {
            ForgeChunkManager.unforceChunk(ticket, new ChunkCoordIntPair(oldChunkX, oldChunkZ));
        }
        return true;
    }

    private static World resolveWorld(int dimId) {
        World world = DimensionManager.getWorld(dimId);
        if (world == null && DimensionManager.isDimensionRegistered(dimId)) {
            DimensionManager.initDimension(dimId);
            world = DimensionManager.getWorld(dimId);
        }
        return world;
    }

    private static ForgeChunkManager.Ticket createTicket(TileEntity owner, World targetWorld, String player,
        int targetDimId) {
        ForgeChunkManager.Ticket ticket;
        if (player == null || player.isEmpty()) {
            ticket = ForgeChunkManager
                .requestTicket(EasyTechnology.instance, targetWorld, ForgeChunkManager.Type.NORMAL);
        } else {
            ticket = ForgeChunkManager
                .requestPlayerTicket(EasyTechnology.instance, player, targetWorld, ForgeChunkManager.Type.NORMAL);
        }
        if (ticket == null) {
            if (GTValues.debugChunkloaders) {
                EasyTechnology.LOG.debug("GTChunkManager: ForgeChunkManager.requestTicket failed");
            }
            return null;
        }
        if (GTValues.debugChunkloaders) {
            EasyTechnology.LOG.debug(
                "GTChunkManager: ticket issued for machine at: (" + owner.xCoord
                    + ", "
                    + owner.yCoord
                    + ", "
                    + owner.zCoord
                    + ")");
        }
        NBTTagCompound tag = ticket.getModData();
        tag.setInteger("OwnerX", owner.xCoord);
        tag.setInteger("OwnerY", owner.yCoord);
        tag.setInteger("OwnerZ", owner.zCoord);
        if (owner.getWorldObj() != null && owner.getWorldObj().provider != null) {
            tag.setInteger("ETHOwnerDim", owner.getWorldObj().provider.dimensionId);
        }
        tag.setInteger("ETHTargetDim", targetDimId);
        tag.setInteger("ETHSchema", 1);
        tag.setString("ETHTicketType", "working_target");
        tag.setString(
            "OwnerType",
            owner.getClass()
                .getSimpleName());
        return ticket;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean requestChunkLoad(TileEntity owner, ChunkCoordIntPair chunkXZ) {
        return requestPlayerChunkLoad(owner, chunkXZ, "");
    }

    public static void releaseChunk(TileEntity owner, ChunkCoordIntPair chunkXZ) {
        ForgeChunkManager.Ticket ticket = instance.registeredTickets.get(owner);
        if (ticket != null && chunkXZ != null) {
            if (GTValues.debugChunkloaders) {
                EasyTechnology.LOG
                    .debug("GTChunkManager: Chunk release: (" + chunkXZ.chunkXPos + ", " + chunkXZ.chunkZPos + ")");
            }
            ForgeChunkManager.unforceChunk(ticket, chunkXZ);
        }
    }

    public static boolean releaseTicket(TileEntity owner) {
        if (owner == null) return false;
        ForgeChunkManager.Ticket ticket = instance.registeredTickets.remove(owner);
        if (ticket != null) {
            if (GTValues.debugChunkloaders) {
                EasyTechnology.LOG.debug(
                    "GTChunkManager: ticket released by machine at: (" + owner.xCoord
                        + ", "
                        + owner.yCoord
                        + ", "
                        + owner.zCoord
                        + ")");
                for (ChunkCoordIntPair chunk : ticket.getChunkList()) {
                    EasyTechnology.LOG
                        .debug("GTChunkManager: Chunk release: (" + chunk.chunkXPos + ", " + chunk.chunkZPos + ")");
                }
            }
            ForgeChunkManager.releaseTicket(ticket);
            return true;
        }
        return false;
    }

    public static void onServerStopped() {
        instance.registeredTickets.clear();
    }

    public static void printTickets() {
        EasyTechnology.LOG.info("GTChunkManager: Start forced chunks dump:");
        instance.registeredTickets.forEach((machine, ticket) -> {
            StringBuilder message = new StringBuilder("GTChunkManager: Chunks forced by the machine at (")
                .append(machine.xCoord)
                .append(", ")
                .append(machine.yCoord)
                .append(", ")
                .append(machine.zCoord)
                .append(")");
            if (ticket.isPlayerTicket()) {
                message.append(" Owner: ")
                    .append(ticket.getPlayerName());
            }
            message.append(" :");
            for (ChunkCoordIntPair chunk : ticket.getChunkList()) {
                message.append('(')
                    .append(chunk.chunkXPos)
                    .append(", ")
                    .append(chunk.chunkZPos)
                    .append("), ");
            }
            EasyTechnology.LOG.info(message.toString());
        });
        EasyTechnology.LOG.info("GTChunkManager: End forced chunks dump:");
    }
}
