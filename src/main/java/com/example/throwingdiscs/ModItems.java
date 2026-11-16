package com.example.throwingdiscs;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/**
 * Registriert die fuenf Throwing-Disc-Items.
 */
public class ModItems {
	public static final Item COPPER_THROWING_DISC = register(DiscTier.COPPER);
	public static final Item IRON_THROWING_DISC = register(DiscTier.IRON);
	public static final Item GOLDEN_THROWING_DISC = register(DiscTier.GOLDEN);
	public static final Item DIAMOND_THROWING_DISC = register(DiscTier.DIAMOND);
	public static final Item NETHERITE_THROWING_DISC = register(DiscTier.NETHERITE);

	private static Item register(DiscTier tier) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ThrowingDiscs.id(tier.itemName()));

		Item.Properties properties = new Item.Properties()
				.durability(tier.durability())   // Haltbarkeit (macht den Stack automatisch zu 1)
				.repairable(tier.repairItem())   // im Amboss mit Barren reparierbar
				.setId(key);

		return Registry.register(BuiltInRegistries.ITEM, key, new ThrowingDiscItem(properties, tier));
	}

	/** Wird in ThrowingDiscs#onInitialize aufgerufen, damit die Items oben registriert werden. */
	public static void initialize() {
		// Alle Scheiben erscheinen im Kreativ-Tab "Kampf".
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
			output.accept(COPPER_THROWING_DISC);
			output.accept(IRON_THROWING_DISC);
			output.accept(GOLDEN_THROWING_DISC);
			output.accept(DIAMOND_THROWING_DISC);
			output.accept(NETHERITE_THROWING_DISC);
		});
	}
}
