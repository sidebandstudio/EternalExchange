package moze_intel.projecte.gameObjs.registration;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public class DoublePEDeferredRegister<PRIMARY, SECONDARY> {

	@NotNull
	protected final PEDeferredRegister<PRIMARY> primaryRegister;
	@NotNull
	protected final PEDeferredRegister<SECONDARY> secondaryRegister;

	public DoublePEDeferredRegister(@NotNull PEDeferredRegister<PRIMARY> primaryRegistry, @NotNull PEDeferredRegister<SECONDARY> secondaryRegistry) {
		this.primaryRegister = primaryRegistry;
		this.secondaryRegister = secondaryRegistry;
	}

	protected DoublePEDeferredRegister(ResourceKey<? extends Registry<PRIMARY>> primaryRegistryName, ResourceKey<? extends Registry<SECONDARY>> secondaryRegistryName,
			String modid) {
		this(primaryRegistryName, PEDeferredRegister.create(secondaryRegistryName, modid), modid);
	}

	protected DoublePEDeferredRegister(ResourceKey<? extends Registry<PRIMARY>> primaryRegistryName, PEDeferredRegister<SECONDARY> secondaryRegistry, String modid) {
		this(PEDeferredRegister.create(primaryRegistryName, modid), secondaryRegistry);
	}

	public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<PRIMARY, P, SECONDARY, S>> W register(String name,
			Supplier<? extends P> primarySupplier, Supplier<? extends S> secondarySupplier, BiFunction<PEDeferredHolder<PRIMARY, P>,
			PEDeferredHolder<SECONDARY, S>, W> objectWrapper) {
		return objectWrapper.apply(primaryRegister.register(name, primarySupplier), secondaryRegister.register(name, secondarySupplier));
	}

	public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<PRIMARY, P, SECONDARY, S>> W register(String name,
			Supplier<? extends P> primarySupplier, Function<P, S> secondarySupplier, BiFunction<PEDeferredHolder<PRIMARY, P>,
			PEDeferredHolder<SECONDARY, S>, W> objectWrapper) {
		return registerAdvanced(name, primarySupplier, secondarySupplier.compose(Supplier::get), objectWrapper);
	}

	public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<PRIMARY, P, SECONDARY, S>> W registerAdvanced(String name,
			Supplier<? extends P> primarySupplier, Function<PEDeferredHolder<PRIMARY, P>, S> secondarySupplier, BiFunction<PEDeferredHolder<PRIMARY, P>,
			PEDeferredHolder<SECONDARY, S>, W> objectWrapper) {
		PEDeferredHolder<PRIMARY, P> primaryObject = primaryRegister.register(name, primarySupplier);
		return objectWrapper.apply(primaryObject, secondaryRegister.register(name, () -> secondarySupplier.apply(primaryObject)));
	}

	public void register() {
		primaryRegister.register();
		secondaryRegister.register();
	}

	public Collection<PEDeferredHolder<PRIMARY, ? extends PRIMARY>> getPrimaryEntries() {
		return primaryRegister.getEntries();
	}

	public Collection<PEDeferredHolder<SECONDARY, ? extends SECONDARY>> getSecondaryEntries() {
		return secondaryRegister.getEntries();
	}
}