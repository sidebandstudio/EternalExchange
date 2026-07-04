package moze_intel.projecte.compat.fluids;

/**
 * Fabric port shim: holds NeoForge's bucket volume constant (millibuckets). Fabric's transfer API uses 81000 droplets per bucket; convert at the boundary.
 */
public final class FluidType {

	public static final int BUCKET_VOLUME = 1000;

	private FluidType() {
	}
}
