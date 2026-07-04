package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.gameObjs.customRecipes.FullKleinStarsCondition;
import moze_intel.projecte.gameObjs.customRecipes.TomeEnabledCondition;

/**
 * Fabric port: recipe load conditions register with Fabric's resource conditions system instead of a NeoForge codec registry. Must run before the first
 * datapack load.
 */
public class PERecipeConditions {

	private PERecipeConditions() {
	}

	public static void init() {
		TomeEnabledCondition.register();
		FullKleinStarsCondition.register();
	}
}
