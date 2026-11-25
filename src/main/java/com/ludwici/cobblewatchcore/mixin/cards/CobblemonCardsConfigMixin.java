package com.ludwici.cobblewatchcore.mixin.cards;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.howlite.cobblemoncards.CobblemonCardsConfig", remap = false)
public abstract class CobblemonCardsConfigMixin {

    @Inject(method = "getStatMultiplier", at = @At("HEAD"), cancellable = true)
    private static void cobblewatch$disableCardStats(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.0f);
    }
}
