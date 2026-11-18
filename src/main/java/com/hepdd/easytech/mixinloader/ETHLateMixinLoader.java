package com.hepdd.easytech.mixinloader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

@LateMixin
public class ETHLateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.easytech.late.json";
    }

    @Nonnull
    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        if (loadedMods.contains("gregtech")) {
            return Arrays.asList("DrillerBaseMixin", "OilDrillBaseMixin");
        }
        return Collections.emptyList();
    }
}
