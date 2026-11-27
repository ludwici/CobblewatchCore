package com.ludwici.cobblewatchcore.mixin.rustlingspots;

import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.spot.RustlingSpot;
import net.levelscraft7.rustlingspots.spot.SpotRewardResolver;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpotRewardResolver.class, remap = false)
public abstract class SpotRewardResolverMixin {

    @Inject(method = "announceShinySpotFind", at = @At("HEAD"), cancellable = true)
    private static void cobblewatch$localizeShinyAnnouncement(ServerLevel level, ServerPlayer player, RustlingSpot spot, String pokemonName, CallbackInfo ci) {
        ci.cancel();
        if (!spot.isShiny() || !RustlingSpotsServerConfig.GENERAL.announceShinyFindsGlobally()) {
            return;
        }

        Component family;
        if (spot.isCustom()) {
            family = Component.literal(spot.displayName());
        } else {
            family = Component.translatableWithFallback("message.rustlingspots.origin." + spot.getFamily().serializedName(), spot.displayName());
        }
        Component message = pokemonName == null
                ? Component.translatable("message.cobblewatch-core.rustlingspots.shiny_find", player.getDisplayName(), family)
                : Component.translatable("message.cobblewatch-core.rustlingspots.shiny_encounter", player.getDisplayName(), family, Component.literal(pokemonName));
        level.getServer().getPlayerList().broadcastSystemMessage(message, false);
    }
}
