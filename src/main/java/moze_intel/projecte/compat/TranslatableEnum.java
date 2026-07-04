package moze_intel.projecte.compat;

import net.minecraft.network.chat.Component;

/**
 * Fabric port shim: equivalent of NeoForge's TranslatableEnum interface.
 */
public interface TranslatableEnum {

	Component getTranslatedName();
}
