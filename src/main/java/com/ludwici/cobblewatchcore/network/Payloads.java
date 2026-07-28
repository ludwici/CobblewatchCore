package com.ludwici.cobblewatchcore.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class Payloads {
    public static void register() {
        PayloadTypeRegistry.playS2C().register(AddItemHandlerS2CPayload.ID, AddItemHandlerS2CPayload.CODEC);
    }
}
