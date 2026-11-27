package com.ludwici.cobblewatchcore.weather;

import com.ourstory.osweather.climate.ClimateProfile;
import com.ourstory.osweather.scenario.AtmosphereTendency;
import com.ourstory.osweather.scenario.ScenarioFamily;
import com.ourstory.osweather.scenario.WeatherScenario;

public final class WeatherBalance {

    private WeatherBalance() {
    }

    public static float weight(WeatherScenario scenario, ClimateProfile climate, float original) {
        String id = scenario.id();
        if (climate.arid() && scenario.family() == ScenarioFamily.COLD) {
            return 0.0F;
        }
        if (original <= 0.0F && id.contains("blizzard") && climate.biomeTemperature() <= 0.15F && !climate.arid()) {
            original = scenario.coldWeight();
        }
        if (original <= 0.0F) {
            return 0.0F;
        }
        return original * switch (scenario.family()) {
            case STABLE -> climate.arid() ? 0.95F : 1.05F;
            case HUMID -> climate.arid() ? 0.25F : scenario.tendency().precipitationBias() >= 0.18F ? (climate.humid() ? 0.85F : 0.55F) : 0.95F;
            case STORM -> climate.arid() ? 0.2F : scenario.extreme() ? 0.9F : 0.65F;
            case COLD -> climate.cold() ? (id.contains("blizzard") ? 3.0F : 2.0F) : 0.7F;
            case DRY -> id.contains("sandstorm") ? 12.0F : id.contains("dusty") ? 4.5F : 0.85F;
            case FOG -> id.contains("dissipation") ? 0.8F : climate.humid() ? 2.2F : climate.arid() ? 0.4F : 1.5F;
            case TRANSITION -> 0.8F;
        };
    }

    public static long duration(WeatherScenario scenario, long original) {
        double factor = switch (scenario.family()) {
            case STABLE -> 0.85;
            case HUMID -> scenario.tendency().precipitationBias() >= 0.18F ? 0.50 : 0.85;
            case STORM -> 0.65;
            case DRY -> scenario.tendency().sandBias() > 0.0F ? 0.9 : 0.55;
            case FOG -> 0.9;
            default -> scenario.extreme() ? 1.0 : 0.8;
        };
        return Math.max(1200L, Math.round(original * factor));
    }

    public static AtmosphereTendency tendency(WeatherScenario scenario, AtmosphereTendency original) {
        ScenarioFamily family = scenario.family();
        float humidity = original.humidityTarget();
        float clouds = original.cloudMoistureTarget();
        float rain = original.precipitationBias();
        float fog = original.fogBias();
        float sand = original.sandBias();
        float pressure = original.pressureTarget();
        if ((family == ScenarioFamily.COLD) && rain >= 0.18F) {
            humidity = clamp(humidity + 0.06F);
            clouds = clamp(clouds + 0.08F);
            rain = clamp(rain + 0.10F);
            pressure -= 1.5F;
        }
        if ((family == ScenarioFamily.HUMID || family == ScenarioFamily.STORM) && rain >= 0.18F) {
            rain = clamp(rain * 0.85F);
        }
        if (family == ScenarioFamily.FOG && fog >= 0.40F) {
            fog = clamp(fog + 0.10F);
        }
        if (family == ScenarioFamily.DRY && sand > 0.0F) {
            sand = clamp(sand + 0.16F);
        }
        if (scenario.id().endsWith(":cloudy_stable") || scenario.id().endsWith(":cloud_build_up")) {
            clouds = clamp(clouds + 0.12F);
        }
        if (humidity == original.humidityTarget() && clouds == original.cloudMoistureTarget() && rain == original.precipitationBias() && fog == original.fogBias() && sand == original.sandBias()) {
            return original;
        }
        return new AtmosphereTendency(humidity, pressure, original.windTarget(), clouds, original.instabilityTarget(), original.temperatureDelta(), rain, fog, sand);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
