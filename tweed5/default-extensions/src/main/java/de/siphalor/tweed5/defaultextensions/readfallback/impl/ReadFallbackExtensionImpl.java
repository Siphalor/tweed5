package de.siphalor.tweed5.defaultextensions.readfallback.impl;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.data.extension.api.TweedEntryReadException;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.defaultextensions.pather.api.PatherExtension;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.defaultextensions.readfallback.api.ReadFallbackExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import lombok.extern.apachecommons.CommonsLog;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Set;

@CommonsLog
public class ReadFallbackExtensionImpl implements ReadFallbackExtension, ReadWriteRelatedExtension {
	private final ConfigContainer<?> configContainer;

	public ReadFallbackExtensionImpl(ConfigContainer<?> configContainer) {
		this.configContainer = configContainer;
	}

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		PresetsExtension presetsExtension = configContainer.extension(PresetsExtension.class)
				.orElseThrow(() -> new IllegalStateException(getClass().getSimpleName()
						+ " requires " + ReadFallbackExtension.class.getSimpleName()));
		PatherExtension patherExtension = configContainer.extension(PatherExtension.class).orElse(null);

		context.registerReaderMiddleware(new Middleware<TweedEntryReader<?, ?>>() {
			@Override
			public String id() {
				return EXTENSION_ID;
			}

			@Override
			public Set<String> mustComeBefore() {
				return Collections.singleton(PatherExtension.EXTENSION_ID);
			}

			@Override
			public Set<String> mustComeAfter() {
				return Collections.singleton(ValidationExtension.EXTENSION_ID);
			}

			@Override
			public TweedEntryReader<?, ?> process(TweedEntryReader<?, ?> inner) {
				//noinspection unchecked
				TweedEntryReader<Object, ConfigEntry<Object>> castedInner =
						(TweedEntryReader<Object, @NonNull ConfigEntry<Object>>) inner;
				return (TweedEntryReader<Object, @NonNull ConfigEntry<Object>>) (reader, entry, context) -> {
					try {
						return castedInner.read(reader, entry, context);
					} catch (TweedEntryReadException e) {
						if (patherExtension == null) {
							log.error("Failed to read entry: " + e.getMessage(), e);
						} else {
							log.error(
									"Failed to read entry: " + e.getMessage()
											+ " at " + patherExtension.getPath(context),
									e
							);
						}
						return presetsExtension.presetValue(entry, PresetsExtension.DEFAULT_PRESET_NAME);
					}
				};
			}
		});
	}
}
