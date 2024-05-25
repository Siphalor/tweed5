package de.siphalor.tweed5.data.extension.api.extension;

import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;

public interface ReadWriteExtensionSetupContext {
	<E> RegisteredExtensionData<ReadWriteContextExtensionsData, E> registerReadWriteContextExtensionData(Class<E> extensionDataClass);
}
