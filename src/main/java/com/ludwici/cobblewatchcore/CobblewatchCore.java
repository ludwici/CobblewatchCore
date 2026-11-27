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
		org.slf4j.LoggerFactory.getLogger("CobblewatchWeather").info("Core weather debug revision 2 (Diorama diagnostics)");
		if (FabricLoader.getInstance().getModContainer("osweather").map(mod -> mod.getMetadata().getVersion().getFriendlyString().equals("1.1")).orElse(false)) {
			CellClimate.register();
			com.ludwici.cobblewatchcore.weather.WeatherDebug.register();
		}
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}
}
