package de.siphalor.tweed5.defaultextensions.readfallback.impl;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.defaultextensions.readfallback.api.ReadFallbackExtension;
import de.siphalor.tweed5.data.extension.api.TweedEntryReadException;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import lombok.extern.apachecommons.CommonsLog;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

@CommonsLog
public class ReadFallbackExtensionImpl implements ReadFallbackExtension, ReadWriteRelatedExtension {
	private final ConfigContainer<?> configContainer;
	private @Nullable PresetsExtension presetsExtension;

	public ReadFallbackExtensionImpl(ConfigContainer<?> configContainer) {
		this.configContainer = configContainer;
	}

	@Override
	public void extensionsFinalized() {
		presetsExtension = configContainer.extension(PresetsExtension.class)
				.orElseThrow(() -> new IllegalStateException(getClass().getSimpleName()
						+ " requires " + ReadFallbackExtension.class.getSimpleName()));
	}

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		assert presetsExtension != null;

		context.registerReaderMiddleware(new Middleware<TweedEntryReader<?, ?>>() {
			@Override
			public String id() {
				return EXTENSION_ID;
			}

			@Override
			public Set<String> mustComeBefore() {
				return Collections.singleton(DEFAULT_START);
			}

			@Override
			public Set<String> mustComeAfter() {
				return Collections.emptySet();
			}

			@Override
			public TweedEntryReader<?, ?> process(TweedEntryReader<?, ?> inner) {
				//noinspection unchecked
				TweedEntryReader<Object, ConfigEntry<Object>> castedInner =
						(TweedEntryReader<Object, @NonNull ConfigEntry<Object>>) inner;
				return (TweedEntryReader<Object, @NonNull ConfigEntry<Object>>) (reader, entry, context1) -> {
					try {
						return castedInner.read(reader, entry, context1);
					} catch (TweedEntryReadException e) {
						log.error("Failed to read entry: " + e.getMessage(), e);
						return presetsExtension.presetValue(entry, PresetsExtension.DEFAULT_PRESET_NAME);
					}
				};
			}
		});
	}
}
