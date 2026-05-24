package de.siphalor.tweed5.serde.extension.impl;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.SubEntryKey;
import de.siphalor.tweed5.serde.extension.api.*;
import de.siphalor.tweed5.serde.extension.api.path.EntryPath;
import de.siphalor.tweed5.serde.extension.api.read.result.TweedReadResult;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.serde.extension.impl.path.ReadWritePathTracking;
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

	private final ReadWritePathTracking entryPathTracking = new ReadWritePathTracking();
	private final ReadWritePathTracking valuePathTracking = new ReadWritePathTracking();

	@Override
	public EntryPath currentEntryPath() {
		return new EntryPath(entryPathTracking.currentPathParts(), entryPathTracking.currentPath());
	}

	@Override
	public EntryPath currentValuePath() {
		return new EntryPath(valuePathTracking.currentPathParts(), valuePathTracking.currentPath());
	}

	@Override
	public <T extends @Nullable Object, C extends ConfigEntry<T>> TweedReadResult<T> readSubEntry(
			TweedDataReader reader, C entry, SubEntryKey key
	) {
		try {
			entryPathTracking.push(key.entry());
			valuePathTracking.push(key.value());
			return readWriteExtension.getReaderChain(entry).read(reader, entry, this);
		} finally {
			entryPathTracking.pop();
			valuePathTracking.pop();
		}
	}

	@Override
	public <T extends @Nullable Object, C extends ConfigEntry<T>> void writeSubEntry(
			TweedDataVisitor writer, C entry, SubEntryKey key, @Nullable T value
	) throws TweedEntryWriteException, TweedDataWriteException {
		try {
			entryPathTracking.push(key.entry());
			valuePathTracking.push(key.value());
			readWriteExtension.getWriterChain(entry).write(writer, value, entry, this);
		} finally {
			entryPathTracking.pop();
			valuePathTracking.pop();
		}
	}
}
