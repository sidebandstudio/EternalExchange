package moze_intel.projecte.compat.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

/**
 * Fabric port shim: mirrors the NeoForge PacketDistributor helpers ProjectE uses, on top of Fabric networking.
 */
public final class PacketDistributor {

	private PacketDistributor() {
	}

	/**
	 * Client -> server. Only call from client code.
	 */
	public static void sendToServer(CustomPacketPayload payload) {
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);
	}

	public static void sendToPlayer(ServerPlayer player, CustomPacketPayload... payloads) {
		for (CustomPacketPayload payload : payloads) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	public static void sendToAllPlayers(MinecraftServer server, CustomPacketPayload... payloads) {
		for (ServerPlayer player : PlayerLookup.all(server)) {
			sendToPlayer(player, payloads);
		}
	}

	public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload... payloads) {
		for (ServerPlayer player : PlayerLookup.tracking(level, chunkPos)) {
			sendToPlayer(player, payloads);
		}
	}
}
