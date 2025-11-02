package de.siphalor.tweed5.defaultextensions.presets.impl;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class PresetsExtensionImpl implements PresetsExtension {
	private final PatchworkPartAccess<Map<String, Object>> presetsDataAccess;

	public PresetsExtensionImpl(TweedExtensionSetupContext setupContext) {
		//noinspection unchecked
		presetsDataAccess = setupContext.registerEntryExtensionData((Class<Map<String, Object>>)(Class<?>) Map.class);
	}

	@Override
	public <T extends @Nullable Object> void presetValue(ConfigEntry<T> entry, String name, T value) {
		if (value != null && !entry.valueClass().isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException(
					"The preset value is not of the expected type " + entry.valueClass().getName()
			);
		}
		entry.visitInOrder(new ConfigEntryValueVisitor() {
			@Override
			public <U> void visitEntry(ConfigEntry<U> entry, U value) {
				getOrCreatePresetsData(entry.extensionsData()).put(name, value);
			}
		}, value);
	}

	@Override
	public <T> @Nullable T presetValue(ConfigEntry<T> entry, String name) {
		//noinspection unchecked
		return (T) getPresetsData(entry.extensionsData()).get(name);
	}

	private Map<String, Object> getOrCreatePresetsData(Patchwork extensionsData) {
		Map<String, Object> data = extensionsData.get(presetsDataAccess);
		if (data == null) {
			extensionsData.set(presetsDataAccess, data = new HashMap<>());
		}
		return data;
	}

	private Map<String, Object> getPresetsData(Patchwork extensionsData) {
		Map<String, Object> data = extensionsData.get(presetsDataAccess);
		return data != null ? data : Collections.emptyMap();
	}
}
