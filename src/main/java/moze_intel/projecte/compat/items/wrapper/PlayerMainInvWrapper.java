package moze_intel.projecte.compat.items.wrapper;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port shim: wraps only the main (hotbar + storage) portion of a player's inventory.
 */
public class PlayerMainInvWrapper extends InvWrapper {

	private final Inventory inventoryPlayer;

	public PlayerMainInvWrapper(Inventory inv) {
		super(inv);
		this.inventoryPlayer = inv;
	}

	@Override
	public int getSlots() {
		return inventoryPlayer.items.size();
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		ItemStack rest = super.insertItem(slot, stack, simulate);
		if (rest.getCount() != stack.getCount()) {
			ItemStack inSlot = getStackInSlot(slot);
			if (!inSlot.isEmpty() && !simulate && inventoryPlayer.player.level().isClientSide) {
				inSlot.setPopTime(5);
			}
		}
		return rest;
	}

	public Inventory getInventoryPlayer() {
		return inventoryPlayer;
	}
}
