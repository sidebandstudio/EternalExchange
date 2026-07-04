package moze_intel.projecte.api;

import moze_intel.projecte.compat.registries.PEDeferredHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

public class PEDataComponents {

	public static final PEDeferredHolder<DataComponentType<Integer>> CHARGE = get("charge");

	private PEDataComponents() {
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <TYPE> PEDeferredHolder<DataComponentType<TYPE>> get(String name) {
		return (PEDeferredHolder) PEDeferredHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath(ProjectEAPI.PROJECTE_MODID, name));
	}
}
