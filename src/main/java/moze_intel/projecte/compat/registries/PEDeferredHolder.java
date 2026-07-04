package moze_intel.projecte.compat.registries;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric port shim: equivalent of NeoForge's DeferredHolder — a {@link Holder} that resolves lazily from the registry on first access, so static references can be
 * created before registration runs.
 */
public class PEDeferredHolder<R> implements Holder<R>, Supplier<R> {

	private final ResourceKey<R> key;
	private Holder<R> resolved;

	protected PEDeferredHolder(ResourceKey<R> key) {
		this.key = key;
	}

	public static <R> PEDeferredHolder<R> create(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation name) {
		return new PEDeferredHolder<>(ResourceKey.create(registryKey, name));
	}

	@SuppressWarnings("unchecked")
	protected Holder<R> resolve() {
		if (resolved == null) {
			Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.get(key.registry());
			if (registry == null) {
				throw new IllegalStateException("Registry not present: " + key.registry());
			}
			resolved = registry.getHolderOrThrow(key);
		}
		return resolved;
	}

	public ResourceKey<R> getKey() {
		return key;
	}

	public ResourceLocation getId() {
		return key.location();
	}

	@Override
	public R get() {
		return value();
	}

	@Override
	@NotNull
	public R value() {
		return resolve().value();
	}

	@Override
	public boolean isBound() {
		Registry<?> registry = BuiltInRegistries.REGISTRY.get(key.registry());
		return registry != null && registry.containsKey(key.location());
	}

	@Override
	public boolean is(@NotNull ResourceLocation location) {
		return key.location().equals(location);
	}

	@Override
	public boolean is(@NotNull ResourceKey<R> resourceKey) {
		return key == resourceKey;
	}

	@Override
	public boolean is(@NotNull Predicate<ResourceKey<R>> predicate) {
		return predicate.test(key);
	}

	@Override
	public boolean is(@NotNull TagKey<R> tagKey) {
		return resolve().is(tagKey);
	}

	@Override
	@Deprecated
	public boolean is(@NotNull Holder<R> holder) {
		return resolve().is(holder);
	}

	@Override
	@NotNull
	public Stream<TagKey<R>> tags() {
		return resolve().tags();
	}

	@Override
	@NotNull
	public Either<ResourceKey<R>, R> unwrap() {
		return Either.left(key);
	}

	@Override
	@NotNull
	public Optional<ResourceKey<R>> unwrapKey() {
		return Optional.of(key);
	}

	@Override
	@NotNull
	public Kind kind() {
		return Kind.REFERENCE;
	}

	@Override
	public boolean canSerializeIn(@NotNull HolderOwner<R> owner) {
		return resolve().canSerializeIn(owner);
	}
}
