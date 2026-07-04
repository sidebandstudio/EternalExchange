package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.compat.items.IItemHandlerModifiable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port shim: clean-room equivalent of NeoForge's RangedWrapper, exposing the slot range [minSlot, maxSlotExclusive) of another handler.
 */
public class RangedWrapper implements IItemHandlerModifiable {

	private final IItemHandlerModifiable compose;
	private final int minSlot;
	private final int maxSlotExclusive;

	public RangedWrapper(IItemHandlerModifiable compose, int minSlot, int maxSlotExclusive) {
		if (maxSlotExclusive <= minSlot) {
			throw new IllegalArgumentException("Max slot must be greater than min slot");
		}
		this.compose = compose;
		this.minSlot = minSlot;
		this.maxSlotExclusive = maxSlotExclusive;
	}

	private boolean checkSlot(int slot) {
		return slot >= 0 && slot + minSlot < maxSlotExclusive;
	}

	@Override
	public int getSlots() {
		return maxSlotExclusive - minSlot;
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		if (checkSlot(slot)) {
			return compose.getStackInSlot(slot + minSlot);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		if (checkSlot(slot)) {
			compose.setStackInSlot(slot + minSlot, stack);
		}
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (checkSlot(slot)) {
			return compose.insertItem(slot + minSlot, stack, simulate);
		}
		return stack;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (checkSlot(slot)) {
			return compose.extractItem(slot + minSlot, amount, simulate);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		if (checkSlot(slot)) {
			return compose.getSlotLimit(slot + minSlot);
		}
		return 0;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return checkSlot(slot) && compose.isItemValid(slot + minSlot, stack);
	}
}
