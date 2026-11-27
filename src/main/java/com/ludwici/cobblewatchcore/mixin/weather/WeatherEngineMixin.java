package com.ludwici.cobblewatchcore.mixin.weather;

import com.ludwici.cobblewatchcore.weather.CellClimate;
import com.ourstory.osweather.climate.ClimateProfile;
import com.ourstory.osweather.scenario.ScenarioCatalog;
import com.ourstory.osweather.scenario.ScenarioFamily;
import com.ourstory.osweather.simulation.AtmosphericCell;
import com.ourstory.osweather.simulation.CellKey;
import com.ourstory.osweather.simulation.ClimateAnchor;
import com.ourstory.osweather.simulation.WeatherEngine;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WeatherEngine.class, remap = false)
public abstract class WeatherEngineMixin {

    @Redirect(method = "lambda$update$0", at = @At(value = "INVOKE", target = "Lcom/ourstory/osweather/simulation/ClimateAnchor;climate()Lcom/ourstory/osweather/climate/ClimateProfile;"), require = 2)
    private ClimateProfile cobblewatch$sampleNewCell(ClimateAnchor receiver, ClimateAnchor anchor, long tick, CellKey key) {
        return CellClimate.sample(key, receiver.climate());
    }

    @Redirect(method = "update", at = @At(value = "FIELD", target = "Lcom/ourstory/osweather/simulation/AtmosphericCell;climate:Lcom/ourstory/osweather/climate/ClimateProfile;", opcode = Opcodes.PUTFIELD))
    private void cobblewatch$keepCellClimate(AtmosphericCell cell, ClimateProfile fallback) {
        cell.climate = CellClimate.sample(cell.key, fallback);
    }

    @ModifyVariable(method = "snap", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private ClimateProfile cobblewatch$sampleQueryCell(ClimateProfile fallback, CellKey key, long tick, ClimateProfile original) {
        return CellClimate.sample(key, fallback);
    }

    @Inject(method = "stepCell", at = @At("HEAD"))
    private void cobblewatch$updateExistingCell(AtmosphericCell cell, long tick, int delta, CallbackInfo ci) {
        cell.climate = CellClimate.sample(cell.key, cell.climate);
        if (ScenarioCatalog.get(cell.state.scenarioId).family() == ScenarioFamily.STABLE) {
            cell.state.scenarioRemaining = Math.min(cell.state.scenarioRemaining, 16200L);
        }
    }
}
