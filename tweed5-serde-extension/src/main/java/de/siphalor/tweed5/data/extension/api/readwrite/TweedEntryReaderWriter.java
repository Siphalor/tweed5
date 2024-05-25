package de.siphalor.tweed5.data.extension.api.readwrite;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;

public interface TweedEntryReaderWriter<T, C extends ConfigEntry<T>> extends TweedEntryReader<T, C>, TweedEntryWriter<T, C> {}
