package com.ludwici.cobblewatchcore.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.howlite.cobblemoncards.screen.BinderScreen", remap = false)
public abstract class BinderScreenMixin {

    @Inject(method = "renderStatsPanel", at = @At("HEAD"), cancellable = true)
    private void cobblewatch$hideBonusPanel(CallbackInfo ci) {
        ci.cancel();
    }
}
