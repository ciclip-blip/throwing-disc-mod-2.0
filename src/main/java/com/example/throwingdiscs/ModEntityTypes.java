package com.example.throwingdiscs;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Registriert die fliegende Scheibe (die Entity). Alle fuenf Stufen benutzen dieselbe Entity -
 * die Stufe wird aus dem Item abgelesen, das in der Entity steckt.
 */
public class ModEntityTypes {
	public static final ResourceKey<EntityType<?>> THROWING_DISC_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, ThrowingDiscs.id("throwing_disc"));

	public static final EntityType<ThrownDiscEntity> THROWN_DISC = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			THROWING_DISC_KEY,
			EntityType.Builder.<ThrownDiscEntity>of(ThrownDiscEntity::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)        // Hitbox (Breite, Hoehe)
					.clientTrackingRange(8)   // bis zu welcher Entfernung (Chunks) Clients sie sehen
					.updateInterval(1)        // jeden Tick Position senden (die Scheibe aendert staendig die Richtung)
					.build(THROWING_DISC_KEY)
	);

	public static void initialize() {
	}
}
