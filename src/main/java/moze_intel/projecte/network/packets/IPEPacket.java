package moze_intel.projecte.network.packets;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import moze_intel.projecte.compat.network.IPayloadContext;

public interface IPEPacket extends CustomPacketPayload {

	void handle(IPayloadContext context);
}