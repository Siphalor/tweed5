package de.siphalor.tweed5.serde.extension.api.readwrite;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.serde.extension.api.TweedEntryReader;
import de.siphalor.tweed5.serde.extension.api.TweedEntryWriter;
import org.jspecify.annotations.Nullable;

public interface TweedEntryReaderWriter<T extends @Nullable Object, C extends ConfigEntry<T>> extends TweedEntryReader<T, C>, TweedEntryWriter<T, C> {}
