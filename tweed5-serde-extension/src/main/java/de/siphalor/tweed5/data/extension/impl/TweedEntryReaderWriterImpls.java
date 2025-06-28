package de.siphalor.tweed5.data.extension.impl;

import de.siphalor.tweed5.core.api.entry.CollectionConfigEntry;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.data.extension.api.*;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriter;
import de.siphalor.tweed5.dataapi.api.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

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

	public static final TweedEntryReaderWriter<Collection<Object>, CollectionConfigEntry<Object, Collection<Object>>> COLLECTION_READER_WRITER = new CollectionReaderWriter<>();
	public static final TweedEntryReaderWriter<Object, CompoundConfigEntry<Object>> COMPOUND_READER_WRITER = new CompoundReaderWriter<>();

	public static final TweedEntryReaderWriter<Object, ConfigEntry<Object>> NOOP_READER_WRITER = new NoopReaderWriter();

	@RequiredArgsConstructor
	public static class NullableReader<T extends @Nullable Object, C extends ConfigEntry<T>> implements TweedEntryReader<T, C> {
		private final TweedEntryReader<T, C> delegate;

		@Override
		public T read(TweedDataReader reader, C entry, TweedReadContext context) throws TweedEntryReadException {
			if (reader.peekToken().isNull()) {
				reader.readToken();
				return null;
			}
			return delegate.read(reader, entry, context);
		}
	}

	@RequiredArgsConstructor
	public static class NullableWriter<T extends @Nullable Object, C extends ConfigEntry<T>> implements TweedEntryWriter<T, C> {
		private final TweedEntryWriter<T, C> delegate;

		@Override
		public void write(TweedDataVisitor writer, T value, C entry, TweedWriteContext context) throws TweedEntryWriteException, TweedDataWriteException {
			if (value == null) {
				writer.visitNull();
			} else {
				delegate.write(writer, value, entry, context);
			}
		}
	}

	@RequiredArgsConstructor
	private static class PrimitiveReaderWriter<T extends @Nullable Object> implements TweedEntryReaderWriter<T, ConfigEntry<T>> {
		private final Function<TweedDataToken, T> readerCall;
		private final BiConsumer<TweedDataVisitor, T> writerCall;

		@Override
		public T read(TweedDataReader reader, ConfigEntry<T> entry, TweedReadContext context) {
			return readerCall.apply(reader.readToken());
		}

		@Override
		public void write(TweedDataVisitor writer, @Nullable T value, ConfigEntry<T> entry, TweedWriteContext context) throws TweedEntryWriteException, TweedDataWriteException {
			requireNonNullWriteValue(value, context);
			writerCall.accept(writer, value);
		}
	}

	public static class CollectionReaderWriter<T extends @Nullable Object, C extends Collection<T>> implements TweedEntryReaderWriter<C, CollectionConfigEntry<T, C>> {
		@Override
		public C read(TweedDataReader reader, CollectionConfigEntry<T, C> entry, TweedReadContext context) throws
				TweedEntryReadException {
			assertIsToken(reader.readToken(), TweedDataToken::isListStart, "Expected list start", context);
			TweedDataToken token = reader.peekToken();
			if (token.isListEnd()) {
				return entry.instantiateCollection(0);
			}

			ConfigEntry<T> elementEntry = entry.elementEntry();
			TweedEntryReader<T, ConfigEntry<T>> elementReader = context.readWriteExtension().getReaderChain(elementEntry);

			List<@Nullable T> list = new ArrayList<>(20);
			while (true) {
				token = reader.peekToken();
				if (token.isListEnd()) {
					reader.readToken();
					break;
				} else if (token.isListValue()) {
					list.add(elementReader.read(reader, elementEntry, context));
				} else {
					throw new TweedEntryReadException(
							"Unexpected token " + token + ": expected next list value or list end",
							context
					);
				}
			}

			C result = entry.instantiateCollection(list.size());
			result.addAll(list);
			return result;
		}

		@Override
		public void write(TweedDataVisitor writer, C value, CollectionConfigEntry<T, C> entry, TweedWriteContext context) throws TweedEntryWriteException, TweedDataWriteException {
			requireNonNullWriteValue(value, context);

			if (value.isEmpty()) {
				writer.visitEmptyList();
				return;
			}

			ConfigEntry<T> elementEntry = entry.elementEntry();
			TweedEntryWriter<T, ConfigEntry<T>> elementWriter = context.readWriteExtension().getWriterChain(elementEntry);

			writer.visitListStart();
			for (T element : value) {
				elementWriter.write(writer, element, elementEntry, context);
			}
			writer.visitListEnd();
		}
	}

	public static class CompoundReaderWriter<T> implements TweedEntryReaderWriter<T, CompoundConfigEntry<T>> {
		@Override
		public T read(TweedDataReader reader, CompoundConfigEntry<T> entry, TweedReadContext context) throws
				TweedEntryReadException {
			assertIsToken(reader.readToken(), TweedDataToken::isMapStart, "Expected map start", context);

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
					TweedEntryReader<Object, ConfigEntry<Object>> subEntryReaderChain = context.readWriteExtension().getReaderChain(subEntry);
					Object subEntryValue = subEntryReaderChain.read(reader, subEntry, context);
					entry.set(compoundValue, key, subEntryValue);
				} else {
					throw new TweedEntryReadException(
							"Unexpected token " + token + ": Expected map key or map end",
							context
					);
				}
			}
			return compoundValue;
		}

		@Override
		public void write(TweedDataVisitor writer, @Nullable T value, CompoundConfigEntry<T> entry, TweedWriteContext context) throws TweedEntryWriteException, TweedDataWriteException {
			requireNonNullWriteValue(value, context);

			writer.visitMapStart();

			//noinspection unchecked
			Map<String, ConfigEntry<Object>> compoundEntries = (Map<String, ConfigEntry<Object>>)(Map<?, ?>) entry.subEntries();
			for (Map.Entry<String, ConfigEntry<Object>> e : compoundEntries.entrySet()) {
				String key = e.getKey();
				ConfigEntry<Object> subEntry = e.getValue();

				TweedEntryWriter<Object, ConfigEntry<Object>> subEntryWriterChain = context.readWriteExtension().getWriterChain(subEntry);

				writer.visitMapEntryKey(key);
				subEntryWriterChain.write(writer, entry.get(value, key), subEntry, context);
			}

			writer.visitMapEnd();
		}
	}

	public static class NoopReaderWriter implements TweedEntryReaderWriter<@Nullable Object, ConfigEntry<Object>> {
		@Override
		public @Nullable Object read(TweedDataReader reader, ConfigEntry<Object> entry, TweedReadContext context) {
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
		public void write(TweedDataVisitor writer, @Nullable Object value, ConfigEntry<Object> entry, TweedWriteContext context) throws TweedDataWriteException {
			writer.visitNull();
		}

		private enum Context {
			LIST, MAP,
		}
	}

	@Contract("null, _ -> fail")
	private static <T> void requireNonNullWriteValue(
			@Nullable T value,
			TweedWriteContext context
	) throws TweedEntryWriteException {
		if (value == null) {
			throw new TweedEntryWriteException("Unable to write null value", context);
		}
	}

	private static void assertIsToken(
			TweedDataToken token,
			Predicate<TweedDataToken> isToken,
			String description,
			TweedReadContext context
	) throws TweedEntryReadException {
		if (!isToken.test(token)) {
			throw new TweedEntryReadException("Unexpected token " + token + ": " + description, context);
		}
	}
}
