package com.ludwici.cobblewatchcore.weather;

import com.ourstory.osweather.climate.ClimateProfile;
import com.ourstory.osweather.scenario.AtmosphereTendency;
import com.ourstory.osweather.scenario.WeatherScenario;
import com.ourstory.osweather.simulation.AtmosphericState;
import com.ourstory.osweather.simulation.ScenarioSelector;

public final class InitialWeather {

    private InitialWeather() {
    }

    public static void initialize(AtmosphericState state, ClimateProfile climate, long cellSeed) {
        WeatherScenario scenario = ScenarioSelector.choose(climate, state, cellSeed ^ 0x9E3779B97F4A7C15L);
        AtmosphereTendency tendency = scenario.tendency();
        long duration = ScenarioSelector.duration(scenario, cellSeed ^ 0xD1B54A32D192ED03L);
        double phase = AtmosphericState.noise(cellSeed ^ 0x94D049BB133111EBL);
        double angle = AtmosphericState.noise(cellSeed ^ 0xBF58476D1CE4E5B9L) * Math.PI * 2.0;

        state.scenarioId = scenario.id();
        state.scenarioRemaining = Math.max(400L, (long) (duration * (0.25 + phase * 0.75)));
        state.temperatureC = climate.baselineTemperatureC() + tendency.temperatureDelta();
        state.humidity = tendency.humidityTarget();
        state.pressureHpa = tendency.pressureTarget();
        state.windX = (float) (Math.cos(angle) * tendency.windTarget());
        state.windZ = (float) (Math.sin(angle) * tendency.windTarget());
        state.cloudMoisture = tendency.cloudMoistureTarget();
        state.instability = tendency.instabilityTarget();
    }
}
