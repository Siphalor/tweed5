package de.siphalor.tweed5.serde.extension.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.serde_api.api.TweedDataVisitor;
import de.siphalor.tweed5.serde_api.api.TweedDataWriteException;
import org.jspecify.annotations.Nullable;

public interface TweedWriteContext {
	Patchwork extensionsData();

	<T extends @Nullable Object, C extends ConfigEntry<T>> void writeSubEntry(
			TweedDataVisitor writer, @Nullable T value, C entry
	) throws TweedEntryWriteException, TweedDataWriteException;
}
