package moze_intel.projecte.api.event;

import java.util.UUID;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired serverside after a players transmutation knowledge is changed
 * <p>
 * Fabric port note: register listeners via {@link #EVENT}
 */
public class PlayerKnowledgeChangeEvent {

	public static final Event<Callback> EVENT = EventFactory.createArrayBacked(Callback.class, listeners -> event -> {
		for (Callback listener : listeners) {
			listener.onKnowledgeChange(event);
		}
	});

	@FunctionalInterface
	public interface Callback {

		void onKnowledgeChange(PlayerKnowledgeChangeEvent event);
	}

	private final UUID playerUUID;

	public PlayerKnowledgeChangeEvent(@NotNull Player player) {
		this(player.getUUID());
	}

	public PlayerKnowledgeChangeEvent(@NotNull UUID playerUUID) {
		this.playerUUID = playerUUID;
	}

	/**
	 * @return The player UUID whose knowledge changed. The associated player may or may not be logged in when this event fires.
	 */
	@NotNull
	public UUID getPlayerUUID() {
		return playerUUID;
	}
}
