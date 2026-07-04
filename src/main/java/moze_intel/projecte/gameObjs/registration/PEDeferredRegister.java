package moze_intel.projecte.gameObjs.registration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port: reimplementation of upstream's DeferredRegister subclass on top of direct vanilla registration. Entries are queued in declaration order and
 * registered when {@link #register()} is called from mod init (replacing NeoForge's {@code register(IEventBus)}).
 */
public class PEDeferredRegister<T> {

	@NotNull
	private final Function<ResourceKey<T>, ? extends PEDeferredHolder<T, ?>> holderCreator;
	private final ResourceKey<? extends Registry<T>> registryKey;
	private final String namespace;
	private final List<Runnable> pendingEntries = new ArrayList<>();
	private final List<PEDeferredHolder<T, ? extends T>> allEntries = new ArrayList<>();
	private final Set<String> seenNames = new HashSet<>();
	private boolean registered;

	public PEDeferredRegister(@NotNull ResourceKey<? extends Registry<T>> registryKey, @NotNull String namespace) {
		this(registryKey, namespace, PEDeferredHolder::new);
	}

	public static <T> PEDeferredRegister<T> create(@NotNull ResourceKey<? extends Registry<T>> registryKey, @NotNull String namespace) {
		return new PEDeferredRegister<>(registryKey, namespace);
	}

	public PEDeferredRegister(@NotNull ResourceKey<? extends Registry<T>> registryKey, @NotNull String namespace,
			@NotNull Function<ResourceKey<T>, ? extends PEDeferredHolder<T, ? extends T>> holderCreator) {
		this.registryKey = registryKey;
		this.namespace = namespace;
		this.holderCreator = holderCreator;
	}

	@NotNull
	public <I extends T> PEDeferredHolder<T, I> register(@NotNull String name, @NotNull Supplier<? extends I> sup) {
		return register(name, rl -> sup.get());
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public <I extends T> PEDeferredHolder<T, I> register(@NotNull String name, @NotNull Function<ResourceLocation, ? extends I> func) {
		if (!seenNames.add(name)) {
			throw new IllegalArgumentException("Duplicate registration: " + name);
		}
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, name);
		PEDeferredHolder<T, I> holder = (PEDeferredHolder<T, I>) holderCreator.apply(ResourceKey.create(registryKey, id));
		allEntries.add(holder);
		pendingEntries.add(() -> {
			I value = func.apply(id);
			Holder<T> vanillaHolder = Registry.registerForHolder(registry(), id, value);
			holder.bind(value, vanillaHolder);
		});
		return holder;
	}

	/**
	 * Performs the queued registrations, in declaration order.
	 */
	public void register() {
		if (registered) {
			throw new IllegalStateException("Register " + registryKey.location() + " for " + namespace + " already ran");
		}
		registered = true;
		pendingEntries.forEach(Runnable::run);
		pendingEntries.clear();
	}

	@SuppressWarnings("unchecked")
	protected Registry<T> registry() {
		Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
		if (registry == null) {
			throw new IllegalStateException("Missing registry: " + registryKey.location());
		}
		return registry;
	}

	public ResourceKey<? extends Registry<T>> getRegistryKey() {
		return registryKey;
	}

	public String getNamespace() {
		return namespace;
	}

	@NotNull
	public java.util.Collection<PEDeferredHolder<T, ? extends T>> getEntries() {
		return java.util.Collections.unmodifiableList(allEntries);
	}
}
