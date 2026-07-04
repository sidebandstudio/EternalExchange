package moze_intel.projecte.compat.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port shim: clean-room equivalents of the NeoForge ItemHandlerHelper methods ProjectE uses.
 */
public final class ItemHandlerHelper {

	private ItemHandlerHelper() {
	}

	/**
	 * Inserts the stack, preferring slots that already contain the same item, and returns the remainder.
	 */
	@NotNull
	public static ItemStack insertItemStacked(IItemHandler inventory, @NotNull ItemStack stack, boolean simulate) {
		if (inventory == null || stack.isEmpty()) {
			return stack;
		}
		if (!stack.isStackable()) {
			return insertItem(inventory, stack, simulate);
		}
		int slots = inventory.getSlots();
		for (int slot = 0; slot < slots && !stack.isEmpty(); slot++) {
			if (ItemStack.isSameItemSameComponents(inventory.getStackInSlot(slot), stack)) {
				stack = inventory.insertItem(slot, stack, simulate);
			}
		}
		if (!stack.isEmpty()) {
			stack = insertItem(inventory, stack, simulate);
		}
		return stack;
	}

	/**
	 * Inserts the stack into the first slots that accept it and returns the remainder.
	 */
	@NotNull
	public static ItemStack insertItem(IItemHandler inventory, @NotNull ItemStack stack, boolean simulate) {
		if (inventory == null || stack.isEmpty()) {
			return stack;
		}
		int slots = inventory.getSlots();
		for (int slot = 0; slot < slots && !stack.isEmpty(); slot++) {
			stack = inventory.insertItem(slot, stack, simulate);
		}
		return stack;
	}

	/**
	 * Comparator signal strength for an inventory, mirroring NeoForge's helper.
	 */
	public static int calcRedstoneFromInventory(@org.jetbrains.annotations.Nullable IItemHandler inv) {
		if (inv == null) {
			return 0;
		}
		int itemsFound = 0;
		float proportion = 0.0F;
		for (int j = 0; j < inv.getSlots(); j++) {
			ItemStack stack = inv.getStackInSlot(j);
			if (!stack.isEmpty()) {
				proportion += (float) stack.getCount() / Math.min(inv.getSlotLimit(j), stack.getMaxStackSize());
				itemsFound++;
			}
		}
		proportion = proportion / inv.getSlots();
		return net.minecraft.util.Mth.floor(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
	}

	/**
	 * Gives the stack to the player, dropping any remainder at their position.
	 */
	public static void giveItemToPlayer(Player player, @NotNull ItemStack stack) {
		if (!stack.isEmpty() && !player.addItem(stack)) {
			player.drop(stack, false);
		}
	}
}
