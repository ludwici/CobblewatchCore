package com.ludwici.cobblewatchcore.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static com.ludwici.cobblewatchcore.CobblewatchCore.MODID;

public record AddItemHandlerS2CPayload(ItemStack itemStack) implements CustomPacketPayload {
    public static final ResourceLocation PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(MODID, "add_item_handler");
    public static final CustomPacketPayload.Type<AddItemHandlerS2CPayload> ID = new Type<>(PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, AddItemHandlerS2CPayload> CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, AddItemHandlerS2CPayload::itemStack,
            AddItemHandlerS2CPayload::new
    );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
