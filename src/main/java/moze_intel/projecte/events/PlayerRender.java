package moze_intel.projecte.events;

import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

/**
 * Fabric port: invoked from FovModifierMixin (upstream used ComputeFovModifierEvent).
 */
public class PlayerRender {

	public static float adjustFovModifier(Player player, float fovModifier) {
		if (!player.getItemBySlot(EquipmentSlot.FEET).isEmpty() && player.getItemBySlot(EquipmentSlot.FEET).is(PEItems.GEM_BOOTS)) {
			return fovModifier - 0.5F * Minecraft.getInstance().options.fovEffectScale().get().floatValue();
		}
		return fovModifier;
	}
}
