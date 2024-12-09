package de.siphalor.tweed5.data.extension.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteContextExtensionsData;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;

public interface ReadWriteExtension extends TweedExtension {
	void setEntryReaderWriterDefinition(ConfigEntry<?> entry, EntryReaderWriterDefinition readerWriterDefinition);

	ReadWriteContextExtensionsData createReadWriteContextExtensionsData();

	<T> T read(TweedDataReader reader, ConfigEntry<T> entry, ReadWriteContextExtensionsData contextExtensionsData) throws TweedEntryReadException;

	<T> void write(TweedDataVisitor writer, T value, ConfigEntry<T> entry, ReadWriteContextExtensionsData contextExtensionsData) throws TweedEntryWriteException;
}
