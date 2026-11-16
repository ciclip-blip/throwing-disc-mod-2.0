package com.example.throwingdiscs;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Haupteinstiegspunkt der Mod (laeuft auf Client UND Server).
 */
public class ThrowingDiscs implements ModInitializer {
	public static final String MOD_ID = "throwing_discs";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Reihenfolge: erst die Entity, dann die Items.
		ModEntityTypes.initialize();
		ModItems.initialize();

		LOGGER.info("Throwing Discs geladen.");
	}

	/** Kleine Hilfsmethode, um Identifier dieser Mod zu bauen: id("irgendwas") -> throwing_discs:irgendwas */
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
