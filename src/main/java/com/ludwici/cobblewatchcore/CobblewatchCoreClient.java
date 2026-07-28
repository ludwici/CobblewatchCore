package com.ludwici.cobblewatchcore;

import com.ludwici.cobblewatchcore.network.AddItemHandlerS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CobblewatchCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(AddItemHandlerS2CPayload.ID, ((payload, context) -> {
            context.client().execute(() -> {
                LootJournalCompat.handleAddItemPacket(payload.itemStack());
            });
        }));
    }
}
