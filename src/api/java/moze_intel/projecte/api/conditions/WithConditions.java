package moze_intel.projecte.api.conditions;

import java.util.List;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;

/**
 * Fabric port shim: equivalent of NeoForge's WithConditions carrier record, holding Fabric {@link ResourceCondition}s instead.
 */
public record WithConditions<T>(T carrier, List<ResourceCondition> conditions) {

	public WithConditions(T carrier, ResourceCondition... conditions) {
		this(carrier, List.of(conditions));
	}
}
