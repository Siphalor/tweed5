package de.siphalor.tweed5.weaver.pojoext.presets.api;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
@RequiredArgsConstructor
public class DefaultPresetWeavingProcessor<T> implements TweedPojoWeavingExtension {
	private final ConfigContainer<T> configContainer;

	@Override
	public void setup(SetupContext context) {
	}

	@Override
	public void afterWeave() {
		PresetsExtension presetsExtension = configContainer.extension(PresetsExtension.class)
				.orElseThrow(() -> new IllegalStateException(
						"Can only use " + DefaultPresetWeavingProcessor.class.getSimpleName()
								+ " if " + PresetsExtension.class.getSimpleName() + " is registered"
				));

		if (presetsExtension.presetValue(configContainer.rootEntry(), PresetsExtension.DEFAULT_PRESET_NAME) != null) {
			log.debug("Default preset already registered, skipping auto instantiation");
			return;
		}

		T defaultValue = instantiateEntry(configContainer.rootEntry());

		presetsExtension.presetValue(configContainer.rootEntry(), PresetsExtension.DEFAULT_PRESET_NAME, defaultValue);
	}

	private T instantiateEntry(ConfigEntry<T> entry) {
		if (entry instanceof CompoundConfigEntry) {
			return ((CompoundConfigEntry<T>) entry).instantiateCompoundValue();
		} else {
			throw new IllegalArgumentException(
					"Can only determine default preset from instantiation for POJOs. "
							+ "Only apply " + getClass().getSimpleName() + " to POJOs."
			);
		}
	}
}
