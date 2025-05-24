package com.hepdd.easytech.loaders.preload;

import static galacticgreg.registry.GalacticGregRegistry.getModContainers;

import java.util.HashMap;
import java.util.Map;

import bartworks.common.configs.Configuration;
import galacticgreg.api.ModContainer;
import galacticgreg.api.ModDimensionDef;

public class ETHStatics implements Runnable {

    public static Map<String, String> DimMap;

    @Override
    public void run() {
        getDimMap();
    }

    public static void getDimMap() {
        DimMap = new HashMap<>();
        // vanilla dims
        DimMap.put("Nether", "-1");
        DimMap.put("Overworld", "0");
        DimMap.put("TheEnd", "1");
        DimMap.put("EndAsteroid", "1");
        // Twilight Forest
        DimMap.put("Twilight", "7");
        // ross dims
        DimMap.put("Ross128b", String.valueOf(Configuration.crossModInteractions.ross128BID));
        DimMap.put("Ross128ba", String.valueOf(Configuration.crossModInteractions.ross128BAID));

        for (ModContainer modContainer : getModContainers()) {
            for (ModDimensionDef dimDef : modContainer.getDimensionList()) {
                DimMap.put(dimDef.getDimensionName(), dimDef.getChunkProviderName());
            }
        }
    }
}
