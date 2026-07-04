package moze_intel.projecte.api;

import moze_intel.projecte.compat.registries.LazyRegistryHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

public class PEDataComponents {

	public static final LazyRegistryHolder<DataComponentType<Integer>> CHARGE = get("charge");

	private PEDataComponents() {
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <TYPE> LazyRegistryHolder<DataComponentType<TYPE>> get(String name) {
		return (LazyRegistryHolder) LazyRegistryHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath(ProjectEAPI.PROJECTE_MODID, name));
	}
}
