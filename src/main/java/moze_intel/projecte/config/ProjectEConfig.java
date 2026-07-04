package moze_intel.projecte.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.compat.config.ModConfigSpec;
import net.fabricmc.loader.api.FabricLoader;

public class ProjectEConfig {

	public static final Path CONFIG_DIR = createConfigDir();
	private static final Map<ModConfigSpec, IPEConfig> KNOWN_CONFIGS = new HashMap<>();

	public static final ServerConfig server = new ServerConfig();
	public static final CommonConfig common = new CommonConfig();
	public static final ClientConfig client = new ClientConfig();

	private static Path createConfigDir() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(PECore.MODNAME);
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to create ProjectE config directory", e);
		}
		return path;
	}

	/**
	 * Fabric port note: configs load eagerly from {@code config/ProjectE/*.toml} at registration; there is no FML config event lifecycle, and SERVER-type configs are
	 * neither stored per-world nor synced to clients.
	 */
	public static void register() {
		registerConfig(server);
		registerConfig(common);
		registerConfig(client);
	}

	public static Collection<IPEConfig> getConfigs() {
		return Collections.unmodifiableCollection(KNOWN_CONFIGS.values());
	}

	/**
	 * Creates and registers a config, tracking it so that we can properly clear cached values.
	 */
	public static void registerConfig(IPEConfig config) {
		config.getConfigSpec().setupFile(CONFIG_DIR.resolve(config.getFileName() + ".toml"));
		KNOWN_CONFIGS.put(config.getConfigSpec(), config);
		config.clearCache(false);
	}
}
