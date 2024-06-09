package de.siphalor.tweed5.data.extension.impl;

import de.siphalor.tweed5.core.api.entry.CoherentCollectionConfigEntry;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.data.extension.api.*;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriter;
import de.siphalor.tweed5.dataapi.api.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TweedEntryReaderWriterImpls {
	public static final TweedEntryReaderWriter<Boolean, ConfigEntry<Boolean>> BOOLEAN_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsBoolean, TweedDataVisitor::visitBoolean);
	public static final TweedEntryReaderWriter<Byte, ConfigEntry<Byte>> BYTE_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsByte, TweedDataVisitor::visitByte);
	public static final TweedEntryReaderWriter<Short, ConfigEntry<Short>> SHORT_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsShort, TweedDataVisitor::visitShort);
	public static final TweedEntryReaderWriter<Integer, ConfigEntry<Integer>> INT_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsInt, TweedDataVisitor::visitInt);
	public static final TweedEntryReaderWriter<Long, ConfigEntry<Long>> LONG_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsLong, TweedDataVisitor::visitLong);
	public static final TweedEntryReaderWriter<Float, ConfigEntry<Float>> FLOAT_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsFloat, TweedDataVisitor::visitFloat);
	public static final TweedEntryReaderWriter<Double, ConfigEntry<Double>> DOUBLE_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsDouble, TweedDataVisitor::visitDouble);
	public static final TweedEntryReaderWriter<String, ConfigEntry<String>> STRING_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsString, TweedDataVisitor::visitString);

	public static final TweedEntryReaderWriter<Collection<Object>, CoherentCollectionConfigEntry<Object, Collection<Object>>> COHERENT_COLLECTION_READER_WRITER = new CoherentCollectionReaderWriter<>();
	public static final TweedEntryReaderWriter<Object, CompoundConfigEntry<Object>> COMPOUND_READER_WRITER = new CompoundReaderWriter<>();

	public static final TweedEntryReaderWriter<Object, ConfigEntry<Object>> NOOP_READER_WRITER = new NoopReaderWriter();

	@RequiredArgsConstructor
	private static class PrimitiveReaderWriter<T> implements TweedEntryReaderWriter<T, ConfigEntry<T>> {
		private final Function<TweedDataToken, T> readerCall;
		private final BiConsumer<TweedDataVisitor, T> writerCall;

		@Override
		public T read(TweedDataReader reader, ConfigEntry<T> entry, TweedReadContext context) throws TweedDataReadException {
			return readerCall.apply(reader.readToken());
		}

		@Override
		public void write(TweedDataVisitor writer, T value, ConfigEntry<T> entry, TweedWriteContext context) throws TweedEntryWriteException, TweedDataWriteException {
			requireNonNullWriteValue(value);
			writerCall.accept(writer, value);
		}
	}

	public static class CoherentCollectionReaderWriter<T, C extends Collection<T>> implements TweedEntryReaderWriter<C, CoherentCollectionConfigEntry<T, C>> {
		@Override
		public C read(TweedDataReader reader, CoherentCollectionConfigEntry<T, C> entry, TweedReadContext context) throws TweedEntryReadException, TweedDataReadException {
			assertIsToken(reader.readToken(), TweedDataToken::isListStart, "Expected list start");
			TweedDataToken token = reader.peekToken();
			if (token.isListEnd()) {
				return entry.instantiateCollection(0);
			}

			ConfigEntry<T> elementEntry = entry.elementEntry();
			TweedEntryReader<T, ConfigEntry<T>> elementReader = ReadWriteExtensionImpl.getReaderChain(elementEntry);

			List<T> list = new ArrayList<>(20);
			while (true) {
				token = reader.peekToken();
				if (token.isListEnd()) {
					reader.readToken();
					break;
				} else if (token.isListValue()) {
					list.add(elementReader.read(reader, elementEntry, context));
				} else {
					throw new TweedEntryReadException("Unexpected token " + token + ": expected next list value or list end");
				}
			}

			C result = entry.instantiateCollection(list.size());
			result.addAll(list);
			return result;
		}

		@Override
		public void write(TweedDataVisitor writer, C value, CoherentCollectionConfigEntry<T, C> entry, TweedWriteContext context) throws TweedEntryWriteException, TweedDataWriteException {
			requireNonNullWriteValue(value);

			if (value.isEmpty()) {
				writer.visitEmptyList();
				return;
			}

			ConfigEntry<T> elementEntry = entry.elementEntry();
			TweedEntryWriter<T, ConfigEntry<T>> elementWriter = ReadWriteExtensionImpl.getWriterChain(elementEntry);

			writer.visitListStart();
			for (T element : value) {
				elementWriter.write(writer, element, elementEntry, context);
			}
			writer.visitListEnd();
		}
	}

	public static class CompoundReaderWriter<T> implements TweedEntryReaderWriter<T, CompoundConfigEntry<T>> {
		@Override
		public T read(TweedDataReader reader, CompoundConfigEntry<T> entry, TweedReadContext context) throws TweedEntryReadException, TweedDataReadException {
			assertIsToken(reader.readToken(), TweedDataToken::isMapStart, "Expected map start");

			Map<String, ConfigEntry<?>> compoundEntries = entry.subEntries();
			T compoundValue = entry.instantiateCompoundValue();
			while (true) {
				TweedDataToken token = reader.readToken();
				if (token.isMapEnd()) {
					break;
				} else if (token.isMapEntryKey()) {
					String key = token.readAsString();

					//noinspection unchecked
					ConfigEntry<Object> subEntry = (ConfigEntry<Object>) compoundEntries.get(key);
					TweedEntryReader<Object, ConfigEntry<Object>> subEntryReaderChain = ReadWriteExtensionImpl.getReaderChain(subEntry);

					Object subEntryValue = subEntryReaderChain.read(reader, subEntry, context);
					entry.set(compoundValue, key, subEntryValue);
				} else {
					throw new TweedEntryReadException("Unexpected token " + token + ": Expected map key or map end");
				}
			}
			return compoundValue;
		}

		@Override
		public void write(TweedDataVisitor writer, T value, CompoundConfigEntry<T> entry, TweedWriteContext context) throws TweedEntryWriteException, TweedDataWriteException {
			requireNonNullWriteValue(value);

			writer.visitMapStart();

			//noinspection unchecked
			Map<String, ConfigEntry<Object>> compoundEntries = (Map<String, ConfigEntry<Object>>)(Map<?, ?>) entry.subEntries();
			for (Map.Entry<String, ConfigEntry<Object>> e : compoundEntries.entrySet()) {
				String key = e.getKey();
				ConfigEntry<Object> subEntry = e.getValue();

				writer.visitMapEntryKey(key);

				TweedEntryWriter<Object, ConfigEntry<Object>> subEntryWriterChain = ReadWriteExtensionImpl.getWriterChain(subEntry);
				subEntryWriterChain.write(writer, entry.get(value, key), subEntry, context);
			}

			writer.visitMapEnd();
		}
	}

	public static class NoopReaderWriter implements TweedEntryReaderWriter<Object, ConfigEntry<Object>> {
		@Override
		public Object read(TweedDataReader reader, ConfigEntry<Object> entry, TweedReadContext context) throws TweedDataReadException {
			TweedDataToken token = reader.readToken();
			if (!token.isListStart() && !token.isMapStart()) {
				return null;
			}

			ArrayDeque<Context> stack = new ArrayDeque<>(20);
			if (token.isListStart()) {
				stack.push(Context.LIST);
			} else if (token.isMapStart()) {
				stack.push(Context.MAP);
			}

			while (true) {
				token = reader.readToken();
				if (token.isListStart()) {
					stack.push(Context.LIST);
				} else if (token.isMapStart()) {
					stack.push(Context.MAP);
				} else if (token.isListEnd() || token.isMapEnd()) {
					stack.pop();
				}
				if (stack.isEmpty()) {
					return null;
				}
			}
		}

		@Override
		public void write(TweedDataVisitor writer, Object value, ConfigEntry<Object> entry, TweedWriteContext context) throws TweedDataWriteException {
			writer.visitNull();
		}

		private enum Context {
			LIST, MAP,
		}
	}

	private static <T> void requireNonNullWriteValue(T value) throws TweedEntryWriteException {
		if (value == null) {
			throw new TweedEntryWriteException("Unable to write null value");
		}
	}

	private static void assertIsToken(TweedDataToken token, Predicate<TweedDataToken> isToken, String description) throws TweedEntryReadException {
		if (!isToken.test(token)) {
			throw new TweedEntryReadException("Unexpected token " + token + ": " + description);
		}
	}
}
