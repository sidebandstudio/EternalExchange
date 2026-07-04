package moze_intel.projecte.compat.events;

import java.util.Optional;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Fabric port shim: mirrors the slice of NeoForge's BuildCreativeModeTabContentsEvent that ProjectE uses, wrapping Fabric's item group entry hooks.
 */
public class BuildCreativeModeTabContentsEvent {

	private final ResourceKey<CreativeModeTab> tabKey;
	private final CreativeModeTab tab;
	private final FabricItemGroupEntries entries;

	public BuildCreativeModeTabContentsEvent(ResourceKey<CreativeModeTab> tabKey, CreativeModeTab tab, FabricItemGroupEntries entries) {
		this.tabKey = tabKey;
		this.tab = tab;
		this.entries = entries;
	}

	public static Optional<BuildCreativeModeTabContentsEvent> wrap(CreativeModeTab tab, FabricItemGroupEntries entries) {
		return BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).map(key -> new BuildCreativeModeTabContentsEvent(key, tab, entries));
	}

	public ResourceKey<CreativeModeTab> getTabKey() {
		return tabKey;
	}

	public CreativeModeTab getTab() {
		return tab;
	}

	public boolean hasPermissions() {
		return entries.shouldShowOpRestrictedItems();
	}

	public void accept(ItemLike item) {
		entries.accept(item);
	}

	public void accept(ItemStack stack) {
		entries.accept(stack);
	}
}
