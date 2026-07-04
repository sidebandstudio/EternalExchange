package moze_intel.projecte.gameObjs.items;

/**
 * Fabric port: items implementing this register their own API lookup providers directly (upstream received NeoForge's RegisterCapabilitiesEvent).
 */
public interface ICapabilityAware {

	void attachCapabilities();
}
