package de.siphalor.tweed5.core.api.container;

import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * The main wrapper for a config tree.<br />
 * Holds certain global metadata like registered extensions and manages the initialization phases.
 * @param <T> The class that the config tree represents
 * @see ConfigContainerSetupPhase
 */
public interface ConfigContainer<T extends @Nullable Object> {
	@SuppressWarnings("rawtypes")
	TweedConstructFactory<ConfigContainer> FACTORY = TweedConstructFactory.builder(ConfigContainer.class).build();

	ConfigContainerSetupPhase setupPhase();

	default void registerExtensions(Class<? extends TweedExtension>... extensionClasses) {
		for (Class<? extends TweedExtension> extensionClass : extensionClasses) {
			registerExtension(extensionClass);
		}
	}
	void registerExtension(Class<? extends TweedExtension> extensionClass);

	<E extends TweedExtension> Optional<E> extension(Class<E> extensionClass);
	Collection<TweedExtension> extensions();

	void finishExtensionSetup();

	void attachTree(ConfigEntry<T> rootEntry);

	Patchwork createExtensionsData();

	void initialize();

	ConfigEntry<T> rootEntry();
}
