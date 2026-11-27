package com.ludwici.cobblewatchcore.mixin.weather;

import com.ludwici.cobblewatchcore.weather.WeatherBalance;
import com.ourstory.osweather.climate.ClimateProfile;
import com.ourstory.osweather.scenario.AtmosphereTendency;
import com.ourstory.osweather.scenario.WeatherScenario;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WeatherScenario.class, remap = false)
public abstract class WeatherScenarioMixin {

    @Unique
    private volatile AtmosphereTendency cobblewatch$balancedTendency;

    @Inject(method = "climateWeight", at = @At("RETURN"), cancellable = true)
    private void cobblewatch$balanceWeight(ClimateProfile climate, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(WeatherBalance.weight((WeatherScenario) (Object) this, climate, cir.getReturnValue()));
    }

    @Inject(method = "tendency", at = @At("RETURN"), cancellable = true)
    private void cobblewatch$balanceTendency(CallbackInfoReturnable<AtmosphereTendency> cir) {
        if (cobblewatch$balancedTendency == null) {
            cobblewatch$balancedTendency = WeatherBalance.tendency((WeatherScenario) (Object) this, cir.getReturnValue());
        }
        cir.setReturnValue(cobblewatch$balancedTendency);
    }
}
