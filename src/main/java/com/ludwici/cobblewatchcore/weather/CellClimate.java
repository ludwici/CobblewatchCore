package com.ludwici.cobblewatchcore.weather;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.ourstory.osweather.climate.ClimateProfile;
import com.ourstory.osweather.simulation.CellKey;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CellClimate {

    private static final int SAMPLE_HEIGHT = 80;
    private static final int MAX_CACHED_CELLS = 16384;
    private static final Map<CellKey, ClimateProfile> CELLS = new LinkedHashMap<>(256, 0.75F, true);
    private static final Map<Biome, Float> DOWNFALL = new IdentityHashMap<>();
    private static volatile MinecraftServer server;

    private CellClimate() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(value -> {
            CELLS.clear();
            DOWNFALL.clear();
            server = value;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(value -> {
            server = null;
            CELLS.clear();
            DOWNFALL.clear();
        });
    }

    public static ClimateProfile sample(CellKey key, ClimateProfile fallback) {
        MinecraftServer current = server;
        if (current == null || !current.isSameThread()) {
            return fallback;
        }
        ServerLevel level = current.overworld();
        if (level == null) {
            return fallback;
        }
        ClimateProfile cached = CELLS.get(key);
        if (cached != null) {
            return cached;
        }
        Biome biome = level.getUncachedNoiseBiome(key.x() * 128 + 64, SAMPLE_HEIGHT / 4, key.z() * 128 + 64).value();
        float downfall = DOWNFALL.computeIfAbsent(biome, value -> readDownfall(level, value));
        ClimateProfile result = ClimateProfile.classify(biome.getBaseTemperature(), downfall, SAMPLE_HEIGHT);
        CELLS.put(key, result);
        if (CELLS.size() > MAX_CACHED_CELLS) {
            CELLS.remove(CELLS.keySet().iterator().next());
        }
        return result;
    }

    private static float readDownfall(ServerLevel level, Biome biome) {
        if (!biome.hasPrecipitation()) {
            return 0.04F;
        }
        return Biome.NETWORK_CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, level.registryAccess()), biome)
                .result()
                .filter(JsonElement::isJsonObject)
                .map(value -> value.getAsJsonObject().get("downfall"))
                .filter(value -> value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber())
                .map(JsonElement::getAsFloat)
                .orElse(0.55F);
    }
}
