package moze_intel.projecte.network.packets.to_client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port: client-to-common indirection so to-client packet classes carry no client-only types in their constant pools (the dedicated server classloader
 * refuses to load them otherwise). This class itself only ever loads on the client, at first invocation from a packet handler.
 */
public final class ClientPayloadHelper {

	private ClientPayloadHelper() {
	}

	@Nullable
	public static Player getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	@Nullable
	public static net.minecraft.world.level.Level getClientLevel() {
		return Minecraft.getInstance().level;
	}

	public static boolean isJumpPressed(Player player) {
		return player instanceof net.minecraft.client.player.LocalPlayer clientPlayer && clientPlayer.input.jumping;
	}
}
