package moze_intel.projecte.compat.items;

import moze_intel.projecte.PECore;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port shim: stands in for NeoForge's Capabilities.ItemHandler. ProjectE's own machines expose their inventories through this lookup; there is no
 * cross-mod pipe interop until an adapter to the Fabric transfer API is added.
 */
public final class ItemHandler {

	public static final BlockApiLookup<IItemHandler, @Nullable Direction> BLOCK = BlockApiLookup.get(PECore.rl("item_handler"), IItemHandler.class, Direction.class);

	public static final ItemApiLookup<IItemHandler, Void> ITEM = ItemApiLookup.get(PECore.rl("item_handler"), IItemHandler.class, Void.class);

	private ItemHandler() {
	}
}
