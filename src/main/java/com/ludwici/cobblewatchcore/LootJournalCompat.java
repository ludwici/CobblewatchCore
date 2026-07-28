package com.ludwici.cobblewatchcore;

import dev.obscuria.lootjournal.LootJournalHelper;
import dev.obscuria.lootjournal.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LootJournalCompat {
    public static void handleSetSlotPacket(Level level, ClientboundContainerSetSlotPacket packet) {
        if (!Config.ENABLE_LOOT_JOURNAL.get()) {
            return;
        }
        if (packet.getContainerId() != -2) {
            return;
        }
        Minecraft instance = Minecraft.getInstance();
        @Nullable var owner = instance.player;
        if (owner == null) {
            return;
        }
        LootJournalHelper.pickupItem(owner, packet.getItem());
    }

    public static void handleAddItemPacket(ItemStack stack) {
        if (!Config.ENABLE_LOOT_JOURNAL.get()) {
            return;
        }
        Minecraft instance = Minecraft.getInstance();
        @Nullable var owner = instance.player;
        if (owner == null) {
            return;
        }
        LootJournalHelper.pickupItem(owner, stack);
    }
}
