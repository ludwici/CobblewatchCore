package com.ludwici.cobblewatchcore;

import net.fabricmc.api.ClientModInitializer;

public class CobblewatchCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("diorama").map(mod -> mod.getMetadata().getVersion().getFriendlyString().startsWith("0.11.0")).orElse(false)) {
            com.ludwici.cobblewatchcore.weather.DioramaWeatherDebug.register();
        }

    }
}
