package com.ludwici.cobblewatchcore.weather;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public record ForestClimate(float weight, long seed) {

    public static final ForestClimate OPEN = new ForestClimate(1.0F, 0L);

    private static final TagKey<Biome> FOREST = tag("minecraft:is_forest");
    private static final TagKey<Biome> COMMON_FOREST = tag("c:is_forest");
    private static final TagKey<Biome> TAIGA = tag("minecraft:is_taiga");
    private static final TagKey<Biome> JUNGLE = tag("minecraft:is_jungle");
    private static final TagKey<Biome> SWAMP = tag("c:is_swamp");
    private static final TagKey<Biome> DENSE = tag("c:is_dense_vegetation");

    public static float weight(Holder<Biome> biome) {
        String name = biome.unwrapKey().map(key -> key.location().getPath()).orElse("");
        if (name.contains("pale_garden")) {
            return 3.0F;
        }
        if (biome.is(SWAMP) || name.contains("swamp") || name.contains("marsh") || name.contains("bayou") || name.contains("bog") || name.contains("mangrove")) {
            return 2.5F;
        }
        if (name.contains("sparse") || name.contains("woodland") || name.contains("windswept")) {
            return isWooded(biome, name) ? 1.2F : 1.0F;
        }
        if (name.contains("dark_forest") || name.contains("blackwood") || name.contains("dense_forest") || name.contains("rainforest") || name.contains("redwood") || biome.is(JUNGLE) || name.contains("jungle")) {
            return 2.2F;
        }
        if (isWooded(biome, name) && (name.contains("old_growth") || name.contains("ancient") || name.contains("primeval"))) {
            return 1.8F;
        }
        if (biome.is(DENSE) && isWooded(biome, name)) {
            return 1.8F;
        }
        if (name.contains("birch")) {
            return 1.2F;
        }
        if (biome.is(TAIGA) || name.contains("taiga")) {
            return 1.5F;
        }
        return isWooded(biome, name) ? 1.4F : 1.0F;
    }

    private static boolean isWooded(Holder<Biome> biome, String name) {
        return biome.is(FOREST) || biome.is(COMMON_FOREST) || biome.is(TAIGA) || biome.is(JUNGLE)
                || name.contains("forest") || name.contains("woodland") || name.contains("woods") || name.contains("taiga")
                || name.contains("grove") || name.contains("jungle") || name.contains("thicket") || name.contains("orchard");
    }

    private static TagKey<Biome> tag(String name) {
        return TagKey.create(Registries.BIOME, ResourceLocation.parse(name));
    }
}
