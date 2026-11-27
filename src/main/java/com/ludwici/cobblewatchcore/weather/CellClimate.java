package com.ludwici.cobblewatchcore.weather;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.ourstory.osweather.climate.ClimateProfile;
import com.ourstory.osweather.simulation.CellKey;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.Holder;
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
    private static final Map<ClimateProfile, ForestClimate> FORESTS = new IdentityHashMap<>();
    private static final Map<Biome, Float> DOWNFALL = new IdentityHashMap<>();
    private static volatile MinecraftServer server;

    private CellClimate() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(value -> {
            CELLS.clear();
            FORESTS.clear();
            DOWNFALL.clear();
            server = value;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(value -> {
            server = null;
            CELLS.clear();
            FORESTS.clear();
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
        int centerX = key.x() * 128 + 64;
        int centerZ = key.z() * 128 + 64;
        Holder<Biome> center = level.getUncachedNoiseBiome(centerX, SAMPLE_HEIGHT / 4, centerZ);
        Biome biome = center.value();
        float downfall = DOWNFALL.computeIfAbsent(biome, value -> readDownfall(level, value));
        ClimateProfile result = ClimateProfile.classify(biome.getBaseTemperature(), downfall, SAMPLE_HEIGHT);
        float forestWeight = ForestClimate.weight(center) * 2.0F;
        for (int dx : new int[]{-32, 32}) {
            for (int dz : new int[]{-32, 32}) {
                forestWeight += ForestClimate.weight(level.getUncachedNoiseBiome(centerX + dx, SAMPLE_HEIGHT / 4, centerZ + dz));
            }
        }
        long seed = level.getSeed() ^ ((long) key.x() * 0x9E3779B97F4A7C15L) ^ ((long) key.z() * 0xD1B54A32D192ED03L);
        FORESTS.put(result, new ForestClimate(forestWeight / 6.0F, seed));
        CELLS.put(key, result);
        if (CELLS.size() > MAX_CACHED_CELLS) {
            FORESTS.remove(CELLS.remove(CELLS.keySet().iterator().next()));
        }
        return result;
    }

    public static ForestClimate forest(ClimateProfile climate) {
        MinecraftServer current = server;
        if (current == null || !current.isSameThread()) {
            return ForestClimate.OPEN;
        }
        return FORESTS.getOrDefault(climate, ForestClimate.OPEN);
    }

    public static float fogTimeWeight() {
        MinecraftServer current = server;
        if (current == null || !current.isSameThread() || current.overworld() == null) {
            return 1.0F;
        }
        long time = Math.floorMod(current.overworld().getDayTime(), 24000L);
        return time >= 13000L || time < 2000L ? 1.15F : 1.0F;
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
