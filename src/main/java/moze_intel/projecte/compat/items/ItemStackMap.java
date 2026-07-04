package moze_intel.projecte.compat.items;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric port shim: maps keyed by an ItemStack's item + components (count ignored), mirroring NeoForge's ItemStackMap.
 */
public final class ItemStackMap {

	private static final Hash.Strategy<ItemStack> TYPE_AND_TAG = new Hash.Strategy<>() {
		@Override
		public int hashCode(ItemStack o) {
			return o == null ? 0 : ItemStack.hashItemAndComponents(o);
		}

		@Override
		public boolean equals(ItemStack a, ItemStack b) {
			if (a == null || b == null) {
				return a == b;
			}
			return ItemStack.isSameItemSameComponents(a, b);
		}
	};

	private ItemStackMap() {
	}

	public static <V> Map<ItemStack, V> createTypeAndTagMap() {
		return new Object2ObjectOpenCustomHashMap<>(TYPE_AND_TAG);
	}

	public static <V> Map<ItemStack, V> createTypeAndTagLinkedMap() {
		return new Object2ObjectLinkedOpenCustomHashMap<>(TYPE_AND_TAG);
	}
}
