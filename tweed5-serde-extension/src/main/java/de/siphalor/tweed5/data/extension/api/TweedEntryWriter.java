package de.siphalor.tweed5.data.extension.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.dataapi.api.TweedDataWriteException;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;

@FunctionalInterface
public interface TweedEntryWriter<T, C extends ConfigEntry<T>> {
	void write(TweedDataVisitor writer, T value, C entry, TweedWriteContext context) throws TweedEntryWriteException, TweedDataWriteException;
}
