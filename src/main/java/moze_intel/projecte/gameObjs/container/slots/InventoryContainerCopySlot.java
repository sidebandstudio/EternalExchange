package moze_intel.projecte.gameObjs.container.slots;

import net.minecraft.world.item.ItemStack;
import moze_intel.projecte.compat.items.IItemHandler;

public class InventoryContainerCopySlot extends ItemHandlerCopySlot implements IInventoryContainerSlot {

    public InventoryContainerCopySlot(IItemHandler itemHandler, int index, int x, int y) {
        super(itemHandler, index, x, y);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(getMaxStackSize(), stack.getMaxStackSize());
    }
}