package moze_intel.projecte.compat.attachments;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port shim: mirrors the slice of NeoForge's AttachmentType builder that ProjectE uses. Persistence and death-copying are driven by
 * {@link PEAttachments} (a Player mixin + Fabric's ServerPlayerEvents.COPY_FROM).
 */
public final class AttachmentType<T> {

	private ResourceLocation id;
	private final Function<IAttachmentHolder, T> defaultCreator;
	@Nullable
	private final Codec<T> codec;
	@Nullable
	private final Predicate<T> shouldSerialize;
	@Nullable
	private final CopyHandler<T> copyHandler;
	private final boolean copyOnDeath;

	private AttachmentType(Builder<T> builder) {
		this.defaultCreator = builder.defaultCreator;
		this.codec = builder.codec;
		this.shouldSerialize = builder.shouldSerialize;
		this.copyHandler = builder.copyHandler;
		this.copyOnDeath = builder.copyOnDeath;
	}

	void bindId(ResourceLocation id) {
		this.id = id;
	}

	public ResourceLocation id() {
		return id;
	}

	public T createDefault(IAttachmentHolder holder) {
		return defaultCreator.apply(holder);
	}

	@Nullable
	public Codec<T> codec() {
		return codec;
	}

	public boolean shouldSerialize(T value) {
		return codec != null && (shouldSerialize == null || shouldSerialize.test(value));
	}

	public boolean copyOnDeath() {
		return copyOnDeath;
	}

	@Nullable
	public T copyValue(T value, IAttachmentHolder newHolder, HolderLookup.Provider registries) {
		if (copyHandler != null) {
			return copyHandler.copy(value, newHolder, registries);
		}
		return value;
	}

	public static <T> Builder<T> builder(Function<IAttachmentHolder, T> defaultCreator) {
		return new Builder<>(defaultCreator);
	}

	@FunctionalInterface
	public interface CopyHandler<T> {

		@Nullable
		T copy(T attachment, IAttachmentHolder holder, HolderLookup.Provider registries);
	}

	public static final class Builder<T> {

		private final Function<IAttachmentHolder, T> defaultCreator;
		@Nullable
		private Codec<T> codec;
		@Nullable
		private Predicate<T> shouldSerialize;
		@Nullable
		private CopyHandler<T> copyHandler;
		private boolean copyOnDeath;

		private Builder(Function<IAttachmentHolder, T> defaultCreator) {
			this.defaultCreator = defaultCreator;
		}

		public Builder<T> serialize(Codec<T> codec) {
			this.codec = codec;
			return this;
		}

		public Builder<T> serialize(Codec<T> codec, Predicate<T> shouldSerialize) {
			this.codec = codec;
			this.shouldSerialize = shouldSerialize;
			return this;
		}

		public Builder<T> copyHandler(CopyHandler<T> copyHandler) {
			this.copyHandler = copyHandler;
			return this;
		}

		public Builder<T> copyOnDeath() {
			this.copyOnDeath = true;
			return this;
		}

		public AttachmentType<T> build() {
			return new AttachmentType<>(this);
		}
	}
}
