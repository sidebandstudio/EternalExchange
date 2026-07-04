package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.compat.items.IItemHandlerModifiable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port shim: clean-room equivalent of NeoForge's CombinedInvWrapper, exposing several handlers as one contiguous slot space.
 */
public class CombinedInvWrapper implements IItemHandlerModifiable {

	protected final IItemHandlerModifiable[] itemHandler;
	//Index of the first slot after each handler
	protected final int[] baseIndex;
	protected final int slotCount;

	public CombinedInvWrapper(IItemHandlerModifiable... itemHandler) {
		this.itemHandler = itemHandler;
		this.baseIndex = new int[itemHandler.length];
		int index = 0;
		for (int i = 0; i < itemHandler.length; i++) {
			index += itemHandler[i].getSlots();
			baseIndex[i] = index;
		}
		this.slotCount = index;
	}

	protected int getIndexForSlot(int slot) {
		if (slot >= 0) {
			for (int i = 0; i < baseIndex.length; i++) {
				if (slot < baseIndex[i]) {
					return i;
				}
			}
		}
		return -1;
	}

	protected int getSlotFromIndex(int slot, int index) {
		return index <= 0 ? slot : slot - baseIndex[index - 1];
	}

	@Override
	public int getSlots() {
		return slotCount;
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		int index = getIndexForSlot(slot);
		if (index == -1) {
			return ItemStack.EMPTY;
		}
		return itemHandler[index].getStackInSlot(getSlotFromIndex(slot, index));
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		int index = getIndexForSlot(slot);
		if (index != -1) {
			itemHandler[index].setStackInSlot(getSlotFromIndex(slot, index), stack);
		}
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		int index = getIndexForSlot(slot);
		if (index == -1) {
			return stack;
		}
		return itemHandler[index].insertItem(getSlotFromIndex(slot, index), stack, simulate);
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		int index = getIndexForSlot(slot);
		if (index == -1) {
			return ItemStack.EMPTY;
		}
		return itemHandler[index].extractItem(getSlotFromIndex(slot, index), amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		int index = getIndexForSlot(slot);
		if (index == -1) {
			return 0;
		}
		return itemHandler[index].getSlotLimit(getSlotFromIndex(slot, index));
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		int index = getIndexForSlot(slot);
		return index != -1 && itemHandler[index].isItemValid(getSlotFromIndex(slot, index), stack);
	}
}
