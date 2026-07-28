package com.ludwici.cobblewatchcore.mixin.server;

import com.hisroyalty.cobbledgacha.item.custom.CapsuleItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ludwici.cobblewatchcore.network.AddItemHandlerS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapsuleItem.class)
public class CapsuleItemMixin {
    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean checkUse(Player player, ItemStack itemStack, Operation<Boolean> original) {
        ItemStack targetItem = itemStack.copy();
        boolean result = original.call(player, itemStack);
        if (result && !targetItem.isEmpty()) {
            AddItemHandlerS2CPayload payload = new AddItemHandlerS2CPayload(targetItem);
            ServerPlayNetworking.send((ServerPlayer) player, payload);
        }
        return result;
    }
}
