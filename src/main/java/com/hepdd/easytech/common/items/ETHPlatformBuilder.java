package com.hepdd.easytech.common.items;

import java.awt.*;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Unique;

import com.gtnewhorizon.structurelib.util.Vec3Impl;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.items.GTGenericItem;

public class ETHPlatformBuilder extends GTGenericItem {

    public ETHPlatformBuilder(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
        super(aUnlocalized, aEnglish, aEnglishTooltip);
    }

    private static final BoundHighlighter boundHighlighter = new BoundHighlighter();
    @Unique
    private static boolean isShowHighlight = false;
    @Unique
    private final BlockInfo[][] templatePlatform = new BlockInfo[16][16];

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote) {
            if (player.isSneaking()) {
                isShowHighlight = !isShowHighlight;
            }
        }
        return super.onItemRightClick(itemStackIn, worldIn, player);
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        // side 0:bottom,1:top,2:north,3:sourth,4:west,5:east
        if (world.isRemote) return false;
        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        int startX = 0, startY = 0, startZ = 0;
        if (player.isSneaking()) {
            startX = chunk.xPosition * 16;
            startY = y;
            startZ = chunk.zPosition * 16;
            boundHighlighter.dim = world.provider.dimensionId;
            boundHighlighter.pos1 = new Vec3Impl(startX, startY, startZ);
            boundHighlighter.pos2 = new Vec3Impl(startX + 16, startY + 1, startZ + 16);
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    templatePlatform[i][j] = new BlockInfo(
                        world.getBlock(startX + i, startY, startZ + j),
                        world.getBlockMetadata(startX + i, startY, startZ + j));
                }
            }
            player.addChatMessage(new ChatComponentText("已设置平台模板"));
        } else {
            switch (side) {
                case 0:
                    // bottom
                    startX = chunk.xPosition;
                    startY = y - 1;
                    startZ = chunk.zPosition;
                    break;
                case 1:
                    // top
                    startX = chunk.xPosition;
                    startY = y + 1;
                    startZ = chunk.zPosition;
                    break;
                case 2:
                    // north
                    if (z - 1 < chunk.zPosition * 16) {
                        startZ = chunk.zPosition - 1;
                    } else {
                        startZ = chunk.zPosition;
                    }
                    startX = chunk.xPosition;
                    startY = y;
                    break;
                case 3:
                    // sourth
                    if (z + 1 >= chunk.zPosition * 16 + 16) {
                        startZ = chunk.zPosition + 1;
                    } else {
                        startZ = chunk.zPosition;
                    }
                    startX = chunk.xPosition;
                    startY = y;
                    break;
                case 4:
                    // west
                    if (x - 1 < chunk.xPosition * 16) {
                        startX = chunk.xPosition - 1;
                    } else {
                        startX = chunk.xPosition;
                    }
                    startY = y;
                    startZ = chunk.zPosition;
                    break;
                case 5:
                    // east
                    if (x + 1 >= chunk.xPosition * 16 + 16) {
                        startX = chunk.xPosition + 1;
                    } else {
                        startX = chunk.xPosition;
                    }
                    startY = y;
                    startZ = chunk.zPosition;
                    break;
            }
            startX = startX * 16;
            startZ = startZ * 16;
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    if (templatePlatform[i][j] != null) {
                        world.setBlock(
                            startX + i,
                            startY,
                            startZ + j,
                            templatePlatform[i][j].block,
                            templatePlatform[i][j].meta,
                            2);
                    }
                }
            }
        }
        return true;
    }

    private static class BlockInfo {

        public Block block;
        public int meta;

        public BlockInfo() {
            this.block = Blocks.air;
            this.meta = 0;
        }

        public BlockInfo(Block block1, int meta1) {
            this.block = block1;
            this.meta = meta1;
        }
    }

    public static class EventHandler {

        @SuppressWarnings("unused")
        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public void onRenderWorldLast(RenderWorldLastEvent e) {
            ETHPlatformBuilder.boundHighlighter.renderHighlightedBlock(e);
        }
    }

    private static class BoundHighlighter {

        public Vec3Impl pos1;
        public Vec3Impl pos2;
        public int dim;

        @SideOnly(Side.CLIENT)
        private void renderHighlightedBlock(RenderWorldLastEvent event) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem()
                .getItem() instanceof ETHPlatformBuilder)) {
                return;
            }

            if (pos1 == null || pos2 == null || !ETHPlatformBuilder.isShowHighlight) {
                return;
            }

            int dimension = mc.theWorld.provider.dimensionId;

            if (dimension != dim) {
                pos1 = null;
                pos2 = null;
                return;
            }

            EntityPlayerSP p = mc.thePlayer;
            double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * event.partialTicks;
            double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * event.partialTicks;
            double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * event.partialTicks;

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glLineWidth(3);
            GL11.glTranslated(-doubleX, -doubleY, -doubleZ);

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            renderHighLightedArenaOutline(pos1.get0(), pos1.get1(), pos1.get2(), pos2.get0(), pos2.get1(), pos2.get2());

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }

        @SideOnly(Side.CLIENT)
        static void renderHighLightedArenaOutline(double x1, double y1, double z1, double x2, double y2, double z2) {
            final Tessellator tess = Tessellator.instance;
            tess.startDrawing(GL11.GL_LINE_STRIP);

            tess.addVertex(x1, y1, z1);
            tess.addVertex(x1, y2, z1);
            tess.addVertex(x1, y2, z2);
            tess.addVertex(x1, y1, z2);
            tess.addVertex(x1, y1, z1);

            tess.addVertex(x2, y1, z1);
            tess.addVertex(x2, y2, z1);
            tess.addVertex(x2, y2, z2);
            tess.addVertex(x2, y1, z2);
            tess.addVertex(x2, y1, z1);

            tess.addVertex(x1, y1, z1);
            tess.addVertex(x2, y1, z1);
            tess.addVertex(x2, y1, z2);
            tess.addVertex(x1, y1, z2);
            tess.addVertex(x1, y2, z2);
            tess.addVertex(x2, y2, z2);
            tess.addVertex(x2, y2, z1);
            tess.addVertex(x2, y1, z1);
            tess.addVertex(x1, y1, z1);
            tess.addVertex(x2, y1, z1);
            tess.addVertex(x2, y2, z1);
            tess.addVertex(x1, y2, z1);
            tess.addVertex(x1, y2, z2);
            tess.addVertex(x2, y2, z2);
            tess.addVertex(x2, y1, z2);
            tess.addVertex(x1, y1, z2);

            tess.draw();
        }
    }
}
