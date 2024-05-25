package de.siphalor.tweed5.data.extension.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteContextExtensionsData;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataWriter;

public interface ReadWriteExtension extends TweedExtension {
	ReadWriteContextExtensionsData createReadWriteContextExtensionsData();

	<T> T read(TweedDataReader reader, ConfigEntry<T> entry, ReadWriteContextExtensionsData contextExtensionsData) throws TweedEntryReadException;

	<T> void write(TweedDataWriter writer, T value, ConfigEntry<T> entry, ReadWriteContextExtensionsData contextExtensionsData) throws TweedEntryWriteException;
}
