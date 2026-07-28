package com.ludwici.cobblewatchcore;

import com.ludwici.cobblewatchcore.network.Payloads;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;

public class CobblewatchCore implements ModInitializer {
	public static final String MODID = "cobblewatch-core";

	@Override
	public void onInitialize() {
		Payloads.register();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}
}
