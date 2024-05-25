package de.siphalor.tweed5.data.extension.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;

@FunctionalInterface
public interface TweedEntryReader<T, C extends ConfigEntry<T>> {
	T read(TweedDataReader reader, C entry, TweedReadContext context) throws TweedEntryReadException, TweedDataReadException;
}
