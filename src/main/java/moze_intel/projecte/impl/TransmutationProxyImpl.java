package moze_intel.projecte.impl;

import java.util.Objects;
import java.util.UUID;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import moze_intel.projecte.compat.fml.EffectiveSide;
import moze_intel.projecte.compat.fml.ServerLifecycleHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

public class TransmutationProxyImpl implements ITransmutationProxy {

	@NotNull
	@Override
	public IKnowledgeProvider getKnowledgeProviderFor(@NotNull UUID playerUUID) {
		if (EffectiveSide.isServer()) {
			Objects.requireNonNull(playerUUID);
			MinecraftServer server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(), "Server must be running to query knowledge!");
			Player player = server.getPlayerList().getPlayer(playerUUID);
			if (player != null) {
				return PECapabilities.KNOWLEDGE_CAPABILITY.find(Objects.requireNonNull(player), null);
			}
			return TransmutationOffline.forPlayer(server, playerUUID);
		} else if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			//Fabric port: resolved through ClientPayloadHelper so this class stays loadable on dedicated servers
			Player player = Objects.requireNonNull(moze_intel.projecte.network.packets.to_client.ClientPayloadHelper.getClientPlayer(), "Client player doesn't exist!");
			return PECapabilities.KNOWLEDGE_CAPABILITY.find(player, null);
		}
		throw new IllegalStateException("unreachable");
	}
}