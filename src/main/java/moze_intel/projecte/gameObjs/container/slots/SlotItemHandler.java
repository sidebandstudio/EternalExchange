package moze_intel.projecte.gameObjs.container.slots;

import moze_intel.projecte.compat.items.IItemHandler;
import moze_intel.projecte.compat.items.IItemHandlerModifiable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port shim: clean-room equivalent of NeoForge's SlotItemHandler. A vanilla {@link Slot} that is backed by an {@link IItemHandler} instead of a
 * {@link Container}; every stack access is redirected to the handler, and pickup/stack size checks are based on simulated extraction/insertion so the
 * handler's own rules are honored.
 */
public class SlotItemHandler extends Slot {

	private static final Container emptyInventory = new SimpleContainer(0);

	private final IItemHandler itemHandler;
	protected final int index;

	public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(emptyInventory, index, xPosition, yPosition);
		this.itemHandler = itemHandler;
		this.index = index;
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return !stack.isEmpty() && itemHandler.isItemValid(index, stack);
	}

	@NotNull
	@Override
	public ItemStack getItem() {
		return this.getItemHandler().getStackInSlot(index);
	}

	//Note: Requires the backing item handler to implement IItemHandlerModifiable; override if it does not
	@Override
	public void set(@NotNull ItemStack stack) {
		((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(index, stack);
		this.setChanged();
	}

	/**
	 * Fabric port: on NeoForge this is only ever called by 1.21.2+ client content syncing; it does not exist on the vanilla 1.21.1 {@link Slot}, so it is
	 * declared here (without {@code @Override}) purely so that slots can keep their upstream overrides.
	 */
	//Note: Requires the backing item handler to implement IItemHandlerModifiable; override if it does not
	public void initialize(@NotNull ItemStack stack) {
		((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(index, stack);
		this.setChanged();
	}

	@Override
	public void setChanged() {
		//The backing item handler is responsible for persisting its own changes, and the dummy backing container has nothing to mark dirty
	}

	@Override
	public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {
	}

	@Override
	public int getMaxStackSize() {
		return this.itemHandler.getSlotLimit(this.index);
	}

	@Override
	public int getMaxStackSize(@NotNull ItemStack stack) {
		int maxInput = stack.getMaxStackSize();
		ItemStack maxAdd = stack.copyWithCount(maxInput);
		IItemHandler handler = this.getItemHandler();
		ItemStack currentStack = handler.getStackInSlot(index);
		if (handler instanceof IItemHandlerModifiable handlerModifiable) {
			//Simulate the insertion against an emptied slot so that the current contents don't limit how much is theoretically accepted
			handlerModifiable.setStackInSlot(index, ItemStack.EMPTY);
			ItemStack remainder = handlerModifiable.insertItem(index, maxAdd, true);
			handlerModifiable.setStackInSlot(index, currentStack);
			return maxInput - remainder.getCount();
		}
		//If we cannot temporarily empty the slot, add what the simulation accepts on top of the current contents
		ItemStack remainder = handler.insertItem(index, maxAdd, true);
		return currentStack.getCount() + maxInput - remainder.getCount();
	}

	@Override
	public boolean mayPickup(@NotNull Player playerIn) {
		return !this.getItemHandler().extractItem(index, 1, true).isEmpty();
	}

	@NotNull
	@Override
	public ItemStack remove(int amount) {
		return this.getItemHandler().extractItem(index, amount, false);
	}

	public IItemHandler getItemHandler() {
		return itemHandler;
	}

	/**
	 * Fabric port: NeoForge patches this method onto the vanilla {@link Slot}; for handler backed slots it is equivalent to the index within the handler.
	 *
	 * @return Index in the backing item handler for this slot.
	 */
	public int getSlotIndex() {
		return index;
	}
}
