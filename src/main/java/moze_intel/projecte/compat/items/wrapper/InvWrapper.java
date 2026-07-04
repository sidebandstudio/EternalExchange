package moze_intel.projecte.compat.items.wrapper;

import moze_intel.projecte.compat.items.IItemHandlerModifiable;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port shim: clean-room Container -> IItemHandler adapter mirroring NeoForge's InvWrapper semantics.
 */
public class InvWrapper implements IItemHandlerModifiable {

	private final Container inv;

	public InvWrapper(Container inv) {
		this.inv = inv;
	}

	public Container getInv() {
		return inv;
	}

	@Override
	public int getSlots() {
		return inv.getContainerSize();
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv.getItem(slot);
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack stackInSlot = inv.getItem(slot);
		if (!stackInSlot.isEmpty()) {
			if (stackInSlot.getCount() >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot))
				|| !ItemStack.isSameItemSameComponents(stack, stackInSlot)
				|| !inv.canPlaceItem(slot, stack)) {
				return stack;
			}
			int limit = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.getCount();
			if (stack.getCount() <= limit) {
				if (!simulate) {
					ItemStack copy = stack.copy();
					copy.grow(stackInSlot.getCount());
					inv.setItem(slot, copy);
					inv.setChanged();
				}
				return ItemStack.EMPTY;
			}
			stack = stack.copy();
			if (!simulate) {
				ItemStack copy = stack.split(limit);
				copy.grow(stackInSlot.getCount());
				inv.setItem(slot, copy);
				inv.setChanged();
			} else {
				stack.shrink(limit);
			}
			return stack;
		}
		if (!inv.canPlaceItem(slot, stack)) {
			return stack;
		}
		int limit = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
		if (stack.getCount() > limit) {
			stack = stack.copy();
			if (!simulate) {
				inv.setItem(slot, stack.split(limit));
				inv.setChanged();
				return stack;
			}
			stack.shrink(limit);
			return stack;
		}
		if (!simulate) {
			inv.setItem(slot, stack);
			inv.setChanged();
		}
		return ItemStack.EMPTY;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0) {
			return ItemStack.EMPTY;
		}
		ItemStack stackInSlot = inv.getItem(slot);
		if (stackInSlot.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (simulate) {
			return stackInSlot.copyWithCount(Math.min(stackInSlot.getCount(), amount));
		}
		int m = Math.min(stackInSlot.getCount(), amount);
		ItemStack decrStackSize = inv.removeItem(slot, m);
		inv.setChanged();
		return decrStackSize;
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		inv.setItem(slot, stack);
	}

	@Override
	public int getSlotLimit(int slot) {
		return inv.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return inv.canPlaceItem(slot, stack);
	}
}
