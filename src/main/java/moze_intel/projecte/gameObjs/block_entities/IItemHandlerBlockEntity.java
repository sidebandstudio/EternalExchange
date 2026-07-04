package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.compat.items.IItemHandler;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port shim: exposes the item handler a ProjectE block entity registered through NeoForge's {@code Capabilities.ItemHandler.BLOCK}, so our own blocks
 * (comparator signals, inventory drops) can query it directly. Replace usages with a block item handler {@code BlockApiLookup} if this port ever gains one.
 */
public interface IItemHandlerBlockEntity {

	/**
	 * @param side The side being queried, or {@code null} for the full/internal view (mirrors the NeoForge capability context).
	 */
	@Nullable
	IItemHandler getItemHandler(@Nullable Direction side);

	/**
	 * Fabric port: mirror of NeoForge's {@code ItemHandlerHelper.calcRedstoneFromInventory}, here for lack of a better place.
	 */
	static int calcRedstoneFromInventory(@Nullable IItemHandler inv) {
		if (inv == null) {
			return 0;
		}
		int itemsFound = 0;
		float proportion = 0.0F;
		for (int slot = 0, slots = inv.getSlots(); slot < slots; slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			if (!stack.isEmpty()) {
				proportion += stack.getCount() / (float) Math.min(inv.getSlotLimit(slot), stack.getMaxStackSize());
				itemsFound++;
			}
		}
		proportion = proportion / inv.getSlots();
		return Mth.floor(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
	}
}
