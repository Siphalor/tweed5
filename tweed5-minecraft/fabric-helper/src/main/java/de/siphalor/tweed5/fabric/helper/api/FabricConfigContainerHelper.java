package de.siphalor.tweed5.fabric.helper.api;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.container.ConfigContainerSetupPhase;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataWriter;
import de.siphalor.tweed5.dataapi.api.TweedSerde;
import de.siphalor.tweed5.defaultextensions.patch.api.PatchExtension;
import de.siphalor.tweed5.defaultextensions.patch.api.PatchInfo;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

@CommonsLog
public class FabricConfigContainerHelper<T extends @Nullable Object> {
	@Getter
	private final ConfigContainer<T> configContainer;
	private final ReadWriteExtension readWriteExtension;
	private final @Nullable PatchExtension patchExtension;
	private final @Nullable PresetsExtension presetsExtension;
	private final TweedSerde serde;
	@Getter
	private final String modId;

	private @Nullable Path tempConfigDirectory;

	public static <T extends @Nullable Object> FabricConfigContainerHelper<T> create(
			ConfigContainer<T> configContainer,
			TweedSerde serde,
			String modId
	) {
		if (configContainer.setupPhase() != ConfigContainerSetupPhase.INITIALIZED) {
			throw new IllegalStateException(
					"Config container must be fully initialized before creating helper. "
							+ "Usually you're just missing a call to initialize()"
			);
		}
		return new FabricConfigContainerHelper<>(configContainer, serde, modId);
	}

	private FabricConfigContainerHelper(ConfigContainer<T> configContainer, TweedSerde serde, String modId) {
		this.configContainer = configContainer;
		this.readWriteExtension = configContainer.extension(ReadWriteExtension.class)
				.orElseThrow(() -> new IllegalStateException("ReadWriteExtension not declared in config container"));
		this.patchExtension = configContainer.extension(PatchExtension.class).orElse(null);
		this.presetsExtension = configContainer.extension(PresetsExtension.class).orElse(null);
		this.serde = serde;
		this.modId = modId;
	}

	public T loadAndUpdateInConfigDirectory() {
		T defaultPresetValue = getDefaultPresetValue();
		return loadAndUpdateInConfigDirectory(() -> configContainer.rootEntry().deepCopy(defaultPresetValue));
	}

	public T loadAndUpdateInConfigDirectory(Supplier<T> defaultValueSupplier) {
		T configValue = readConfigInConfigDirectory(defaultValueSupplier);
		writeConfigInConfigDirectory(configValue);
		return configValue;
	}

	public void readPartialConfigInConfigDirectory(T value, Consumer<Patchwork> readContextCustomizer) {
		if (patchExtension == null) {
			throw new IllegalStateException(
					"PatchExtension must be declared in config container for partially loading config"
			);
		}

		File configFile = getConfigFile();
		if (!configFile.exists()) {
			return;
		}

		try (TweedDataReader reader = serde.createReader(new FileInputStream(configFile))) {
			Patchwork contextExtensionsData = readWriteExtension.createReadWriteContextExtensionsData();
			readContextCustomizer.accept(contextExtensionsData);
			PatchInfo patchInfo = patchExtension.collectPatchInfo(contextExtensionsData);

			T readValue = readWriteExtension.read(reader, configContainer().rootEntry(), contextExtensionsData);

			patchExtension.patch(configContainer.rootEntry(), value, readValue, patchInfo);
		} catch (Exception e) {
			log.error("Failed loading config file " + configFile.getAbsolutePath(), e);
		}
	}

	public T readConfigInConfigDirectory() {
		T defaultPresetValue = getDefaultPresetValue();
		return readConfigInConfigDirectory(() -> configContainer.rootEntry().deepCopy(defaultPresetValue));
	}

	public T readConfigInConfigDirectory(Supplier<T> defaultValueSupplier) {
		File configFile = getConfigFile();
		if (!configFile.exists()) {
			return defaultValueSupplier.get();
		}

		try (TweedDataReader reader = serde.createReader(new FileInputStream(configFile))) {
			Patchwork contextExtensionsData = readWriteExtension.createReadWriteContextExtensionsData();
			return readWriteExtension.read(reader, configContainer.rootEntry(), contextExtensionsData);
		} catch (Exception e) {
			log.error("Failed loading config file " + configFile.getAbsolutePath(), e);
			return defaultValueSupplier.get();
		}
	}

	public void writeConfigInConfigDirectory(T configValue) {
		File configFile = getConfigFile();
		Path tempConfigDirectory = getOrCreateTempConfigDirectory();
		File tempConfigFile = tempConfigDirectory.resolve(getConfigFileName()).toFile();
		try (TweedDataWriter writer = serde.createWriter(new FileOutputStream(tempConfigFile))) {
			Patchwork contextExtensionsData = readWriteExtension.createReadWriteContextExtensionsData();

			readWriteExtension.write(
					writer,
					configValue,
					configContainer.rootEntry(),
					contextExtensionsData
			);
		} catch (Exception e) {
			log.error("Failed to write config file " + tempConfigFile.getAbsolutePath(), e);
			return;
		}

		try {
			if (configFile.exists()) {
				if (!configFile.delete()) {
					throw new IOException("Failed to overwrite old config file " + configFile.getAbsolutePath());
				}
			}
			Files.move(tempConfigFile.toPath(), configFile.toPath());
		} catch (IOException e) {
			log.error("Failed to move temporary config file " + tempConfigFile.getAbsolutePath() + " to " + configFile.getAbsolutePath(), e);
		}
	}

	private File getConfigFile() {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		configDir.toFile().mkdirs();
		return configDir.resolve(getConfigFileName()).toFile();
	}

	private String getConfigFileName() {
		return modId + serde.getPreferredFileExtension();
	}

	private Path getOrCreateTempConfigDirectory() {
		if (tempConfigDirectory == null) {
			try {
				tempConfigDirectory = Files.createTempDirectory("tweed5-config");
				tempConfigDirectory.toFile().deleteOnExit();
				return tempConfigDirectory;
			} catch (IOException e) {
				log.warn("Failed to create temporary config directory, using game directory instead");
			}
			tempConfigDirectory = FabricLoader.getInstance().getGameDir().resolve(".tweed5-tmp/").resolve(modId);
			tempConfigDirectory.toFile().mkdirs();
		}
		return tempConfigDirectory;
	}

	private T getDefaultPresetValue() {
		if (presetsExtension == null) {
			throw new IllegalStateException(
					"No presets extension registered, either register such extension or provide a default value manually"
			);
		}
		return presetsExtension.presetValue(configContainer.rootEntry(), PresetsExtension.DEFAULT_PRESET_NAME);
	}
}
