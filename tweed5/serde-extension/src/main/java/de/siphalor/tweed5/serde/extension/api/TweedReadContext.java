package de.siphalor.tweed5.serde.extension.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.SubEntryKey;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.serde.extension.api.read.result.TweedReadResult;
import de.siphalor.tweed5.serde_api.api.TweedDataReader;
import org.jspecify.annotations.Nullable;

public interface TweedReadContext {
	ReadWriteExtension readWriteExtension();
	Patchwork extensionsData();

	<T extends @Nullable Object, C extends ConfigEntry<T>> TweedReadResult<T> readSubEntry(
			TweedDataReader reader, C entry, SubEntryKey key
	);
}
