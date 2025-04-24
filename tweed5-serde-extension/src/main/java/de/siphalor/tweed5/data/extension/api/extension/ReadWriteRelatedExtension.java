package de.siphalor.tweed5.data.extension.api.extension;

import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import org.jspecify.annotations.Nullable;

public interface ReadWriteRelatedExtension {
	default void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {

	}

	@Nullable
	default Middleware<TweedEntryReader<?, ?>> entryReaderMiddleware() {
		return null;
	}

	@Nullable
	default Middleware<TweedEntryWriter<?, ?>> entryWriterMiddleware() {
		return null;
	}
}
