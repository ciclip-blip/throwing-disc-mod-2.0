package com.example.throwingdiscs;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/**
 * Client-Einstiegspunkt: sagt Minecraft, wie die fliegende Scheibe gezeichnet wird.
 * Wir nehmen den Standard-Renderer fuer geworfene Items (wie bei Schneebaellen) - er zeigt das Item-Sprite.
 */
public class ThrowingDiscsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntityTypes.THROWN_DISC, ThrownItemRenderer::new);
	}
}
