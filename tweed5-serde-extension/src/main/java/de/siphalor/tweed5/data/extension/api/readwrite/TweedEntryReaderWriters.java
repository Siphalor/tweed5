package de.siphalor.tweed5.data.extension.api.readwrite;

import de.siphalor.tweed5.core.api.entry.CollectionConfigEntry;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.data.extension.impl.TweedEntryReaderWriterImpls;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TweedEntryReaderWriters {
	public static TweedEntryReaderWriter<Boolean, ConfigEntry<Boolean>> booleanReaderWriter() {
		return TweedEntryReaderWriterImpls.BOOLEAN_READER_WRITER;
	}

	public static TweedEntryReaderWriter<Byte, ConfigEntry<Byte>> byteReaderWriter() {
		return TweedEntryReaderWriterImpls.BYTE_READER_WRITER;
	}

	public static TweedEntryReaderWriter<Short, ConfigEntry<Short>> shortReaderWriter() {
		return TweedEntryReaderWriterImpls.SHORT_READER_WRITER;
	}

	public static TweedEntryReaderWriter<Integer, ConfigEntry<Integer>> intReaderWriter() {
		return TweedEntryReaderWriterImpls.INT_READER_WRITER;
	}

	public static TweedEntryReaderWriter<Long, ConfigEntry<Long>> longReaderWriter() {
		return TweedEntryReaderWriterImpls.LONG_READER_WRITER;
	}

	public static TweedEntryReaderWriter<Float, ConfigEntry<Float>> floatReaderWriter() {
		return TweedEntryReaderWriterImpls.FLOAT_READER_WRITER;
	}

	public static TweedEntryReaderWriter<Double, ConfigEntry<Double>> doubleReaderWriter() {
		return TweedEntryReaderWriterImpls.DOUBLE_READER_WRITER;
	}

	public static TweedEntryReaderWriter<String, ConfigEntry<String>> stringReaderWriter() {
		return TweedEntryReaderWriterImpls.STRING_READER_WRITER;
	}

	public static <T, C extends ConfigEntry<T>> TweedEntryReader<T, C> nullableReader(TweedEntryReader<T, C> delegate) {
		return new TweedEntryReaderWriterImpls.NullableReader<>(delegate);
	}

	public static <T, C extends ConfigEntry<T>> TweedEntryWriter<T, C> nullableWriter(TweedEntryWriter<T, C> delegate) {
		return new TweedEntryReaderWriterImpls.NullableWriter<>(delegate);
	}

	public static <T, C extends Collection<T>> TweedEntryReaderWriter<C, CollectionConfigEntry<T, C>> collectionReaderWriter() {
		//noinspection unchecked
		return (TweedEntryReaderWriter<C, @NonNull CollectionConfigEntry<T, C>>) (TweedEntryReaderWriter<?, ?>) TweedEntryReaderWriterImpls.COLLECTION_READER_WRITER;
	}

	public static <T> TweedEntryReaderWriter<T, CompoundConfigEntry<T>> compoundReaderWriter() {
		//noinspection unchecked
		return (TweedEntryReaderWriter<T, @NonNull CompoundConfigEntry<T>>) (TweedEntryReaderWriter<?, ?>) TweedEntryReaderWriterImpls.COMPOUND_READER_WRITER;
	}
}
