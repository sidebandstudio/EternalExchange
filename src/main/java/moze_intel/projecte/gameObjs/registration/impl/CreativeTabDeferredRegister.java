package moze_intel.projecte.gameObjs.registration.impl;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import moze_intel.projecte.gameObjs.registration.PEDeferredHolder;
import moze_intel.projecte.gameObjs.registration.PEDeferredRegister;
import moze_intel.projecte.utils.text.ILangEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import moze_intel.projecte.compat.events.BuildCreativeModeTabContentsEvent;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.level.ItemLike;

public class CreativeTabDeferredRegister extends PEDeferredRegister<CreativeModeTab> {

	private final Consumer<BuildCreativeModeTabContentsEvent> addToExistingTabs;

	public CreativeTabDeferredRegister(String modid, Consumer<BuildCreativeModeTabContentsEvent> addToExistingTabs) {
		super(Registries.CREATIVE_MODE_TAB, modid);
		this.addToExistingTabs = addToExistingTabs;
	}

	@Override
	public void register() {
		super.register();
		ItemGroupEvents.MODIFY_ENTRIES_ALL.register((tab, entries) ->
				BuildCreativeModeTabContentsEvent.wrap(tab, entries).ifPresent(addToExistingTabs));
	}

	/**
	 * @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
	 */
	public PEDeferredHolder<CreativeModeTab, CreativeModeTab> registerMain(ILangEntry title, ItemLike icon, UnaryOperator<CreativeModeTab.Builder> operator) {
		return register(getNamespace(), title, icon, operator);
	}

	/**
	 * @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
	 */
	public PEDeferredHolder<CreativeModeTab, CreativeModeTab> register(String name, ILangEntry title, ItemLike icon, UnaryOperator<CreativeModeTab.Builder> operator) {
		return register(name, () -> {
			CreativeModeTab.Builder builder = CreativeModeTab.builder()
					.title(title.translate())
					.icon(() -> icon.asItem().getDefaultInstance());
			return operator.apply(builder).build();
		});
	}
}