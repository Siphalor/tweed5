package de.siphalor.tweed5.serde.extension.impl;

import de.siphalor.tweed5.core.api.entry.CollectionConfigEntry;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.NullableConfigEntry;
import de.siphalor.tweed5.serde.extension.api.*;
import de.siphalor.tweed5.serde.extension.api.read.result.ThrowingReadFunction;
import de.siphalor.tweed5.serde.extension.api.read.result.TweedReadIssue;
import de.siphalor.tweed5.serde.extension.api.read.result.TweedReadResult;
import de.siphalor.tweed5.serde.extension.api.readwrite.TweedEntryReaderWriter;
import de.siphalor.tweed5.serde_api.api.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TweedEntryReaderWriterImpls {
	public static final TweedEntryReaderWriter<Boolean, ConfigEntry<Boolean>> BOOLEAN_READER_WRITER = new PrimitiveReaderWriter<>(
			TweedDataToken::readAsBoolean, TweedDataVisitor::visitBoolean);
	public static final TweedEntryReaderWriter<Byte, ConfigEntry<Byte>> BYTE_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsByte, TweedDataVisitor::visitByte);
	public static final TweedEntryReaderWriter<Short, ConfigEntry<Short>> SHORT_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsShort, TweedDataVisitor::visitShort);
	public static final TweedEntryReaderWriter<Integer, ConfigEntry<Integer>> INT_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsInt, TweedDataVisitor::visitInt);
	public static final TweedEntryReaderWriter<Long, ConfigEntry<Long>> LONG_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsLong, TweedDataVisitor::visitLong);
	public static final TweedEntryReaderWriter<Float, ConfigEntry<Float>> FLOAT_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsFloat, TweedDataVisitor::visitFloat);
	public static final TweedEntryReaderWriter<Double, ConfigEntry<Double>> DOUBLE_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsDouble, TweedDataVisitor::visitDouble);
	public static final TweedEntryReaderWriter<String, ConfigEntry<String>> STRING_READER_WRITER = new PrimitiveReaderWriter<>(TweedDataToken::readAsString, TweedDataVisitor::visitString);
	public static final TweedEntryReaderWriter<Enum<?>, ConfigEntry<Enum<?>>> ENUM_READER_WRITER = new EnumReaderWriter<>();

	public static final TweedEntryReaderWriter<Object, NullableConfigEntry<Object>> NULLABLE_READER_WRITER = new NullableReaderWriter<>();
	public static final TweedEntryReaderWriter<Collection<Object>, CollectionConfigEntry<Object, Collection<Object>>> COLLECTION_READER_WRITER = new CollectionReaderWriter<>();
	public static final TweedEntryReaderWriter<Object, CompoundConfigEntry<Object>> COMPOUND_READER_WRITER = new CompoundReaderWriter<>();

	public static final TweedEntryReaderWriter<Object, ConfigEntry<Object>> NOOP_READER_WRITER = new NoopReaderWriter();

	public static class NullableReaderWriter<T extends @Nullable Object> implements TweedEntryReaderWriter<T, NullableConfigEntry<T>> {
		@Override
		public TweedReadResult<T> read(TweedDataReader reader, NullableConfigEntry<T> entry, TweedReadContext context) {
			try {
				if (reader.peekToken().isNull()) {
					reader.readToken();
					return TweedReadResult.ok(null);
				}
			} catch (TweedDataReadException e) {
				return TweedReadResult.failed(TweedReadIssue.error(e, context));
			}

			return context.readSubEntry(reader, entry.nonNullEntry());
		}

		@Override
		public void write(
				TweedDataVisitor writer,
				@Nullable T value,
				NullableConfigEntry<T> entry,
				TweedWriteContext context
		) throws TweedEntryWriteException, TweedDataWriteException {
			if (value == null) {
				writer.visitNull();
			} else {
				context.writeSubEntry(writer, value, entry.nonNullEntry());
			}
		}
	}

	@Deprecated
	@RequiredArgsConstructor
	public static class FixedNullableReader<T extends @Nullable Object, C extends ConfigEntry<T>> implements TweedEntryReader<T, C> {
		private final TweedEntryReader<T, C> delegate;

		@Override
		public TweedReadResult<T> read(TweedDataReader reader, C entry, TweedReadContext context) {
			try {
				if (reader.peekToken().isNull()) {
					reader.readToken();
					return TweedReadResult.ok(null);
				}
			} catch (TweedDataReadException e) {
				return TweedReadResult.failed(TweedReadIssue.error(e, context));
			}
			return delegate.read(reader, entry, context);
		}
	}

	@Deprecated
	@RequiredArgsConstructor
	public static class FixedNullableWriter<T extends @Nullable Object, C extends ConfigEntry<T>> implements TweedEntryWriter<T, C> {
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
	private static class PrimitiveReaderWriter<T> implements TweedEntryReaderWriter<T, ConfigEntry<T>> {
		private final PrimitiveReadFunction<T> readerFunction;
		private final PrimitiveWriteFunction<T> writerFunction;

		@Override
		public TweedReadResult<T> read(TweedDataReader reader, ConfigEntry<T> entry, TweedReadContext context) {
			try {
				return TweedReadResult.ok(readerFunction.read(reader.readToken()));
			} catch (TweedDataReadException e) {
				return TweedReadResult.failed(TweedReadIssue.error(e, context));
			}
		}

		@Override
		public void write(TweedDataVisitor writer, @Nullable T value, ConfigEntry<T> entry, TweedWriteContext context) throws TweedEntryWriteException, TweedDataWriteException {
			requireNonNullWriteValue(value, context);
			writerFunction.write(writer, value);
		}
	}

	@RequiredArgsConstructor
	public static class EnumReaderWriter<T extends Enum<?>> implements TweedEntryReaderWriter<T, ConfigEntry<T>> {
		@Override
		public TweedReadResult<T> read(TweedDataReader reader, ConfigEntry<T> entry, TweedReadContext context) {
			//noinspection unchecked,rawtypes
			return requireToken(reader, TweedDataToken::canReadAsString, "Expected string", context)
					.map(TweedDataToken::readAsString, context)
					.andThen(ThrowingReadFunction.any(
							context,
							value -> (T) Enum.valueOf(((Class) entry.valueClass()), value),
							value -> (T) Enum.valueOf(((Class) entry.valueClass()), value.toUpperCase(Locale.ROOT))
					), context);
		}

		@Override
		public void write(
				TweedDataVisitor writer,
				@Nullable T value,
				ConfigEntry<T> entry,
				TweedWriteContext context
		) throws TweedEntryWriteException, TweedDataWriteException {
			requireNonNullWriteValue(value, context);
			writer.visitString(value.name());
		}
	}

	public static class CollectionReaderWriter<T extends @Nullable Object, C extends Collection<T>> implements TweedEntryReaderWriter<C, CollectionConfigEntry<T, C>> {
		@Override
		public TweedReadResult<C> read(TweedDataReader reader, CollectionConfigEntry<T, C> entry, TweedReadContext context) {
			return requireToken(reader, TweedDataToken::isListStart, "Expected list start", context).andThen(_token -> {
				if (reader.peekToken().isListEnd()) {
					return TweedReadResult.ok(entry.instantiateCollection(0));
				}

				ConfigEntry<T> elementEntry = entry.elementEntry();
				List<T> list = new ArrayList<>(20);
				List<TweedReadIssue> issues = new ArrayList<>();
				while (true) {
					try {
						TweedDataToken token = reader.peekToken();
						if (token.isListEnd()) {
							reader.readToken();
							break;
						} else if (token.isListValue()) {
							TweedReadResult<T> elementResult = context.readSubEntry(reader, elementEntry);
							issues.addAll(Arrays.asList(elementResult.issues()));
							if (elementResult.isFailed() || elementResult.isError()) {
								return TweedReadResult.failed(issues.toArray(new TweedReadIssue[0]));
							} else if (elementResult.hasValue()) {
								list.add(elementResult.value());
							}
						} else {
							issues.add(TweedReadIssue.error(
									"Unexpected token " + token + ": expected next list value or list end",
									context
							));
							return TweedReadResult.failed(issues.toArray(new TweedReadIssue[0]));
						}
					} catch (TweedDataReadException e) {
						issues.add(TweedReadIssue.error(e, context));
					}
				}
				C result = entry.instantiateCollection(list.size());
				result.addAll(list);
				return TweedReadResult.ok(result);
			}, context);
		}

		@Override
		public void write(TweedDataVisitor writer, C value, CollectionConfigEntry<T, C> entry, TweedWriteContext context)
				throws TweedEntryWriteException, TweedDataWriteException {
			requireNonNullWriteValue(value, context);

			if (value.isEmpty()) {
				writer.visitEmptyList();
				return;
			}

			ConfigEntry<T> elementEntry = entry.elementEntry();

			writer.visitListStart();
			for (T element : value) {
				context.writeSubEntry(writer, element, elementEntry);
			}
			writer.visitListEnd();
		}
	}

	public static class CompoundReaderWriter<T> implements TweedEntryReaderWriter<T, CompoundConfigEntry<T>> {
		@Override
		public TweedReadResult<T> read(TweedDataReader reader, CompoundConfigEntry<T> entry, TweedReadContext context) {
			return requireToken(reader, TweedDataToken::isMapStart, "Expected map start", context).andThen(_token -> {
				Map<String, ConfigEntry<?>> compoundEntries = entry.subEntries();
				T compoundValue = entry.instantiateValue();
				List<TweedReadIssue> issues = new ArrayList<>();
				while (true) {
					try {
						TweedDataToken token = reader.readToken();
						if (token.isMapEnd()) {
							break;
						} else if (token.isMapEntryKey()) {
							String key = token.readAsString();

							//noinspection unchecked
							ConfigEntry<Object> subEntry = (ConfigEntry<Object>) compoundEntries.get(key);
							if (subEntry == null) {
								TweedReadResult<Object> noopResult = NOOP_READER_WRITER.read(reader, null, context);
								issues.addAll(Arrays.asList(noopResult.issues()));
								if (noopResult.isFailed()) {
									return TweedReadResult.failed(issues.toArray(new TweedReadIssue[0]));
								}
								continue;
							}
							TweedReadResult<Object> subEntryResult = context.readSubEntry(reader, subEntry);
							issues.addAll(Arrays.asList(subEntryResult.issues()));
							if (subEntryResult.isFailed() || subEntryResult.isError()) {
								return TweedReadResult.failed(issues.toArray(new TweedReadIssue[0]));
							} else if (subEntryResult.hasValue()) {
								entry.set(compoundValue, key, subEntryResult.value());
							}
						} else {
							issues.add(TweedReadIssue.error(
									"Unexpected token " + token + ": Expected map key or map end",
									context
							));
							return TweedReadResult.failed(issues.toArray(new TweedReadIssue[0]));
						}
					} catch (TweedDataReadException e) {
						throw new TweedEntryReadException("Failed reading compound entry", e, context);
					}
				}
				if (issues.isEmpty()) {
					return TweedReadResult.ok(compoundValue);
				} else {
					return TweedReadResult.withIssues(compoundValue, issues.toArray(new TweedReadIssue[0]));
				}
			}, context);
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

				writer.visitMapEntryKey(key);
				context.writeSubEntry(writer, entry.get(value, key), subEntry);
			}

			writer.visitMapEnd();
		}
	}

	public static class NoopReaderWriter implements TweedEntryReaderWriter<@Nullable Object, ConfigEntry<Object>> {
		@Override
		public TweedReadResult<@Nullable Object> read(TweedDataReader reader, ConfigEntry<Object> entry, TweedReadContext context) {
			try {
				TweedDataToken token = reader.readToken();
				if (!token.isListStart() && !token.isMapStart()) {
					return TweedReadResult.empty();
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
						return TweedReadResult.empty();
					}
				}
			} catch (TweedDataReadException e) {
				return TweedReadResult.failed(TweedReadIssue.error(e, context));
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

	private interface PrimitiveReadFunction<T extends Object> {
		T read(TweedDataToken token) throws TweedDataReadException;
	}

	private interface PrimitiveWriteFunction<T extends Object> {
		void write(TweedDataVisitor writer, T value) throws TweedDataWriteException;
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

	private static TweedReadResult<TweedDataToken> requireToken(
			TweedDataReader reader,
			Predicate<TweedDataToken> isToken,
			String description,
			TweedReadContext context
	) {
		try {
			TweedDataToken token = reader.readToken();
			if (isToken.test(token)) {
				return TweedReadResult.ok(token);
			}
			return TweedReadResult.failed(TweedReadIssue.error(
					"Unexpected token " + token + ": " + description,
					context
			));
		} catch (TweedDataReadException e) {
			return TweedReadResult.failed(TweedReadIssue.error(e, context));
		}
	}
}
