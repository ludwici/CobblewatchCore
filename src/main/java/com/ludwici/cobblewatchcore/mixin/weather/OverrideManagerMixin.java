package com.ludwici.cobblewatchcore.mixin.weather;

import com.ludwici.cobblewatchcore.weather.WeatherDebug;
import com.ourstory.osweather.api.override.WeatherOverride;
import com.ourstory.osweather.api.weather.AtmosphereSnapshot;
import com.ourstory.osweather.internal.OverrideManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OverrideManager.class, remap = false)
public abstract class OverrideManagerMixin {

    @Inject(method = "map", at = @At("RETURN"), cancellable = true)
    private void cobblewatch$debugFog(WeatherOverride override, AtmosphereSnapshot base, long tick, CallbackInfoReturnable<AtmosphereSnapshot> cir) {
        if (!WeatherDebug.SOURCE.equals(override.source())) {
            return;
        }
        AtmosphereSnapshot s = cir.getReturnValue();
        float fog = override.intensity();
        cir.setReturnValue(new AtmosphereSnapshot(s.weatherId(), s.scenarioId(), s.temperatureC(), s.humidity(), s.pressureHpa(), s.windX(), s.windZ(), s.cloudCover(), s.precipitation(), s.thunder(), fog, s.sandstorm(), s.snow(), 1.0F - fog * 0.62F, s.gameTick()));
    }
}
