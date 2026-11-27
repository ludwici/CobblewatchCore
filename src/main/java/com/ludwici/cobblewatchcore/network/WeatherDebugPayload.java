package com.ludwici.cobblewatchcore.network;

import com.ludwici.cobblewatchcore.CobblewatchCore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record WeatherDebugPayload(List<Cell> cells) implements CustomPacketPayload {

    public static final Type<WeatherDebugPayload> TYPE = new Type<>(CobblewatchCore.id("weather_debug"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WeatherDebugPayload> CODEC = StreamCodec.of(WeatherDebugPayload::write, WeatherDebugPayload::read);

    public record Cell(int x, int z, String biome, String scenario, float forestWeight, float fog) {
    }

    private static void write(RegistryFriendlyByteBuf buf, WeatherDebugPayload payload) {
        buf.writeVarInt(payload.cells.size());
        for (Cell cell : payload.cells) {
            buf.writeInt(cell.x());
            buf.writeInt(cell.z());
            buf.writeUtf(cell.biome());
            buf.writeUtf(cell.scenario());
            buf.writeFloat(cell.forestWeight());
            buf.writeFloat(cell.fog());
        }
    }

    private static WeatherDebugPayload read(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > 25) {
            throw new IllegalArgumentException("Invalid weather debug cell count");
        }
        List<Cell> cells = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cells.add(new Cell(buf.readInt(), buf.readInt(), buf.readUtf(), buf.readUtf(), buf.readFloat(), buf.readFloat()));
        }
        return new WeatherDebugPayload(List.copyOf(cells));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
