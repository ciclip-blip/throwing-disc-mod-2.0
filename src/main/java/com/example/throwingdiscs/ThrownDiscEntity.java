package com.example.throwingdiscs;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Die fliegende Throwing Disc.
 *
 * Verhalten (so wie Mojang die Disc angekuendigt hat):
 *  - fliegt gerade (keine Schwerkraft) und geht DURCH Gegner hindurch, statt zu verschwinden
 *  - prallt von Bloecken ab (Abpraller verlaengern die Flugzeit)
 *  - kehrt danach zum Werfer zurueck und landet wieder in dessen Hand/Inventar
 *
 * Die Zahlen unten kannst du frei veraendern.
 */
public class ThrownDiscEntity extends ThrowableItemProjectile {
	/** Fluggeschwindigkeit nach draussen (Bloecke pro Tick, 20 Ticks = 1 Sekunde). */
	public static final float FLY_SPEED = 1.5F;
	/** Geschwindigkeit auf dem Rueckweg. */
	private static final double RETURN_SPEED = 1.9D;
	/** So viele Ticks fliegt die Scheibe geradeaus (ca. 15 Bloecke), bevor sie umkehrt. */
	private static final int BASE_OUTBOUND_TICKS = 10;
	/** Jeder Abpraller verlaengert die Flugzeit um so viele Ticks. */
	private static final int TICKS_PER_BOUNCE = 5;
	/** Nach so vielen Abprallern kehrt die Scheibe sofort um. */
	private static final int MAX_BOUNCES = 4;
	/** Sicherheitsnetz: Nach so vielen Ticks ist die Scheibe auf jeden Fall wieder weg. */
	private static final int MAX_LIFETIME_TICKS = 120;
	/** Naeher als so viele Bloecke am Werfer = gefangen. */
	private static final double CATCH_DISTANCE = 1.5D;

	private boolean returning;
	private boolean thrownFromOffHand;
	private int flightTicks;
	private int bounces;
	/** IDs der Gegner, die in diesem Flugabschnitt schon getroffen wurden (damit jeder nur einmal pro Durchgang Schaden nimmt). */
	private final Set<Integer> alreadyHit = new HashSet<>();

	// Wird von der Entity-Registrierung benutzt (z. B. beim Laden/Synchronisieren).
	public ThrownDiscEntity(EntityType<? extends ThrownDiscEntity> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
	}

	// Wird beim Werfen benutzt.
	public ThrownDiscEntity(Level level, LivingEntity owner, ItemStack itemStack) {
		super(ModEntityTypes.THROWN_DISC, owner, level, itemStack);
		this.setNoGravity(true);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.COPPER_THROWING_DISC;
	}

	public void setThrownFromOffHand(boolean offHand) {
		this.thrownFromOffHand = offHand;
	}

	/** Die Stufe (Kupfer ... Netherit) wird aus dem Item in der Entity abgelesen. */
	private DiscTier tier() {
		return getItem().getItem() instanceof ThrowingDiscItem disc ? disc.getTier() : DiscTier.COPPER;
	}

	// ------------------------------------------------------------------------------------------
	// Flug
	// ------------------------------------------------------------------------------------------

	@Override
	public void tick() {
		// Flugsteuerung (Umkehren, Zurueckfliegen, Fangen) passiert nur auf dem Server.
		if (level() instanceof ServerLevel serverLevel) {
			if (!steer(serverLevel)) {
				return; // Scheibe wurde gefangen oder fallen gelassen und ist weg
			}
		}

		// Normale Projektil-Physik: bewegen + Treffer erkennen (ruft onHitEntity / onHitBlock auf).
		super.tick();
	}

	/** @return false, wenn die Scheibe in diesem Tick entfernt wurde. */
	private boolean steer(ServerLevel level) {
		flightTicks++;

		if (flightTicks > MAX_LIFETIME_TICKS) {
			giveBackOrDrop(level);
			return false;
		}

		// kleine Funken als Flugspur
		if (tickCount % 2 == 0) {
			level.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
		}

		if (!returning && flightTicks > BASE_OUTBOUND_TICKS + bounces * TICKS_PER_BOUNCE) {
			startReturning();
		}

		if (!returning) {
			// Konstante Geschwindigkeit halten (Minecraft bremst Projektile sonst jeden Tick ein bisschen ab).
			Vec3 motion = getDeltaMovement();
			if (motion.lengthSqr() > 1.0E-6D) {
				setDeltaMovement(motion.normalize().scale(FLY_SPEED));
			}
			return true;
		}

		// Rueckweg: direkt auf den Werfer zufliegen.
		LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
		if (owner == null || !owner.isAlive() || owner.level() != level) {
			// Werfer ist weg (offline, tot, andere Dimension): Scheibe faellt als Item auf den Boden.
			giveBackOrDrop(level);
			return false;
		}

		Vec3 target = owner.getEyePosition().subtract(0.0D, 0.4D, 0.0D);
		Vec3 toOwner = target.subtract(position());
		double distance = toOwner.length();

		if (distance < CATCH_DISTANCE) {
			giveBackOrDrop(level); // gefangen!
			return false;
		}

		// Nie weiter fliegen als die Strecke zum Werfer, damit sie nicht an ihm vorbeischiesst.
		setDeltaMovement(toOwner.scale(Math.min(RETURN_SPEED, distance) / distance));
		return true;
	}

