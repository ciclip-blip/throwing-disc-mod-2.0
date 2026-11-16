package com.example.throwingdiscs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Das Item. Rechtsklick wirft die Scheibe - aus der Haupthand ODER aus der Nebenhand
 * (in der Java Edition ruft Minecraft use() fuer die Nebenhand automatisch auf, wenn die Haupthand nichts tut).
 */
public class ThrowingDiscItem extends Item {
	private final DiscTier tier;

	public ThrowingDiscItem(Properties properties, DiscTier tier) {
		super(properties);
		this.tier = tier;
	}

	public DiscTier getTier() {
		return tier;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS,
				0.7F, 1.3F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

		// Die Entity wird nur auf dem Server erzeugt.
		if (level instanceof ServerLevel serverLevel) {
			ThrownDiscEntity disc = Projectile.spawnProjectileFromRotation(
					ThrownDiscEntity::new,
					serverLevel,
					stack,
					player,
					/* yOffset: */ 0.0F,
					/* pow: */ ThrownDiscEntity.FLY_SPEED,
					/* uncertainty: */ 0.0F);

			// Merken, aus welcher Hand geworfen wurde, damit die Scheibe dorthin zurueckkehrt.
			disc.setThrownFromOffHand(hand == InteractionHand.OFF_HAND);
		}

		player.awardStat(Stats.ITEM_USED.get(this));

		// Die Scheibe verlaesst das Inventar und kommt spaeter zurueck (im Kreativmodus wird nichts verbraucht).
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
