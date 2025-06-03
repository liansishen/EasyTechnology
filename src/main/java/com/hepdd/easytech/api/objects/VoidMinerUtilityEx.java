package com.hepdd.easytech.api.objects;

import static galacticgreg.registry.GalacticGregRegistry.getModContainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.item.ItemStack;

import bartworks.common.configs.Configuration;
import bartworks.system.oregen.BWOreLayer;
import bwcrossmod.galacticgreg.VoidMinerUtility;
import galacticgreg.GalacticGreg;
import galacticgreg.WorldgenOreLayerSpace;
import galacticgreg.WorldgenOreSmallSpace;
import galacticgreg.api.ModContainer;
import galacticgreg.api.ModDimensionDef;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.common.WorldgenGTOreLayer;

public class VoidMinerUtilityEx {

    public static final Map<Integer, VoidMinerUtility.DropMap> rawDropMapsByDimId = new HashMap<>();
    public static final Map<String, VoidMinerUtility.DropMap> rawDropMapsByChunkProviderName = new HashMap<>();

    public static void generateDropMaps() {
        // vanilla dims
        rawDropMapsByDimId.put(-1, getRawOreDropMapVanilla(-1));
        rawDropMapsByDimId.put(0, getRawOreDropMapVanilla(0));
        rawDropMapsByDimId.put(1, getRawOreDropMapVanilla(1));
        // Twilight Forest
        rawDropMapsByDimId.put(7, getRawOreDropMapVanilla(7));

        // ross dims
        rawDropMapsByDimId.put(
            Configuration.crossModInteractions.ross128BID,
            getRawDropMapRoss(Configuration.crossModInteractions.ross128BID));
        rawDropMapsByDimId.put(
            Configuration.crossModInteractions.ross128BAID,
            getRawDropMapRoss(Configuration.crossModInteractions.ross128BAID));

        // other space dims
        for (ModContainer modContainer : getModContainers()) {
            for (ModDimensionDef dimDef : modContainer.getDimensionList()) {
                rawDropMapsByChunkProviderName.put(dimDef.getChunkProviderName(), getRawDropMapSpace(dimDef));
            }
        }
    }

    private static VoidMinerUtility.DropMap getRawOreDropMapVanilla(int dimId) {
        VoidMinerUtility.DropMap dropMap = new VoidMinerUtility.DropMap();

        // Ore Veins
        Predicate<WorldgenGTOreLayer> oreLayerPredicate = makeOreLayerPredicate(dimId);
        WorldgenGTOreLayer.sList.stream()
            .filter(gt_worldgen -> gt_worldgen.mEnabled && oreLayerPredicate.test(gt_worldgen))
            .forEach(element -> {
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mPrimaryMeta % 1000)], 1),
                    element.mWeight);
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mSecondaryMeta % 1000)], 1),
                    element.mWeight);
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mSporadicMeta % 1000)], 1),
                    element.mWeight);
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[(element.mBetweenMeta % 1000)], 1),
                    element.mWeight);
            });

        return dropMap;
    }

    private static VoidMinerUtility.DropMap getRawDropMapRoss(int aID) {
        VoidMinerUtility.DropMap dropMap = new VoidMinerUtility.DropMap();
        for (BWOreLayer oreLayer : BWOreLayer.sList) {
            if (oreLayer.mEnabled && oreLayer.isGenerationAllowed("", aID, 0)) {
                List<ItemStack> data = oreLayer.getStacks();
                if (data == null || data.isEmpty()) continue;
                try {
                    dropMap.addDrop(
                        GTOreDictUnificator.get(
                            OrePrefixes.rawOre,
                            GregTechAPI.sGeneratedMaterials[data.get(0)
                                .getItemDamage()],
                            1),
                        oreLayer.mWeight);
                    dropMap.addDrop(
                        GTOreDictUnificator.get(
                            OrePrefixes.rawOre,
                            GregTechAPI.sGeneratedMaterials[data.get(1)
                                .getItemDamage()],
                            1),
                        oreLayer.mWeight);
                    dropMap.addDrop(
                        GTOreDictUnificator.get(
                            OrePrefixes.rawOre,
                            GregTechAPI.sGeneratedMaterials[data.get(2)
                                .getItemDamage()],
                            1),
                        oreLayer.mWeight / 8f);
                    dropMap.addDrop(
                        GTOreDictUnificator.get(
                            OrePrefixes.rawOre,
                            GregTechAPI.sGeneratedMaterials[data.get(3)
                                .getItemDamage()],
                            1),
                        oreLayer.mWeight / 8f);
                } catch (Exception ignored) {}
            }
        }
        return dropMap;
    }

    private static VoidMinerUtility.DropMap getRawDropMapSpace(ModDimensionDef finalDef) {
        VoidMinerUtility.DropMap dropMap = new VoidMinerUtility.DropMap();

        // Normal Ore Veins
        GalacticGreg.oreVeinWorldgenList.stream()
            .filter(
                gt_worldgen -> gt_worldgen.mEnabled && gt_worldgen instanceof WorldgenOreLayerSpace oreLayerSpace
                    && oreLayerSpace.isEnabledForDim(finalDef))
            .map(gt_worldgen -> (WorldgenOreLayerSpace) gt_worldgen)
            .forEach(element -> {
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[element.mPrimaryMeta % 1000], 1),
                    element.mWeight);
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[element.mSecondaryMeta % 1000], 1),
                    element.mWeight);
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[element.mSporadicMeta % 1000], 1),
                    element.mWeight / 8f);
                dropMap.addDrop(
                    GTOreDictUnificator
                        .get(OrePrefixes.rawOre, GregTechAPI.sGeneratedMaterials[element.mBetweenMeta % 1000], 1),
                    element.mWeight / 8f);
            });

        // Normal Small Ores
        GalacticGreg.smallOreWorldgenList.stream()
            .filter(
                gt_worldgen -> gt_worldgen.mEnabled && gt_worldgen instanceof WorldgenOreSmallSpace oreSmallPiecesSpace
                    && oreSmallPiecesSpace.isEnabledForDim(finalDef))
            .map(gt_worldgen -> (WorldgenOreSmallSpace) gt_worldgen)
            .forEach(element -> dropMap.addDrop(element.mMeta, element.mAmount, false));
        return dropMap;
    }

    private static Predicate<WorldgenGTOreLayer> makeOreLayerPredicate(int dimensionId) {
        return switch (dimensionId) {
            case -1 -> gt_worldgen -> gt_worldgen.mNether;
            case 0 -> gt_worldgen -> gt_worldgen.mOverworld;
            case 1 -> gt_worldgen -> gt_worldgen.mEnd || gt_worldgen.mEndAsteroid;
            case 7 -> gt_worldgen -> gt_worldgen.twilightForest;
            default -> throw new IllegalStateException();
        };
    }
}
