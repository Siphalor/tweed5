package de.siphalor.tweed5.data.extension.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.dataapi.api.TweedDataWriteException;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface TweedEntryWriter<T extends @Nullable Object, C extends ConfigEntry<T>> {
	void write(TweedDataVisitor writer, @Nullable T value, C entry, TweedWriteContext context) throws TweedEntryWriteException, TweedDataWriteException;
}
