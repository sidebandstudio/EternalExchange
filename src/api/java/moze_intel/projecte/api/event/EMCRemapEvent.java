package moze_intel.projecte.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * This event is fired on the server after all EMC values are recalculated
 * <p>
 * Fabric port note: register listeners via {@link #EVENT}
 */
public class EMCRemapEvent {

	public static final Event<Callback> EVENT = EventFactory.createArrayBacked(Callback.class, listeners -> event -> {
		for (Callback listener : listeners) {
			listener.onEmcRemap(event);
		}
	});

	@FunctionalInterface
	public interface Callback {

		void onEmcRemap(EMCRemapEvent event);
	}
}
