package com.ludwici.cobblewatchcore.mixin.weather;

import com.ludwici.cobblewatchcore.weather.InitialWeather;
import com.ourstory.osweather.climate.ClimateProfile;
import com.ourstory.osweather.simulation.AtmosphericState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AtmosphericState.class, remap = false)
public abstract class AtmosphericStateMixin {

    @Inject(method = "initial", at = @At("RETURN"))
    private static void cobblewatch$initializeWeather(ClimateProfile climate, long cellSeed, long tick, CallbackInfoReturnable<AtmosphericState> cir) {
        InitialWeather.initialize(cir.getReturnValue(), climate, cellSeed);
    }
}
