package moze_intel.projecte.network;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.items.rings.ArchangelSmite;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.network.packets.IPEPacket;
import moze_intel.projecte.network.packets.to_client.NovaExplosionSyncPKT;
import moze_intel.projecte.network.packets.to_client.SyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.SyncFuelMapperPKT;
import moze_intel.projecte.network.packets.to_client.container.SyncOffhandPkt;
import moze_intel.projecte.network.packets.to_client.SyncWorldTransmutations;
import moze_intel.projecte.network.packets.to_client.container.UpdateCondenserLockPKT;
import moze_intel.projecte.network.packets.to_client.container.UpdateWindowLongPKT;
import moze_intel.projecte.network.packets.to_client.alch_bag.SyncAllBagDataPKT;
import moze_intel.projecte.network.packets.to_client.alch_bag.SyncBagsDataPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncChangePKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncInputsAndLocksPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncPKT;
import moze_intel.projecte.network.packets.to_server.KeyPressPKT;
import moze_intel.projecte.network.packets.to_server.SearchUpdatePKT;
import moze_intel.projecte.network.packets.to_server.UpdateGemModePKT;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import moze_intel.projecte.compat.network.IPayloadContext;
import moze_intel.projecte.compat.network.PacketDistributor;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Heavily based off of Mekanism's packet handler
 */
public final class PacketHandler {

	//Client to server instanced packets
	private SimplePacketPayLoad activateArchangel;

	//Server to client instanced packets
	private SimplePacketPayLoad clearKnowledge;
	private SimplePacketPayLoad updateTransmutationTargets;

	private SimplePacketPayLoad resetCooldown;

	/**
	 * Fabric port: payload types for both directions register immediately (common side); client receivers are collected and installed by the client
	 * entrypoint via {@link #registerClientReceivers(BiConsumer)}.
	 */
	private static final List<ClientReceiver<?>> CLIENT_RECEIVERS = new ArrayList<>();

	public record ClientReceiver<MSG extends CustomPacketPayload>(CustomPacketPayload.Type<MSG> type, IPayloadHandler<MSG> handler) {
	}

	@FunctionalInterface
	public interface IPayloadHandler<MSG extends CustomPacketPayload> {

		void handle(MSG payload, IPayloadContext context);
	}

	public PacketHandler() {
		registerClientToServer(new PacketRegistrar(true));
		registerServerToClient(new PacketRegistrar(false));
	}

	/**
	 * Called from the client entrypoint; installs the collected server-to-client receivers with Fabric's client networking.
	 */
	public static void registerClientReceivers(BiConsumer<CustomPacketPayload.Type<? extends CustomPacketPayload>, IPayloadHandler<?>> consumer) {
		for (ClientReceiver<?> receiver : CLIENT_RECEIVERS) {
			consumer.accept(receiver.type(), receiver.handler());
		}
	}

	private void registerClientToServer(PacketRegistrar registrar) {
		registrar.play(KeyPressPKT.TYPE, KeyPressPKT.STREAM_CODEC);
		activateArchangel = registrar.playInstanced(PECore.rl("activate_archangel"), (ignored, context) -> {
			Player player = context.player();
			ItemStack main = player.getMainHandItem();
			if (!main.isEmpty() && main.is(PEItems.ARCHANGEL_SMITE)) {
				ArchangelSmite.fireVolley(main, player);
			}
		});
		registrar.play(SearchUpdatePKT.TYPE, SearchUpdatePKT.STREAM_CODEC);
		registrar.play(UpdateGemModePKT.TYPE, UpdateGemModePKT.STREAM_CODEC);
	}

	private void registerServerToClient(PacketRegistrar registrar) {
		resetCooldown = registrar.playInstanced(PECore.rl("reset_cooldown"), (ignored, context) -> context.player().resetAttackStrengthTicker());
		clearKnowledge = registrar.playInstanced(PECore.rl("clear_knowledge"), (ignored, context) -> {
			Player player = context.player();
			IKnowledgeProvider knowledge = PECapabilities.KNOWLEDGE_CAPABILITY.find(player, null);
			if (knowledge != null) {
				knowledge.clearKnowledge();
				if (player.containerMenu instanceof TransmutationContainer container) {
					container.transmutationInventory.updateClientTargets(false);
				}
			}
		});
		registrar.play(KnowledgeSyncPKT.TYPE, KnowledgeSyncPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncEmcPKT.TYPE, KnowledgeSyncEmcPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncInputsAndLocksPKT.TYPE, KnowledgeSyncInputsAndLocksPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncChangePKT.TYPE, KnowledgeSyncChangePKT.STREAM_CODEC);
		registrar.play(NovaExplosionSyncPKT.TYPE, NovaExplosionSyncPKT.STREAM_CODEC);
		registrar.play(SyncAllBagDataPKT.TYPE, SyncAllBagDataPKT.STREAM_CODEC);
		registrar.play(SyncBagsDataPKT.TYPE, SyncBagsDataPKT.STREAM_CODEC);
		registrar.play(SyncEmcPKT.TYPE, SyncEmcPKT.STREAM_CODEC);
		registrar.play(SyncOffhandPkt.TYPE, SyncOffhandPkt.STREAM_CODEC);
		registrar.play(SyncFuelMapperPKT.TYPE, SyncFuelMapperPKT.STREAM_CODEC);
		registrar.play(SyncWorldTransmutations.TYPE, SyncWorldTransmutations.STREAM_CODEC);
		registrar.play(UpdateCondenserLockPKT.TYPE, UpdateCondenserLockPKT.STREAM_CODEC);
		updateTransmutationTargets = registrar.playInstanced(PECore.rl("update_transmutation_targets"), (ignored, context) -> {
			if (context.player().containerMenu instanceof TransmutationContainer container) {
				container.transmutationInventory.updateClientTargets(false);
			}
		});
		registrar.play(UpdateWindowLongPKT.TYPE, UpdateWindowLongPKT.STREAM_CODEC);
	}

	public void clearKnowledge(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, clearKnowledge);
	}

	public void updateTransmutationTargets(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, updateTransmutationTargets);
	}

	public void resetCooldown(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, resetCooldown);
	}

	public void activateArchangel() {
		PacketDistributor.sendToServer(activateArchangel);
	}

	protected record SimplePacketPayLoad(CustomPacketPayload.Type<CustomPacketPayload> type) implements CustomPacketPayload {

		private SimplePacketPayLoad(ResourceLocation id) {
			this(new CustomPacketPayload.Type<>(id));
		}
	}

	protected record PacketRegistrar(boolean toServer) {

		public <MSG extends IPEPacket> void play(CustomPacketPayload.Type<MSG> type, StreamCodec<? super RegistryFriendlyByteBuf, MSG> reader) {
			playGeneric(type, reader, IPEPacket::handle);
		}

		public SimplePacketPayLoad playInstanced(ResourceLocation id, IPayloadHandler<CustomPacketPayload> handler) {
			SimplePacketPayLoad payload = new SimplePacketPayLoad(id);
			playGeneric(payload.type(), StreamCodec.unit(payload), handler);
			return payload;
		}

		private <MSG extends CustomPacketPayload> void playGeneric(CustomPacketPayload.Type<MSG> type, StreamCodec<? super RegistryFriendlyByteBuf, MSG> reader,
				IPayloadHandler<MSG> handler) {
			if (toServer) {
				PayloadTypeRegistry.playC2S().register(type, reader);
				ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, context::player));
			} else {
				PayloadTypeRegistry.playS2C().register(type, reader);
				CLIENT_RECEIVERS.add(new ClientReceiver<>(type, handler));
			}
		}
	}
}