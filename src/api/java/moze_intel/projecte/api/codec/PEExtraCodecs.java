package moze_intel.projecte.api.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.Optional;
import java.util.stream.Stream;
import moze_intel.projecte.api.conditions.WithConditions;

/**
 * Fabric port shim: replacements for the NeoForgeExtraCodecs / ConditionalOps helpers upstream ProjectE uses.
 * <p>
 * Decode tries {@code primary} then {@code alternative}; encode tries {@code primary} and falls back to {@code alternative} when the primary rejects the value
 * (e.g. the NSS tag codec rejecting non-tag stacks, or the positive-long codec rejecting the "free" sentinel).
 */
public final class PEExtraCodecs {

	private PEExtraCodecs() {
	}

	public static <T> Codec<T> withAlternative(Codec<T> primary, Codec<? extends T> alternative) {
		return new Codec<>() {
			@Override
			public <O> DataResult<com.mojang.datafixers.util.Pair<T, O>> decode(DynamicOps<O> ops, O input) {
				DataResult<com.mojang.datafixers.util.Pair<T, O>> result = primary.decode(ops, input);
				if (result.result().isPresent()) {
					return result;
				}
				return alternative.decode(ops, input).map(pair -> pair.mapFirst(value -> value));
			}

			@Override
			@SuppressWarnings("unchecked")
			public <O> DataResult<O> encode(T input, DynamicOps<O> ops, O prefix) {
				DataResult<O> result = primary.encode(input, ops, prefix);
				if (result.result().isPresent()) {
					return result;
				}
				return ((Codec<T>) alternative).encode(input, ops, prefix);
			}

			@Override
			public String toString() {
				return "WithAlternative[" + primary + ", " + alternative + "]";
			}
		};
	}

	public static <T> MapCodec<T> withAlternative(MapCodec<T> primary, MapCodec<? extends T> alternative) {
		return new MapCodec<>() {
			@Override
			public <O> Stream<O> keys(DynamicOps<O> ops) {
				return Stream.concat(primary.keys(ops), alternative.keys(ops)).distinct();
			}

			@Override
			public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
				DataResult<T> result = primary.decode(ops, input);
				if (result.result().isPresent()) {
					return result;
				}
				return alternative.decode(ops, input).map(value -> value);
			}

			@Override
			@SuppressWarnings("unchecked")
			public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
				//Probe whether the primary can represent this value before committing it to the real builder
				DataResult<O> attempt = primary.encode(input, ops, ops.mapBuilder()).build(ops.emptyMap());
				if (attempt.result().isPresent()) {
					return primary.encode(input, ops, prefix);
				}
				return ((MapCodec<T>) alternative).encode(input, ops, prefix);
			}

			@Override
			public String toString() {
				return "WithAlternative[" + primary + ", " + alternative + "]";
			}
		};
	}

	/**
	 * Fabric port note: upstream wraps data files with NeoForge's conditional codec; this port decodes the payload directly and ignores any {@code neoforge:conditions}
	 * field (record codecs skip unknown keys), so conditional entries always load.
	 */
	public static <T> Codec<Optional<WithConditions<T>>> createConditionalCodecWithConditions(Codec<T> codec) {
		return codec.xmap(
				value -> Optional.of(new WithConditions<>(value)),
				optional -> optional.orElseThrow(() -> new IllegalArgumentException("Cannot encode an absent conditional value")).carrier()
		);
	}
}
