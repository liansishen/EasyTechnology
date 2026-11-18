package com.hepdd.easytech.mixinloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ETHLateMixinLoaderTest {

    @Test
    public void testLateMixinLoaderConfiguration() {
        ETHLateMixinLoader loader = new ETHLateMixinLoader();
        assertEquals("mixins.easytech.late.json", loader.getMixinConfig());

        assertTrue(
            loader.getMixins(Collections.emptySet())
                .isEmpty());
        assertTrue(
            loader.getMixins(new HashSet<>(Collections.singletonList("TConstruct")))
                .isEmpty());

        List<String> gtMixins = loader.getMixins(new HashSet<>(Arrays.asList("gregtech", "TConstruct")));
        assertEquals(Arrays.asList("DrillerBaseMixin", "OilDrillBaseMixin"), gtMixins);
    }

    @Test
    public void testBytecodeIsolationAndDeadClassRemoval() throws IOException {
        ClassLoader cl = ETHLateMixinLoaderTest.class.getClassLoader();

        // Obsolete ETHOilDrillMiner must be removed
        assertNull(cl.getResource("com/hepdd/easytech/common/tileentities/machines/multi/ETHOilDrillMiner.class"));

        // ETHStatics must not reference bartworks or galacticgreg in its constant pool
        String staticsPool = readClassAscii("com/hepdd/easytech/loaders/preload/ETHStatics.class");
        assertFalse(staticsPool.contains("bartworks"));
        assertFalse(staticsPool.contains("galacticgreg"));

        // ETHPortableCraftingStation must not reference client-only Keyboard or ETHLoaderKeybind
        String stationPool = readClassAscii(
            "com/hepdd/easytech/common/tileentities/machines/basic/ETHPortableCraftingStation.class");
        assertFalse(stationPool.contains("org/lwjgl/input/Keyboard"));
        assertFalse(stationPool.contains("org.lwjgl.input.Keyboard"));
        assertFalse(stationPool.contains("ETHLoaderKeybind"));

        // ETHLateMixinLoader must reside outside declared Sponge Mixin package to avoid IllegalClassLoadError
        assertOutsideMixinPackage(cl, "mixins.easytech.json");
        assertOutsideMixinPackage(cl, "mixins.easytech.late.json");
    }

    private static void assertOutsideMixinPackage(ClassLoader cl, String configName) throws IOException {
        try (InputStream in = cl.getResourceAsStream(configName)) {
            assertNotNull("Missing mixin config: " + configName, in);
            String jsonText = readStreamUtf8(in);
            JsonObject obj = new JsonParser().parse(jsonText)
                .getAsJsonObject();
            String mixinPackage = obj.get("package")
                .getAsString();
            String loaderPackage = ETHLateMixinLoader.class.getPackage()
                .getName();
            assertFalse(
                "ETHLateMixinLoader must not be inside mixin package " + mixinPackage,
                loaderPackage.equals(mixinPackage) || loaderPackage.startsWith(mixinPackage + "."));
        }
    }

    private static String readStreamUtf8(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String readClassAscii(String resourcePath) throws IOException {
        try (InputStream in = ETHLateMixinLoaderTest.class.getClassLoader()
            .getResourceAsStream(resourcePath)) {
            assertNotNull("Missing compiled class: " + resourcePath, in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.ISO_8859_1);
        }
    }
}
