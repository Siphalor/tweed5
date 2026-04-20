package de.siphalor.tweed5.serde.extension.impl;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.SubEntryKey;
import de.siphalor.tweed5.serde.extension.api.*;
import de.siphalor.tweed5.serde.extension.api.read.result.TweedReadResult;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.serde_api.api.TweedDataReader;
import de.siphalor.tweed5.serde_api.api.TweedDataVisitor;
import de.siphalor.tweed5.serde_api.api.TweedDataWriteException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
class TweedReadWriteContextImpl implements TweedReadContext, TweedWriteContext {
	@Getter
	private final ReadWriteExtensionImpl readWriteExtension;
	@Getter
	private final Patchwork extensionsData;

	@Override
	public <T extends @Nullable Object, C extends ConfigEntry<T>> TweedReadResult<T> readSubEntry(
			TweedDataReader reader, C entry, SubEntryKey key
	) {
		return readWriteExtension.getReaderChain(entry).read(reader, entry, this);
	}

	@Override
	public <T extends @Nullable Object, C extends ConfigEntry<T>> void writeSubEntry(
			TweedDataVisitor writer, C entry, SubEntryKey key, @Nullable T value
	) throws TweedEntryWriteException, TweedDataWriteException {
		readWriteExtension.getWriterChain(entry).write(writer, value, entry, this);
	}
}
