package moze_intel.projecte.compat.items;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port shim: an {@link IItemHandler} whose slots can be set directly, bypassing insertion checks.
 */
public interface IItemHandlerModifiable extends IItemHandler {

	void setStackInSlot(int slot, @NotNull ItemStack stack);
}
