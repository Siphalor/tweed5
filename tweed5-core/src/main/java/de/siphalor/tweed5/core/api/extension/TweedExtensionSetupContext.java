package de.siphalor.tweed5.core.api.extension;

import de.siphalor.tweed5.core.api.container.ConfigContainer;

public interface TweedExtensionSetupContext {
	ConfigContainer<?> configContainer();
	<E> RegisteredExtensionData<EntryExtensionsData, E> registerEntryExtensionData(Class<E> dataClass);
	void registerExtension(TweedExtension extension);
}
