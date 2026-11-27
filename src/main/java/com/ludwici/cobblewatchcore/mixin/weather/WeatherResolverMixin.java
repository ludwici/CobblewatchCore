package com.ludwici.cobblewatchcore.mixin.weather;

import com.ludwici.cobblewatchcore.weather.CellClimate;
import com.ludwici.cobblewatchcore.weather.ForestClimate;
import com.ourstory.osweather.climate.ClimateProfile;
import com.ourstory.osweather.scenario.AtmosphereTendency;
import com.ourstory.osweather.scenario.ScenarioCatalog;
import com.ourstory.osweather.scenario.ScenarioFamily;
import com.ourstory.osweather.simulation.AtmosphericState;
import com.ourstory.osweather.simulation.WeatherResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WeatherResolver.class, remap = false)
public abstract class WeatherResolverMixin {

    @Redirect(method = "resolve", at = @At(value = "INVOKE", target = "Lcom/ourstory/osweather/scenario/AtmosphereTendency;fogBias()F"))
    private static float cobblewatch$varyForestFog(AtmosphereTendency tendency, AtmosphericState state, ClimateProfile climate, long tick) {
        ForestClimate forest = CellClimate.forest(climate);
        float original = tendency.fogBias();
        if (forest.weight() <= 1.0F || ScenarioCatalog.get(state.scenarioId).family() != ScenarioFamily.FOG || state.scenarioId.contains("dissipation")) {
            return original;
        }

        long period = Math.floorDiv(tick, 24000L);
        double phase = Math.floorMod(tick, 24000L) / 24000.0;
        double blend = phase * phase * (3.0 - 2.0 * phase);
        long seed = forest.seed() ^ state.scenarioId.hashCode();
        float from = cobblewatch$density(seed ^ (period * 0x9E3779B97F4A7C15L));
        float to = cobblewatch$density(seed ^ ((period + 1L) * 0x9E3779B97F4A7C15L));
        float density = (float) (from + (to - from) * blend);
        density *= Math.max(0.30F, 1.0F - state.speed() * 1.2F);
        float coverage = Math.min(1.0F, (forest.weight() - 1.0F) / 0.4F);
        float atmosphericContribution = (state.humidity - 0.78F) * 2.2F + (state.speed() < 0.15F ? 0.18F : 0.0F);
        return original + (density - atmosphericContribution - original) * coverage;
    }

    @org.spongepowered.asm.mixin.Unique
    private static float cobblewatch$density(long seed) {
        double roll = AtmosphericState.noise(seed);
        if (roll < 0.45) {
            return 0.10F + (float) (roll / 0.45 * 0.15);
        }
        if (roll < 0.75) {
            return 0.25F + (float) ((roll - 0.45) / 0.30 * 0.20);
        }
        if (roll < 0.95) {
            return 0.45F + (float) ((roll - 0.75) / 0.20 * 0.25);
        }
        return 0.70F + (float) ((roll - 0.95) / 0.05 * 0.25);
    }
}
