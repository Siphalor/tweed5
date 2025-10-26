package de.siphalor.tweed5.core.api.extension;

import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;

public interface TweedExtensionSetupContext {
	<E> PatchworkPartAccess<E> registerEntryExtensionData(Class<E> dataClass);
	void registerExtension(Class<? extends TweedExtension> extensionClass);
}
