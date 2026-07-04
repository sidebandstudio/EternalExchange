package moze_intel.projecte.compat.config;

/**
 * Fabric port shim: stand-in for NeoForge's ModConfig, providing the config type enum upstream code references.
 * <p>
 * On this port all types load from {@code config/ProjectE/*.toml}; SERVER configs are not stored per-world and are not synced to clients (keep them matched manually
 * in multiplayer).
 */
public final class ModConfig {

	private ModConfig() {
	}

	public enum Type {
		COMMON,
		CLIENT,
		SERVER
	}
}
