package moze_intel.projecte.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port: upstream discovers mappers/processors by scanning all mods for annotations; Fabric has no annotation scanning, so discovery uses entrypoints
 * instead. Mods (including ProjectE itself) list implementations in fabric.mod.json under the keys {@code projecte:emc_mappers},
 * {@code projecte:recipe_type_mappers} and {@code projecte:data_component_processors}. The upstream annotations are still read (reflectively) for priority and
 * required-mods filtering.
 */
public class AnnotationHelper {

	public static final String ENTRYPOINT_EMC_MAPPERS = "projecte:emc_mappers";
	public static final String ENTRYPOINT_RECIPE_TYPE_MAPPERS = "projecte:recipe_type_mappers";
	public static final String ENTRYPOINT_DATA_COMPONENT_PROCESSORS = "projecte:data_component_processors";

	public static List<IDataComponentProcessor> getDataComponentProcessors() {
		return getEntrypoints(ENTRYPOINT_DATA_COMPONENT_PROCESSORS, IDataComponentProcessor.class, DataComponentProcessor.class,
				DataComponentProcessor::priority, DataComponentProcessor::requiredMods, IDataComponentProcessor::getName);
	}

	public static List<IRecipeTypeMapper> getRecipeTypeMappers() {
		return getEntrypoints(ENTRYPOINT_RECIPE_TYPE_MAPPERS, IRecipeTypeMapper.class, RecipeTypeMapper.class,
				RecipeTypeMapper::priority, RecipeTypeMapper::requiredMods, IRecipeTypeMapper::getName);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static List<IEMCMapper<NormalizedSimpleStack, Long>> getEMCMappers() {
		List<IEMCMapper> mappers = getEntrypoints(ENTRYPOINT_EMC_MAPPERS, IEMCMapper.class, EMCMapper.class,
				EMCMapper::priority, EMCMapper::requiredMods, IEMCMapper::getName);
		return (List<IEMCMapper<NormalizedSimpleStack, Long>>) (List) mappers;
	}

	private static <T, A extends Annotation> List<T> getEntrypoints(String key, Class<T> type, Class<A> annotationClass, ToIntFunction<A> priorityGetter,
			Function<A, String[]> requiredModsGetter, Function<T, String> nameGetter) {
		List<T> found = new ArrayList<>();
		Object2IntMap<T> priorities = new Object2IntOpenHashMap<>();
		for (EntrypointContainer<T> container : FabricLoader.getInstance().getEntrypointContainers(key, type)) {
			T instance;
			try {
				instance = container.getEntrypoint();
			} catch (Throwable t) {
				PECore.LOGGER.error("Failed to load '{}' entrypoint from mod {}", key, container.getProvider().getMetadata().getId(), t);
				continue;
			}
			int priority = 0;
			A annotation = instance.getClass().getAnnotation(annotationClass);
			if (annotation != null) {
				if (!checkRequiredMods(requiredModsGetter.apply(annotation))) {
					PECore.debugLog("Skipping {} from {}: required mods are not loaded", type.getSimpleName(), instance.getClass().getName());
					continue;
				}
				priority = priorityGetter.applyAsInt(annotation);
			}
			found.add(instance);
			priorities.put(instance, priority);
			PECore.debugLog("Found and loaded {}: {}, with priority {}", type.getSimpleName(), nameGetter.apply(instance), priority);
		}
		found.sort(Comparator.comparingInt(priorities::getInt).reversed());
		return found;
	}

	private static boolean checkRequiredMods(@Nullable String[] requiredMods) {
		if (requiredMods != null) {
			for (String requiredMod : requiredMods) {
				if (!requiredMod.isEmpty() && !FabricLoader.getInstance().isModLoaded(requiredMod)) {
					return false;
				}
			}
		}
		return true;
	}
}
