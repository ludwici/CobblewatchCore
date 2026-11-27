package com.ludwici.cobblewatchcore;

import com.ludwici.cobblewatchcore.network.Payloads;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import com.ludwici.cobblewatchcore.weather.CellClimate;

import net.minecraft.resources.ResourceLocation;

public class CobblewatchCore implements ModInitializer {
	public static final String MODID = "cobblewatch-core";

	@Override
	public void onInitialize() {
		Payloads.register();
		if (FabricLoader.getInstance().getModContainer("osweather").map(mod -> mod.getMetadata().getVersion().getFriendlyString().equals("1.1")).orElse(false)) {
			CellClimate.register();
		}
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}
}
