package de.siphalor.tweed5.data.extension.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface TweedEntryReader<T extends @Nullable Object, C extends ConfigEntry<T>> {
	T read(TweedDataReader reader, C entry, TweedReadContext context) throws TweedEntryReadException;
}
