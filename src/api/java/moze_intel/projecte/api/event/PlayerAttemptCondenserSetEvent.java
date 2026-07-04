package moze_intel.projecte.api.event;

import moze_intel.projecte.api.ItemInfo;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired on the server when a player is attempting to place an item in the condenser.
 * <p>
 * Fabric port note: register listeners via {@link #EVENT}. Cancel via {@link #setCanceled(boolean)}; once canceled, remaining listeners are skipped.
 */
public class PlayerAttemptCondenserSetEvent {

	public static final Event<Callback> EVENT = EventFactory.createArrayBacked(Callback.class, listeners -> event -> {
		for (Callback listener : listeners) {
			listener.onAttemptCondenserSet(event);
			if (event.isCanceled()) {
				return;
			}
		}
	});

	@FunctionalInterface
	public interface Callback {

		void onAttemptCondenserSet(PlayerAttemptCondenserSetEvent event);
	}

	private final Player player;
	private final ItemInfo sourceInfo;
	private final ItemInfo reducedInfo;
	private boolean canceled;

	public PlayerAttemptCondenserSetEvent(@NotNull Player player, @NotNull ItemInfo sourceInfo, @NotNull ItemInfo reducedInfo) {
		this.player = player;
		this.sourceInfo = sourceInfo;
		this.reducedInfo = reducedInfo;
	}

	/**
	 * @return The player this event is about.
	 */
	@NotNull
	public Player getPlayer() {
		return player;
	}

	/**
	 * @return The {@link ItemInfo} this event is about.
	 */
	@NotNull
	public ItemInfo getSourceInfo() {
		return sourceInfo;
	}

	/**
	 * Gets the "cleaned" {@link ItemInfo} which may have reduced data component information.
	 *
	 * @return The "cleaned" {@link ItemInfo} this event is about.
	 */
	@NotNull
	public ItemInfo getReducedInfo() {
		return reducedInfo;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}
