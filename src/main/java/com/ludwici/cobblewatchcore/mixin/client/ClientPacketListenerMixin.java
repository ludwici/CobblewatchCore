package com.ludwici.cobblewatchcore.mixin.client;

import com.ludwici.cobblewatchcore.LootJournalCompat;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleContainerSetSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER))
    private void onPlayerTakeItem(ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket, CallbackInfo ci) {
        LootJournalCompat.handleSetSlotPacket(clientboundContainerSetSlotPacket);
    }

}
