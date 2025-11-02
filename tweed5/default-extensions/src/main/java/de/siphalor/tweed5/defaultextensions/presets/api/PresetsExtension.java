package de.siphalor.tweed5.defaultextensions.presets.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.defaultextensions.presets.impl.PresetsExtensionImpl;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface PresetsExtension extends TweedExtension {
	Class<? extends PresetsExtension> DEFAULT = PresetsExtensionImpl.class;
	String EXTENSION_ID = "presets";

	String DEFAULT_PRESET_NAME = "default";

	@Override
	default String getId() {
		return EXTENSION_ID;
	}

	static <C extends ConfigEntry<T>, T> Consumer<C> presetValue(String name, T value) {
		return entry -> {
			PresetsExtension extension = entry.container().extension(PresetsExtension.class)
					.orElseThrow(() -> new IllegalStateException("No presets extension registered"));
			extension.presetValue(entry, name, value);
		};
	}

	static <C extends ConfigEntry<T>, T> Function<C, @Nullable T> presetValue(String name) {
		return entry -> {
			PresetsExtension extension = entry.container().extension(PresetsExtension.class)
					.orElseThrow(() -> new IllegalStateException("No presets extension registered"));
			return extension.presetValue(entry, name);
		};
	}

	<T extends @Nullable Object> void presetValue(ConfigEntry<T> entry, String name, T value);

	<T extends @Nullable Object> @Nullable T presetValue(ConfigEntry<T> entry, String name);
}
