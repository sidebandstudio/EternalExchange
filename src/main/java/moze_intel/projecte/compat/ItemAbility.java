package moze_intel.projecte.compat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fabric port shim: minimal reimplementation of NeoForge's ItemAbility (interned by name). Only used internally by ProjectE's tools; there is no cross-mod
 * ability interop on Fabric.
 */
public final class ItemAbility {

	private static final Map<String, ItemAbility> ABILITIES = new ConcurrentHashMap<>();

	private final String name;

	private ItemAbility(String name) {
		this.name = name;
	}

	public static ItemAbility get(String name) {
		return ABILITIES.computeIfAbsent(name, ItemAbility::new);
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return "ItemAbility[" + name + "]";
	}
}
