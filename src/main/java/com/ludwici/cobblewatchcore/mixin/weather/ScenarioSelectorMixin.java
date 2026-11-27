package com.ludwici.cobblewatchcore.mixin.weather;

import com.ludwici.cobblewatchcore.weather.WeatherBalance;
import com.ourstory.osweather.scenario.WeatherScenario;
import com.ourstory.osweather.simulation.ScenarioSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ScenarioSelector.class, remap = false)
public abstract class ScenarioSelectorMixin {

    @Inject(method = "duration", at = @At("RETURN"), cancellable = true)
    private static void cobblewatch$balanceDuration(WeatherScenario scenario, long seed, CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue(WeatherBalance.duration(scenario, cir.getReturnValue()));
    }
}
