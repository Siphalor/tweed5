package de.siphalor.tweed5.serde.extension.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.serde.extension.api.read.result.TweedReadResult;
import de.siphalor.tweed5.serde_api.api.TweedDataReader;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface TweedEntryReader<T extends @Nullable Object, C extends ConfigEntry<T>> {
	TweedReadResult<T> read(TweedDataReader reader, C entry, TweedReadContext context);
}
