package de.siphalor.tweed5.core.api.extension;

public interface TweedExtensionSetupContext {
	<E> RegisteredExtensionData<EntryExtensionsData, E> registerEntryExtensionData(Class<E> dataClass);
	void registerExtension(Class<? extends TweedExtension> extensionClass);
}
