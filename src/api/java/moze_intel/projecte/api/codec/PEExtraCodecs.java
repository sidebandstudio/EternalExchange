package moze_intel.projecte.api.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.Function;
import moze_intel.projecte.api.conditions.WithConditions;

/**
 * Fabric port shim: replacements for the NeoForgeExtraCodecs / ConditionalOps helpers upstream ProjectE uses.
 */
public final class PEExtraCodecs {

	private PEExtraCodecs() {
	}

	/**
	 * Codec that decodes with {@code primary}, falling back to {@code alternative} on failure. Always encodes with {@code primary}.
	 */
	public static <T> Codec<T> withAlternative(Codec<T> primary, Codec<? extends T> alternative) {
		return Codec.withAlternative(primary, alternative);
	}

	/**
	 * MapCodec that decodes with {@code primary}, falling back to {@code alternative} on failure. Always encodes with {@code primary}.
	 */
	public static <T> MapCodec<T> withAlternative(MapCodec<T> primary, MapCodec<? extends T> alternative) {
		return Codec.mapEither(primary, alternative).xmap(
				either -> either.map(Function.identity(), Function.identity()),
				Either::left
		);
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
