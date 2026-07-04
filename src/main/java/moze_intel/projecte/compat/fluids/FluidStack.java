package moze_intel.projecte.compat.fluids;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port shim: minimal stand-in for NeoForge's FluidStack, covering only the surface ProjectE uses (identity + components + millibucket amounts).
 * <p>
 * Amounts are in NeoForge units (1000 = one bucket); convert at Fabric transfer-API boundaries (81 droplets per millibucket).
 */
public final class FluidStack {

	public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0, DataComponentPatch.EMPTY);

	private final Fluid fluid;
	private final int amount;
	private final DataComponentPatch components;

	public FluidStack(@NotNull Fluid fluid, int amount) {
		this(fluid, amount, DataComponentPatch.EMPTY);
	}

	public FluidStack(@NotNull Fluid fluid, int amount, @NotNull DataComponentPatch components) {
		this.fluid = fluid;
		this.amount = amount;
		this.components = components;
	}

	public FluidStack(@NotNull Holder<Fluid> fluid, int amount) {
		this(fluid.value(), amount);
	}

	public boolean isEmpty() {
		return fluid == Fluids.EMPTY || amount <= 0;
	}

	@NotNull
	public Fluid getFluid() {
		return fluid;
	}

	@NotNull
	public Holder<Fluid> getFluidHolder() {
		return fluid.builtInRegistryHolder();
	}

	public int getAmount() {
		return amount;
	}

	@NotNull
	public DataComponentPatch getComponentsPatch() {
		return components;
	}

	public Component getHoverName() {
		return FluidVariantAttributes.getName(FluidVariant.of(fluid, components));
	}

	public FluidStack copyWithAmount(int amount) {
		return new FluidStack(fluid, amount, components);
	}

	@Override
	public boolean equals(Object o) {
		return o == this || o instanceof FluidStack other && fluid == other.fluid && amount == other.amount && components.equals(other.components);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fluid, amount, components);
	}
}
