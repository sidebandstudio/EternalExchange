package moze_intel.projecte.gameObjs.registration;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port: reimplementation of upstream's DeferredHolder-based wrapper. The value and vanilla holder are bound when the owning
 * {@link PEDeferredRegister#register()} runs during mod init.
 */
public class PEDeferredHolder<R, T extends R> implements Holder<R>, Supplier<T>, INamedEntry {

	private final ResourceKey<R> key;
	@Nullable
	private T value;
	@Nullable
	private Holder<R> holder;

	public PEDeferredHolder(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation valueName) {
		this(ResourceKey.create(registryKey, valueName));
	}

	public PEDeferredHolder(ResourceKey<R> key) {
		this.key = key;
	}

	/**
	 * Called by {@link PEDeferredRegister} once the backing object has been registered.
	 */
	void bind(T value, Holder<R> holder) {
		this.value = value;
		this.holder = holder;
	}

	protected Holder<R> holderOrThrow() {
		if (holder == null) {
			throw new IllegalStateException("Registry object not present: " + key.location());
		}
		return holder;
	}

	@Override
	public T get() {
		if (value == null) {
			throw new IllegalStateException("Registry object not present: " + key.location());
		}
		return value;
	}

	public ResourceLocation getId() {
		return key.location();
	}

	public ResourceKey<R> getKey() {
		return key;
	}

	@Override
	public String getName() {
		return getId().getPath();
	}

	@Override
	@NotNull
	public R value() {
		return get();
	}

	@Override
	public boolean isBound() {
		return value != null;
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
		return holderOrThrow().is(tagKey);
	}

	@Override
	@Deprecated
	public boolean is(@NotNull Holder<R> other) {
		return holderOrThrow().is(other);
	}

	@Override
	@NotNull
	public Stream<TagKey<R>> tags() {
		return holderOrThrow().tags();
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
		return holderOrThrow().canSerializeIn(owner);
	}
}
