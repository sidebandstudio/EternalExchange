package moze_intel.projecte.compat.items.wrapper;

import moze_intel.projecte.compat.items.IItemHandlerModifiable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port shim: wraps a player's full inventory (main + armor + offhand) like NeoForge's entity item handler for players.
 */
public class PlayerInvWrapper implements IItemHandlerModifiable {

	private final InvWrapper full;

	public PlayerInvWrapper(Inventory inv) {
		this.full = new InvWrapper(inv);
	}

	@Override
	public int getSlots() {
		return full.getSlots();
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return full.getStackInSlot(slot);
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		return full.insertItem(slot, stack, simulate);
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return full.extractItem(slot, amount, simulate);
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		full.setStackInSlot(slot, stack);
	}

	@Override
	public int getSlotLimit(int slot) {
		return full.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return full.isItemValid(slot, stack);
	}
}
