package moze_intel.projecte.compat.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric port shim: minimal reimplementation of the slice of NeoForge's ModConfigSpec that ProjectE uses, backed by night-config TOML (the same library and file
 * format NeoForge uses, so config files written by the upstream NeoForge version load unchanged).
 * <p>
 * Unlike NeoForge there is no config event lifecycle: {@link #setupFile(Path)} loads (and self-heals) the file eagerly, and {@link #save()} writes it back.
 */
public class ModConfigSpec {

	private static final Logger LOGGER = LoggerFactory.getLogger("ProjectE config");

	private final List<ValueSpec<?>> values;

	@Nullable
	private CommentedFileConfig data;

	private ModConfigSpec(List<ValueSpec<?>> values) {
		this.values = values;
	}

	public boolean isLoaded() {
		return data != null;
	}

	/**
	 * Loads (creating/correcting as needed) the backing TOML file. Replaces NeoForge's ModContainer#registerConfig lifecycle.
	 */
	public void setupFile(Path path) {
		CommentedFileConfig config = CommentedFileConfig.builder(path)
				.sync()
				.preserveInsertionOrder()
				.onFileNotFound((file, format) -> {
					java.nio.file.Files.createDirectories(file.getParent());
					java.nio.file.Files.createFile(file);
					format.initEmptyFile(file);
					return false;
				})
				.build();
		try {
			config.load();
		} catch (Exception e) {
			LOGGER.error("Failed to load config file {}, using defaults", path, e);
		}
		for (ValueSpec<?> value : values) {
			value.correct(config);
		}
		config.save();
		this.data = config;
	}

	public void save() {
		if (data != null) {
			for (ValueSpec<?> value : values) {
				value.writeCurrent(data);
			}
			data.save();
		}
	}

	@Nullable
	CommentedFileConfig data() {
		return data;
	}

	public static class Builder {

		private final List<ValueSpec<?>> values = new ArrayList<>();
		private final Deque<String> currentPath = new ArrayDeque<>();
		@Nullable
		private String pendingComment;

		public Builder comment(String... comment) {
			this.pendingComment = String.join("\n", comment);
			return this;
		}

		public Builder translation(String translationKey) {
			//Only used by config GUIs, which this port does not provide; accepted for source compatibility.
			return this;
		}

		public Builder worldRestart() {
			//Marker only; the values ProjectE flags with this are read once per world load anyway.
			return this;
		}

		public Builder push(String path) {
			currentPath.addLast(path);
			return this;
		}

		public Builder pop() {
			currentPath.removeLast();
			return this;
		}

		public ConfigValue<Boolean> define(String key, boolean defaultValue) {
			return add(new ValueSpec<>(path(key), defaultValue, pendingComment(), Boolean.class, null));
		}

		public ConfigValue<String> define(String key, String defaultValue) {
			return add(new ValueSpec<>(path(key), defaultValue, pendingComment(), String.class, null));
		}

		public ConfigValue<Integer> defineInRange(String key, int defaultValue, int min, int max) {
			return add(new ValueSpec<>(path(key), defaultValue, rangeComment(min, max), Integer.class, new Range<>(min, max)));
		}

		public ConfigValue<Long> defineInRange(String key, long defaultValue, long min, long max) {
			return add(new ValueSpec<>(path(key), defaultValue, rangeComment(min, max), Long.class, new Range<>(min, max)));
		}

		public ConfigValue<Double> defineInRange(String key, double defaultValue, double min, double max) {
			return add(new ValueSpec<>(path(key), defaultValue, rangeComment(min, max), Double.class, new Range<>(min, max)));
		}

		public ModConfigSpec build() {
			if (!currentPath.isEmpty()) {
				throw new IllegalStateException("Dangling config path: " + String.join(".", currentPath));
			}
			ModConfigSpec spec = new ModConfigSpec(List.copyOf(values));
			for (ValueSpec<?> value : values) {
				value.spec = spec;
			}
			return spec;
		}

		private <T> ConfigValue<T> add(ValueSpec<T> spec) {
			values.add(spec);
			return new ConfigValue<>(spec);
		}

		private String path(String key) {
			if (currentPath.isEmpty()) {
				return key;
			}
			return String.join(".", currentPath) + "." + key;
		}

		@Nullable
		private String pendingComment() {
			String comment = pendingComment;
			pendingComment = null;
			return comment;
		}

		@Nullable
		private <T extends Number> String rangeComment(T min, T max) {
			String base = pendingComment();
			String range = "Range: " + min + " ~ " + max;
			return base == null ? range : base + "\n" + range;
		}
	}

	public static class ConfigValue<T> implements Supplier<T> {

		private final ValueSpec<T> valueSpec;

		private ConfigValue(ValueSpec<T> valueSpec) {
			this.valueSpec = valueSpec;
		}

		@Override
		public T get() {
			return valueSpec.get();
		}

		public T getDefault() {
			return valueSpec.defaultValue;
		}

		public void set(T value) {
			valueSpec.set(value);
		}
	}

	private record Range<T extends Number>(T min, T max) {

		@SuppressWarnings("unchecked")
		T clamp(Number value) {
			double val = value.doubleValue();
			if (val < min.doubleValue()) {
				return min;
			}
			if (val > max.doubleValue()) {
				return max;
			}
			//Convert back to the range's numeric type
			if (min instanceof Integer) {
				return (T) Integer.valueOf(value.intValue());
			} else if (min instanceof Long) {
				return (T) Long.valueOf(value.longValue());
			}
			return (T) Double.valueOf(val);
		}
	}

	private static class ValueSpec<T> {

		private final String path;
		private final T defaultValue;
		@Nullable
		private final String comment;
		private final Class<T> type;
		@Nullable
		private final Range<?> range;

		private ModConfigSpec spec;
		@Nullable
		private T currentValue;

		ValueSpec(String path, T defaultValue, @Nullable String comment, Class<T> type, @Nullable Range<?> range) {
			this.path = path;
			this.defaultValue = defaultValue;
			this.comment = comment;
			this.type = type;
			this.range = range;
		}

		void correct(CommentedFileConfig config) {
			Object raw = config.get(path);
			T value = coerce(raw);
			if (value == null) {
				if (raw != null) {
					LOGGER.warn("Correcting invalid config value {} for {} back to default {}", raw, path, defaultValue);
				}
				value = defaultValue;
			}
			currentValue = value;
			config.set(path, value);
			if (comment != null) {
				config.setComment(path, comment);
			}
		}

		void writeCurrent(CommentedFileConfig config) {
			if (currentValue != null) {
				config.set(path, currentValue);
			}
		}

		T get() {
			return currentValue == null ? defaultValue : currentValue;
		}

		void set(T value) {
			currentValue = value;
			if (spec != null && spec.data() != null) {
				spec.data().set(path, value);
			}
		}

		@Nullable
		@SuppressWarnings("unchecked")
		private T coerce(@Nullable Object raw) {
			if (raw == null) {
				return null;
			}
			if (type == Boolean.class) {
				return raw instanceof Boolean ? (T) raw : null;
			}
			if (type == String.class) {
				return raw instanceof String ? (T) raw : null;
			}
			if (raw instanceof Number number) {
				Number clamped = range == null ? number : range.clamp(number);
				if (clamped != number && !clamped.equals(number)) {
					LOGGER.warn("Config value {} for {} is out of range, clamping to {}", number, path, clamped);
				}
				if (type == Integer.class) {
					return (T) Integer.valueOf(clamped.intValue());
				} else if (type == Long.class) {
					return (T) Long.valueOf(clamped.longValue());
				} else if (type == Double.class) {
					return (T) Double.valueOf(clamped.doubleValue());
				}
			}
			return null;
		}
	}
}
