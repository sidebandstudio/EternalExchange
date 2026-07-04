package moze_intel.projecte.compat.network;

import net.minecraft.world.entity.player.Player;

/**
 * Fabric port shim: the only part of NeoForge's payload context ProjectE uses. On the server this is the sending ServerPlayer; on the client the local player.
 */
@FunctionalInterface
public interface IPayloadContext {

	Player player();
}
