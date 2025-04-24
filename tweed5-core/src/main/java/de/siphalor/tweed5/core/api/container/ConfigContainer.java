package de.siphalor.tweed5.core.api.container;

import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * The main wrapper for a config tree.<br />
 * Holds certain global metadata like registered extensions and manages the initialization phases.
 * @param <T> The class that the config tree represents
 * @see ConfigContainerSetupPhase
 */
public interface ConfigContainer<T> {
	@SuppressWarnings("rawtypes")
	TweedConstructFactory<ConfigContainer> FACTORY = TweedConstructFactory.builder(ConfigContainer.class).build();

	default void registerExtensions(TweedExtension... extensions) {
		for (TweedExtension extension : extensions) {
			registerExtension(extension);
		}
	}

	void registerExtension(TweedExtension extension);

	@Nullable
	<E extends TweedExtension> E extension(Class<E> extensionClass);
	Collection<TweedExtension> extensions();

	void finishExtensionSetup();

	void attachAndSealTree(ConfigEntry<T> rootEntry);

	EntryExtensionsData createExtensionsData();

	void initialize();

	ConfigEntry<T> rootEntry();
	Map<Class<?>, ? extends RegisteredExtensionData<EntryExtensionsData, ?>> entryDataExtensions();
}
