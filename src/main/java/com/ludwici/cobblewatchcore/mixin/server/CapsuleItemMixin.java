package com.ludwici.cobblewatchcore.mixin.server;

import com.hisroyalty.cobbledgacha.item.custom.CapsuleItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapsuleItem.class)
public class CapsuleItemMixin {

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean handleItem(Player player, ItemStack itemStack, Operation<Boolean> original) {
        player.getInventory().placeItemBackInInventory(itemStack);
        return true;
    }

}
