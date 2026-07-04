package moze_intel.projecte.client;

import moze_intel.projecte.events.ToolTipEvent;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.utils.ClientKeyHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class PEClient implements ClientModInitializer {

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void onInitializeClient() {
		ClientKeyHelper.registerKeyBindings();
		ToolTipEvent.register();
		//Install the server-to-client packet receivers collected during common init
		PacketHandler.registerClientReceivers((type, handler) ->
				ClientPlayNetworking.registerGlobalReceiver((CustomPacketPayload.Type) type,
						(payload, context) -> ((PacketHandler.IPayloadHandler) handler).handle((CustomPacketPayload) payload, context::player)));
		//TODO Fabric port: screens (MenuScreens.register), block entity renderers, entity renderers, item properties, layer definitions (upstream client/PEClient.java)
	}
}
