package com.ludwici.cobblewatchcore.weather;

import com.ludwici.cobblewatchcore.network.WeatherDebugPayload;
import dev.diorama.waypoint.Waypoint;
import dev.diorama.waypoint.WaypointStore;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class DioramaWeatherDebug {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("CobblewatchWeather");
    private static final Map<Long, Waypoint> MARKERS = new HashMap<>();
    private static WaypointStore store;
    private static List<WeatherDebugPayload.Cell> pending = List.of();
    private static boolean dirty;

    private DioramaWeatherDebug() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(WeatherDebugPayload.TYPE, (payload, context) -> context.client().execute(() -> {
            pending = payload.cells();
            LOGGER.info("Received {} weather debug cells", pending.size());
            dirty = true;
        }));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                return;
            }
            WaypointStore current = WaypointStore.get();
            if (current == null) {
                return;
            }
            if (store != current) {
                MARKERS.clear();
                store = current;
                dirty = true;
            }
            if (dirty) {
                dirty = false;
                apply(current);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            pending = List.of();
            MARKERS.clear();
            store = null;
            dirty = false;
        });
    }

    private static void apply(WaypointStore current) {
        Set<Long> desired = new HashSet<>();
        List<Waypoint> added = new ArrayList<>();
        Set<Waypoint> existing = new HashSet<>(current.all());
        int removed = 0;
        for (WeatherDebugPayload.Cell cell : pending) {
            long key = ((long) cell.x() << 32) | (cell.z() & 0xFFFFFFFFL);
            desired.add(key);
            String name = String.format(Locale.ROOT, "WX %s x%.2f fog=%.2f %s", cell.biome(), cell.forestWeight(), cell.fog(), cell.scenario().replace("osweather:", ""));
            int color = cell.fog() >= 0.45F ? 0xFFB0B4BA : cell.fog() >= 0.10F ? 0xFF7CE2FE : cell.forestWeight() > 1.1F ? 0xFF46A758 : 0xFFFFC53D;
            Waypoint marker = MARKERS.get(key);
            if (marker == null || !existing.contains(marker)) {
                marker = new Waypoint(name, cell.x(), 80, cell.z(), "minecraft:overworld", color);
                marker.kind = Waypoint.Kind.TEMPORARY;
                marker.initials = "WX";
                MARKERS.put(key, marker);
                added.add(marker);
            } else {
                marker.name = name;
                marker.color = color;
            }
        }
        for (long key : new ArrayList<>(MARKERS.keySet())) {
            if (!desired.contains(key)) {
                Waypoint old = MARKERS.remove(key);
                if (existing.contains(old)) {
                    current.remove(old);
                    removed++;
                }
            }
        }
        current.addAll(added);
        LOGGER.info("Weather waypoints: {} tracked, {} added, {} removed, {} present in Diorama store", MARKERS.size(), added.size(), removed, current.all().stream().filter(MARKERS.values()::contains).count());
    }
}
