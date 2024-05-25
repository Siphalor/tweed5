package de.siphalor.tweed5.core.api.container;

import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.api.extension.TweedExtension;

import java.util.Collection;
import java.util.Map;

public interface ConfigContainer<T> {
	ConfigContainerSetupPhase setupPhase();
	default boolean isReady() {
		return setupPhase() == ConfigContainerSetupPhase.READY;
	}

	void registerExtension(TweedExtension extension);

	void finishExtensionSetup();

	void attachAndSealTree(ConfigEntry<T> rootEntry);

	EntryExtensionsData createExtensionsData();

	void initialize();

	ConfigEntry<T> rootEntry();

	Collection<TweedExtension> extensions();
	Map<Class<?>, ? extends RegisteredExtensionData<EntryExtensionsData, ?>> entryDataExtensions();
}
