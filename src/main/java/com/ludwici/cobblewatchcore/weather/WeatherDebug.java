package com.ludwici.cobblewatchcore.weather;

import com.ludwici.cobblewatchcore.network.WeatherDebugPayload;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.ourstory.osweather.OSWeather;
import com.ourstory.osweather.api.override.OverrideHandle;
import com.ourstory.osweather.api.override.WeatherOverride;
import com.ourstory.osweather.api.weather.AtmosphereSnapshot;
import com.ourstory.osweather.platform.MinecraftWeatherService;
import com.ourstory.osweather.simulation.AtmosphericCell;
import com.ourstory.osweather.simulation.CellKey;
import com.ourstory.osweather.simulation.WeatherEngine;
import com.ourstory.osweather.simulation.WeatherResolver;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class WeatherDebug {

    public static final String SOURCE = "cobblewatch-core:fog_debug";
    private static final Set<UUID> VIEWERS = new HashSet<>();
    private static final Map<UUID, OverrideHandle> OVERRIDES = new HashMap<>();

    private WeatherDebug() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> dispatcher.register(
                Commands.literal("cwweather").requires(source -> source.hasPermission(2))
                        .then(Commands.literal("debug")
                                .then(Commands.literal("on").executes(context -> toggle(context.getSource().getPlayerOrException(), true)))
                                .then(Commands.literal("off").executes(context -> toggle(context.getSource().getPlayerOrException(), false))))
                        .then(Commands.literal("fog")
                                .then(Commands.literal("clear").executes(context -> fog(context.getSource(), -1.0F)))
                                .then(Commands.argument("density", FloatArgumentType.floatArg(0.0F, 1.0F))
                                        .executes(context -> fog(context.getSource(), FloatArgumentType.getFloat(context, "density")))))));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 100 != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (VIEWERS.contains(player.getUUID())) {
                    if (!player.hasPermissions(2)) {
                        toggle(player, false);
                    } else {
                        send(player);
                    }
                }
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            VIEWERS.remove(handler.player.getUUID());
            release(handler.player.getUUID());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            VIEWERS.clear();
            OVERRIDES.values().forEach(OSWeather.api()::releaseOverride);
            OVERRIDES.clear();
        });
    }

    private static int toggle(ServerPlayer player, boolean enabled) {
        if (enabled && !ServerPlayNetworking.canSend(player, WeatherDebugPayload.TYPE)) {
            player.sendSystemMessage(Component.literal("Weather debug requires CobblewatchCore on the client."));
            return 0;
        }
        if (enabled) {
            VIEWERS.add(player.getUUID());
            send(player);
        } else {
            VIEWERS.remove(player.getUUID());
            if (ServerPlayNetworking.canSend(player, WeatherDebugPayload.TYPE)) {
                ServerPlayNetworking.send(player, new WeatherDebugPayload(List.of()));
            }
        }
        player.sendSystemMessage(Component.literal("Weather cell debug: " + (enabled ? "on" : "off")));
        return 1;
    }

    private static void send(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, WeatherDebugPayload.TYPE)) {
            return;
        }
        MinecraftWeatherService service = MinecraftWeatherService.get(player.getServer());
        if (player.level().dimension() != Level.OVERWORLD || service == null || service.controller() == null) {
            ServerPlayNetworking.send(player, new WeatherDebugPayload(List.of()));
            return;
        }
        WeatherEngine engine = service.controller().engine();
        CellKey current = CellKey.fromBlock(player.getX(), player.getZ());
        long tick = player.level().getGameTime();
        List<WeatherDebugPayload.Cell> cells = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                CellKey key = new CellKey(current.x() + dx, current.z() + dz);
                AtmosphericCell cell = engine.cells().get(key);
                if (cell == null) {
                    continue;
                }
                ForestClimate forest = CellClimate.forest(cell.climate);
                AtmosphereSnapshot snapshot = WeatherResolver.resolve(cell.state, cell.climate, tick);
                int x = key.x() * 512 + 256;
                int z = key.z() * 512 + 256;
                cells.add(new WeatherDebugPayload.Cell(x, z, forest.biome(), cell.state.scenarioId, forest.weight(), snapshot.fog()));
                if (dx == 0 && dz == 0) {
                    LoggerFactory.getLogger("CobblewatchWeather").info("Cell {},{} biome={} forest={} climate={} downfall={} scenario={} remaining={} fog={} rain={} wind={}", x, z, forest.biome(), forest.weight(), cell.climate.moisture(), cell.climate.downfall(), cell.state.scenarioId, cell.state.scenarioRemaining, snapshot.fog(), snapshot.precipitation(), snapshot.windSpeed());
                }
            }
        }
        ServerPlayNetworking.send(player, new WeatherDebugPayload(List.copyOf(cells)));
    }

    private static void release(UUID player) {
        OverrideHandle handle = OVERRIDES.remove(player);
        if (handle != null) {
            OSWeather.api().releaseOverride(handle);
        }
    }

    private static int fog(CommandSourceStack source, float density) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        release(player.getUUID());
        if (density >= 0.0F) {
            OVERRIDES.put(player.getUUID(), OSWeather.api().setOverride(player.getUUID(), new WeatherOverride("osweather:fog", density, player.level().getGameTime() + 1200L, 110, SOURCE, true, false)));
        }
        MinecraftWeatherService service = MinecraftWeatherService.get(source.getServer());
        if (service != null) {
            service.syncNow(player);
        }
        source.sendSuccess(() -> Component.literal(density < 0.0F ? "Fog debug cleared." : String.format(Locale.ROOT, "Fog debug: %.2f (60 seconds, visual only)", density)), false);
        return 1;
    }
}
