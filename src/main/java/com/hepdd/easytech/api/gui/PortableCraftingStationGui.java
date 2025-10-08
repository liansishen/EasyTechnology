package com.hepdd.easytech.api.gui;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.hepdd.easytech.api.objects.PortableCraftingStationContainer;

import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Optional;
import tconstruct.tools.logic.CraftingStationLogic;

@Optional.Interface(iface = "codechicken.nei.api.INEIGuiHandler", modid = "NotEnoughItems")
public class PortableCraftingStationGui extends GuiContainer implements INEIGuiHandler {

    /*
     * Slider/slots related. Taken & adapted from Tinkers Construct 1.12 under the MIT License
     */
    private static final ResourceLocation gui_inventory = new ResourceLocation("tinker", "textures/gui/generic.png");
    private static final ResourceLocation background = new ResourceLocation("tinker", "textures/gui/tinkertable.png");
    private static final ResourceLocation icons = new ResourceLocation("tinker", "textures/gui/icons.png");

    public boolean active;

    // Panel positions
    public String toolName;
    public String title, body;
    CraftingStationLogic logic;

    private int craftingLeft = 0;
    private int craftingTop = 0;
    private int craftingTextLeft = 0;

    public PortableCraftingStationGui(InventoryPlayer inventory, CraftingStationLogic logic, int slotId) {
        super(new PortableCraftingStationContainer(inventory, logic, slotId));
        this.logic = logic;

        title = "\u00A7n" + StatCollector.translateToLocal("gui.toolforge1");
        body = StatCollector.translateToLocal("gui.toolforge2");
        toolName = "";
    }

    @Override
    public void initGui() {
        super.initGui();

        this.xSize = 176;
        this.ySize = 166;

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        this.craftingLeft = this.guiLeft;
        this.craftingTop = this.guiTop;

        this.craftingTextLeft = 0;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        this.fontRendererObj
            .drawString(StatCollector.translateToLocal(logic.getInvName()), craftingTextLeft + 8, 6, 0x202020);
        this.fontRendererObj
            .drawString(StatCollector.translateToLocal("container.inventory"), craftingTextLeft + 8, 72, 0x202020);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

        // Draw the background
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager()
            .bindTexture(background);
        this.drawTexturedModalRect(this.craftingLeft, this.craftingTop, 0, 0, 176, 166);

        if (active) {
            this.drawTexturedModalRect(this.craftingLeft + 62, this.craftingTop, 0, 166, 112, 22);
        }

        this.mc.getTextureManager()
            .bindTexture(icons);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager()
            .bindTexture(gui_inventory);
    }

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        currentVisibility.showWidgets = width - xSize >= 107;

        if (guiLeft < 58) {
            currentVisibility.showStateButtons = false;
        }

        return currentVisibility;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return null;
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return Collections.emptyList();
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
        return false;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        final int cw = 0;
        if (y + h - 4 < guiTop || y + 4 > guiTop + ySize) return false;
        return x - w - 4 >= guiLeft + cw && x + 4 <= guiLeft + xSize + cw;
    }
}
