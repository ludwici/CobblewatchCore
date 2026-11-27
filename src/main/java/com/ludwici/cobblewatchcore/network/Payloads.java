package com.ludwici.cobblewatchcore.network;

public class Payloads {
    public static void register() {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(WeatherDebugPayload.TYPE, WeatherDebugPayload.CODEC);
    }
}
