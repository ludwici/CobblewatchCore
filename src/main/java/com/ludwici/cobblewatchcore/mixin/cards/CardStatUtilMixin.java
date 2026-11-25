package com.ludwici.cobblewatchcore.mixin.cards;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.howlite.cobblemoncards.util.CardStatUtil", remap = false)
public abstract class CardStatUtilMixin {

    @Inject(method = "getPlayerDropBonus", at = @At("HEAD"), cancellable = true)
    private static void cobblewatch$disableCardDropBonus(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.0f);
    }
}
