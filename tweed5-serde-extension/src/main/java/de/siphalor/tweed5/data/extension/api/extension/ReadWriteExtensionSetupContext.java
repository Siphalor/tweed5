package de.siphalor.tweed5.data.extension.api.extension;

import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;

public interface ReadWriteExtensionSetupContext {
	<E> PatchworkPartAccess<E> registerReadWriteContextExtensionData(Class<E> extensionDataClass);
	void registerReaderMiddleware(Middleware<TweedEntryReader<?, ?>> middleware);
	void registerWriterMiddleware(Middleware<TweedEntryWriter<?, ?>> middleware);
}
