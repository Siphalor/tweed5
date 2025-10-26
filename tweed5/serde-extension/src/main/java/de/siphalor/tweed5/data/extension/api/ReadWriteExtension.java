package de.siphalor.tweed5.data.extension.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriter;
import de.siphalor.tweed5.data.extension.impl.ReadWriteExtensionImpl;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ReadWriteExtension extends TweedExtension {
	Class<? extends ReadWriteExtension> DEFAULT = ReadWriteExtensionImpl.class;
	String EXTENSION_ID = "read-write";

	@Override
	default String getId() {
		return EXTENSION_ID;
	}

	static <T> Consumer<ConfigEntry<T>> entryReaderWriter(
			TweedEntryReaderWriter<T, ? extends ConfigEntry<T>> entryReaderWriter
	) {
		return entryReaderWriter(entryReaderWriter, entryReaderWriter);
	}

	static <T> Consumer<ConfigEntry<T>> entryReaderWriter(
			TweedEntryReader<T, ? extends ConfigEntry<T>> entryReader,
			TweedEntryWriter<T, ? extends ConfigEntry<T>> entryWriter
	) {
		return entry -> {
			ReadWriteExtension extension = entry.container().extension(ReadWriteExtension.class)
					.orElseThrow(() -> new IllegalStateException("No ReadWriteExtension present"));
			extension.setEntryReader(entry, entryReader);
			extension.setEntryWriter(entry, entryWriter);
		};
	}

	static <T> Consumer<ConfigEntry<T>> entryReader(TweedEntryReader<T, ? extends ConfigEntry<T>> entryReader) {
		return entry -> {
			ReadWriteExtension extension = entry.container().extension(ReadWriteExtension.class)
					.orElseThrow(() -> new IllegalStateException("No ReadWriteExtension present"));
			extension.setEntryReader(entry, entryReader);
		};
	}

	static <T> Consumer<ConfigEntry<T>> entryWriter(TweedEntryWriter<T, ? extends ConfigEntry<T>> entryWriter) {
		return entry -> {
			ReadWriteExtension extension = entry.container().extension(ReadWriteExtension.class)
					.orElseThrow(() -> new IllegalStateException("No ReadWriteExtension present"));
			extension.setEntryWriter(entry, entryWriter);
		};
	}

	static <T extends @Nullable Object> Function<ConfigEntry<T>, T> read(TweedDataReader reader) {
		return read(reader, null);
	}

	static <T extends @Nullable Object> Function<ConfigEntry<T>, T> read(
			TweedDataReader reader,
			@Nullable Consumer<Patchwork> contextExtensionsDataCustomizer
	) {
		return entry -> {
			try {
				ReadWriteExtension extension = entry.container().extension(ReadWriteExtension.class)
						.orElseThrow(() -> new IllegalStateException("No ReadWriteExtension present"));
				Patchwork contextExtensionsData = extension.createReadWriteContextExtensionsData();
				if (contextExtensionsDataCustomizer != null) {
					contextExtensionsDataCustomizer.accept(contextExtensionsData);
				}
				return extension.read(reader, entry, contextExtensionsData);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T extends @Nullable Object> Consumer<ConfigEntry<T>> write(TweedDataVisitor writer, T value) {
		return write(writer, value, null);
	}

	static <T extends @Nullable Object> Consumer<ConfigEntry<T>> write(
			TweedDataVisitor writer,
			T value,
			@Nullable Consumer<Patchwork> contextExtensionsDataCustomizer
	) {
		return entry -> {
			try {
				ReadWriteExtension extension = entry.container().extension(ReadWriteExtension.class)
						.orElseThrow(() -> new IllegalStateException("No ReadWriteExtension present"));
				Patchwork contextExtensionsData = extension.createReadWriteContextExtensionsData();
				if (contextExtensionsDataCustomizer != null) {
					contextExtensionsDataCustomizer.accept(contextExtensionsData);
				}
				extension.write(writer, value, entry, contextExtensionsData);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	<T, C extends ConfigEntry<T>> @Nullable TweedEntryReader<T, C> getDefinedEntryReader(ConfigEntry<T> entry);
	<T, C extends ConfigEntry<T>> @Nullable TweedEntryWriter<T, C> getDefinedEntryWriter(ConfigEntry<T> entry);

	<T, C extends ConfigEntry<T>> void setEntryReaderWriter(
			ConfigEntry<T> entry,
			TweedEntryReader<T, C> entryReader,
			TweedEntryWriter<T, C> entryWriter
	);
	<T, C extends ConfigEntry<T>> void setEntryReader(ConfigEntry<T> entry, TweedEntryReader<T, C> entryReader);
	<T, C extends ConfigEntry<T>> void setEntryWriter(ConfigEntry<T> entry, TweedEntryWriter<T, C> entryWriter);

	Patchwork createReadWriteContextExtensionsData();

	<T extends @Nullable Object> T read(TweedDataReader reader, ConfigEntry<T> entry, Patchwork contextExtensionsData)
			throws TweedEntryReadException;

	<T extends @Nullable Object> void write(
			TweedDataVisitor writer,
			T value,
			ConfigEntry<T> entry,
			Patchwork contextExtensionsData
	) throws TweedEntryWriteException;

	<T, C extends ConfigEntry<T>> TweedEntryReader<T, C> getReaderChain(C entry);
	<T, C extends ConfigEntry<T>> TweedEntryWriter<T, C> getWriterChain(C entry);
}
