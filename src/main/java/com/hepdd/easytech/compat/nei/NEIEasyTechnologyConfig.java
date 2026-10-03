package com.hepdd.easytech.compat.nei;

import com.hepdd.easytech.Tags;
import com.hepdd.easytech.api.gui.PortableCraftingStationGui;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.recipe.DefaultOverlayHandler;

public class NEIEasyTechnologyConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        // NEI ingredients start at (25, 6); the portable grid starts at (30, 17).
        API.registerGuiOverlay(PortableCraftingStationGui.class, "crafting", 5, 11);
        API.registerGuiOverlayHandler(PortableCraftingStationGui.class, new DefaultOverlayHandler(5, 11), "crafting");
    }

    @Override
    public String getName() {
        return "EasyTechnology";
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }
}
