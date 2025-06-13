package de.siphalor.tweed5.data.extension.api.extension;

import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;

public interface ReadWriteExtensionSetupContext {
	<E> PatchworkPartAccess<E> registerReadWriteContextExtensionData(Class<E> extensionDataClass);
}