	private void startReturning() {
		returning = true;
		alreadyHit.clear(); // auf dem Rueckweg kann jeder Gegner nochmal getroffen werden
	}

	// ------------------------------------------------------------------------------------------
	// Treffer
	// ------------------------------------------------------------------------------------------

	@Override
	protected boolean canHitEntity(Entity entity) {
		// Den Werfer selbst und schon getroffene Gegner ignorieren -> die Scheibe fliegt hindurch.
		return entity != getOwner() && !alreadyHit.contains(entity.getId()) && super.canHitEntity(entity);
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);

		Entity target = hitResult.getEntity();
		alreadyHit.add(target.getId());

		// Schaden nur auf dem Server. WICHTIG: kein discard() - die Scheibe fliegt weiter!
		if (level() instanceof ServerLevel serverLevel) {
			DamageSource source = serverLevel.damageSources().thrown(this, getOwner());

			if (target.hurtServer(serverLevel, source, tier().damage())) {
				serverLevel.playSound(null, getX(), getY(), getZ(),
						SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.8F, 1.2F);
				serverLevel.sendParticles(ParticleTypes.CRIT,
						target.getX(), target.getY() + target.getBbHeight() / 2.0D, target.getZ(),
						8, 0.2D, 0.2D, 0.2D, 0.3D);
			}
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		super.onHitBlock(hitResult);

		if (returning) {
			// Auf dem Rueckweg ist ein Block im Weg: Scheibe springt direkt zum Werfer zurueck.
			if (level() instanceof ServerLevel serverLevel) {
				giveBackOrDrop(serverLevel);
			}
			return;
		}

		// Abprallen: Bewegung an der getroffenen Blockflaeche spiegeln.
		Direction face = hitResult.getDirection();
		Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
		Vec3 motion = getDeltaMovement();
		setDeltaMovement(motion.subtract(normal.scale(2.0D * motion.dot(normal))));

		// Ein kleines Stueck von der Wand wegschieben, damit sie nicht sofort wieder dieselbe Wand trifft.
		setPos(hitResult.getLocation().add(normal.scale(0.1D)));

		bounces++;
		alreadyHit.clear(); // nach einem Abpraller kann jeder Gegner wieder getroffen werden

		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.playSound(null, getX(), getY(), getZ(),
					SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 0.8F, 1.8F);
			serverLevel.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 6, 0.1D, 0.1D, 0.1D, 0.2D);

			if (bounces > MAX_BOUNCES) {
				startReturning();
			}
		}
	}

	// ------------------------------------------------------------------------------------------
	// Zurueckgeben
	// ------------------------------------------------------------------------------------------

	/**
	 * Gibt dem Werfer die Scheibe zurueck (dorthin, woher er sie geworfen hat) - oder laesst sie als Item fallen,
	 * wenn der Werfer nicht erreichbar ist. Zieht dabei 1 Haltbarkeitspunkt ab.
	 */
	private void giveBackOrDrop(ServerLevel level) {
		if (isRemoved()) {
			return;
		}

		Entity ownerEntity = getOwner();

		// Kreativ-Spieler verlieren ihre Scheibe beim Werfen nie (sie wird nicht verbraucht) -> einfach entfernen.
		if (ownerEntity instanceof Player creativePlayer && creativePlayer.isCreative()) {
			discard();
			return;
		}

		ItemStack stack = getItem().copy();

		// 1 Haltbarkeitspunkt pro Wurf
		if (stack.isDamageableItem()) {
			int damage = stack.getDamageValue() + 1;

			if (damage >= stack.getMaxDamage()) {
				// Scheibe ist kaputt
				level.playSound(null, getX(), getY(), getZ(),
						SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
				discard();
				return;
			}

			stack.setDamageValue(damage);
		}

		if (ownerEntity instanceof Player player && player.isAlive() && player.level() == level) {
			InteractionHand hand = thrownFromOffHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

			if (player.getItemInHand(hand).isEmpty()) {
				// Wieder in die Hand, aus der sie geworfen wurde.
				player.setItemInHand(hand, stack);
			} else if (!player.getInventory().add(stack)) {
				// Inventar voll -> vor die Fuesse des Spielers legen.
				level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY() + 0.5D, player.getZ(), stack));
			}

			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, 1.6F);
		} else {
			level.addFreshEntity(new ItemEntity(level, getX(), getY(), getZ(), stack));
		}

		discard();
	}
}
