package moze_intel.projecte.integration;

import moze_intel.projecte.PECore;
import moze_intel.projecte.compat.items.IItemHandler;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;

/**
 * Fabric port stub: recipe viewer/CraftTweaker/Curios/Jade/TOP/WTHIT integrations are not ported yet. The Curios lookup exists so EMC search code compiles;
 * nothing registers providers for it until a Trinkets integration is written.
 */
public final class IntegrationHelper {

	public static final String CURIO_MODID = "curios";
	public static final String TOP_MODID = "theoneprobe";

	public static final EntityApiLookup<IItemHandler, Void> CURIO_ITEM_HANDLER =
			EntityApiLookup.get(PECore.rl("curio_item_handler"), IItemHandler.class, Void.class);

	private IntegrationHelper() {
	}
}
