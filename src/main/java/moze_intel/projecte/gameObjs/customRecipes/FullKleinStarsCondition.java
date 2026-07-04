package moze_intel.projecte.gameObjs.customRecipes;

import com.mojang.serialization.MapCodec;
import moze_intel.projecte.PECore;
import moze_intel.projecte.config.ProjectEConfig;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

//Fabric port: NeoForge ICondition replaced with Fabric's ResourceCondition; register() must be called once during mod init
public class FullKleinStarsCondition implements ResourceCondition {

	public static final FullKleinStarsCondition INSTANCE = new FullKleinStarsCondition();
	public static final MapCodec<FullKleinStarsCondition> CODEC = MapCodec.unit(INSTANCE);
	public static final ResourceConditionType<FullKleinStarsCondition> TYPE = ResourceConditionType.create(PECore.rl("full_klein_stars"), CODEC);

	private FullKleinStarsCondition() {
	}

	public static void register() {
		ResourceConditions.register(TYPE);
	}

	@Override
	public ResourceConditionType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean test(@Nullable HolderLookup.Provider registryLookup) {
		return ProjectEConfig.common.fullKleinStars.get();
	}
}
