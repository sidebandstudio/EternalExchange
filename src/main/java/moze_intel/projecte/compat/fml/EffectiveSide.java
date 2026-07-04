package moze_intel.projecte.compat.fml;

import net.minecraft.server.MinecraftServer;

/**
 * Fabric port shim: mirrors NeoForge's EffectiveSide thread-based logical side check.
 */
public final class EffectiveSide {

	private EffectiveSide() {
	}

	public static boolean isServer() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return server != null && server.isSameThread();
	}

	public static boolean isClient() {
		return !isServer();
	}
}
