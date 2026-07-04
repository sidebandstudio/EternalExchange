package moze_intel.projecte.compat.fml;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port shim: tracks the running server like NeoForge's ServerLifecycleHooks. {@link #init()} must be called once from mod init.
 */
public final class ServerLifecycleHooks {

	@Nullable
	private static volatile MinecraftServer currentServer;

	private ServerLifecycleHooks() {
	}

	public static void init() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);
	}

	@Nullable
	public static MinecraftServer getCurrentServer() {
		return currentServer;
	}
}
