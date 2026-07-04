package moze_intel.projecte.gameObjs.container.slots;

import moze_intel.projecte.compat.items.IItemHandler;
import moze_intel.projecte.compat.items.IItemHandlerModifiable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port shim: clean-room equivalent of NeoForge's ItemHandlerCopySlot (backed by its StackCopySlot behavior), for item handlers that only expose
 * copies of their stacks (for example handlers backed by a data component).
 * <p>
 * Vanilla menu code modifies the stack returned by {@link #getItem()} directly and then calls {@link #setChanged()}, so the returned stack is cached and
 * written back into the handler when {@link #setChanged()} is called.
 */
public class ItemHandlerCopySlot extends SlotItemHandler {

	@Nullable
	private ItemStack cachedReturnedStack = null;

	public ItemHandlerCopySlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@NotNull
	@Override
	public ItemStack getItem() {
		return cachedReturnedStack = super.getItem();
	}

	@Override
	public void set(@NotNull ItemStack stack) {
		//Note: Unlike the base handler slot this does not call setChanged, as that would just write the cached stack back a second time
		((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(index, stack);
		cachedReturnedStack = stack;
	}

	@Override
	public void initialize(@NotNull ItemStack stack) {
		//Nothing calls this on 1.21.1, but make sure it would keep the cache consistent instead of writing a stale stack back via setChanged
		set(stack);
	}

	@Override
	public void setChanged() {
		if (cachedReturnedStack != null) {
			set(cachedReturnedStack);
		}
	}

	@NotNull
	@Override
	public ItemStack remove(int amount) {
		//Split the removed amount off a copy of the stored stack and write the remainder back to the handler
		ItemStack stack = super.getItem().copy();
		ItemStack ret = stack.split(amount);
		set(stack);
		cachedReturnedStack = null;
		return ret;
	}
}
