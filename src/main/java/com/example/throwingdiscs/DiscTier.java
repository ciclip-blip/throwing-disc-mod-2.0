package com.example.throwingdiscs;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Die fuenf Stufen der Throwing Disc. Hier kannst du alles ausbalancieren.
 *
 * Mojang hat bisher nur bekannt gegeben, dass es Kupfer, Eisen, Gold, Diamant und Netherit gibt -
 * die konkreten Werte unten sind also ERFUNDEN (angelehnt an die Schwerter).
 */
public enum DiscTier {
	//     Item-ID                   Schaden  Haltbarkeit  Reparatur-Material
	COPPER("copper_throwing_disc",     4.0F,   190,        Items.COPPER_INGOT),
	IRON("iron_throwing_disc",         5.0F,   250,        Items.IRON_INGOT),
	GOLDEN("golden_throwing_disc",     4.0F,    32,        Items.GOLD_INGOT),
	DIAMOND("diamond_throwing_disc",   6.0F,  1561,        Items.DIAMOND),
	NETHERITE("netherite_throwing_disc", 7.0F, 2031,       Items.NETHERITE_INGOT);

	private final String itemName;
	private final float damage;
	private final int durability;
	private final Item repairItem;

	DiscTier(String itemName, float damage, int durability, Item repairItem) {
		this.itemName = itemName;
		this.damage = damage;
		this.durability = durability;
		this.repairItem = repairItem;
	}

	public String itemName() {
		return itemName;
	}

	/** Schaden pro Treffer (2.0 = 1 Herz). */
	public float damage() {
		return damage;
	}

	/** Wie oft die Scheibe zurueckkehren kann, bevor sie kaputtgeht. */
	public int durability() {
		return durability;
	}

	/** Damit kann man die Scheibe im Amboss reparieren. */
	public Item repairItem() {
		return repairItem;
	}
}
