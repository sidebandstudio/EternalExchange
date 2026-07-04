package moze_intel.projecte.compat.items;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port shim: minimal clean-room equivalent of NeoForge's item handler contract, kept source-compatible so upstream ProjectE code ports with an import swap.
 */
public interface IItemHandler {

	int getSlots();

	@NotNull
	ItemStack getStackInSlot(int slot);

	/**
	 * Inserts an ItemStack into the given slot and returns the remainder.
	 *
	 * @param simulate if true, the insertion is only simulated
	 */
	@NotNull
	ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate);

	/**
	 * Extracts up to amount items from the given slot and returns them.
	 *
	 * @param simulate if true, the extraction is only simulated
	 */
	@NotNull
	ItemStack extractItem(int slot, int amount, boolean simulate);

	int getSlotLimit(int slot);

	boolean isItemValid(int slot, @NotNull ItemStack stack);
}
