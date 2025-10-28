package de.siphalor.tweed5.data.extension.impl;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.data.extension.api.TweedReaderWriterProvider;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

import static de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters.*;

@AutoService(TweedReaderWriterProvider.class)
public class DefaultTweedEntryReaderWriterImplsProvider implements TweedReaderWriterProvider {
	@Override
	public void provideReaderWriters(ProviderContext context) {
		StaticReaderWriterFactory<TweedEntryReader<?, ?>> booleanReaderFactory = new StaticReaderWriterFactory<>(booleanReaderWriter());
		StaticReaderWriterFactory<TweedEntryWriter<?, ?>> booleanWriterFactory = new StaticReaderWriterFactory<>(booleanReaderWriter());
		context.registerReaderFactory("tweed5.bool", booleanReaderFactory);
		context.registerReaderFactory("tweed5.boolean", booleanReaderFactory);
		context.registerWriterFactory("tweed5.bool", booleanWriterFactory);
		context.registerWriterFactory("tweed5.boolean", booleanWriterFactory);
		context.registerReaderFactory("tweed5.byte", new StaticReaderWriterFactory<>(byteReaderWriter()));
		context.registerWriterFactory("tweed5.byte", new StaticReaderWriterFactory<>(byteReaderWriter()));
		context.registerReaderFactory("tweed5.short", new StaticReaderWriterFactory<>(shortReaderWriter()));
		context.registerWriterFactory("tweed5.short", new StaticReaderWriterFactory<>(shortReaderWriter()));
		StaticReaderWriterFactory<TweedEntryReader<?, ?>> integerReaderFactory =
				new StaticReaderWriterFactory<>(intReaderWriter());
		StaticReaderWriterFactory<TweedEntryWriter<?, ?>> integerWriterFactory =
				new StaticReaderWriterFactory<>(intReaderWriter());
		context.registerReaderFactory("tweed5.int", integerReaderFactory);
		context.registerReaderFactory("tweed5.integer", integerReaderFactory);
		context.registerWriterFactory("tweed5.int", integerWriterFactory);
		context.registerWriterFactory("tweed5.integer", integerWriterFactory);
		context.registerReaderFactory("tweed5.long", new StaticReaderWriterFactory<>(longReaderWriter()));
		context.registerWriterFactory("tweed5.long", new StaticReaderWriterFactory<>(longReaderWriter()));
		context.registerReaderFactory("tweed5.float", new StaticReaderWriterFactory<>(floatReaderWriter()));
		context.registerWriterFactory("tweed5.float", new StaticReaderWriterFactory<>(floatReaderWriter()));
		context.registerReaderFactory("tweed5.double", new StaticReaderWriterFactory<>(doubleReaderWriter()));
		context.registerWriterFactory("tweed5.double", new StaticReaderWriterFactory<>(doubleReaderWriter()));
		context.registerReaderFactory("tweed5.string", new StaticReaderWriterFactory<>(stringReaderWriter()));
		context.registerWriterFactory("tweed5.string", new StaticReaderWriterFactory<>(stringReaderWriter()));
		context.registerReaderFactory("tweed5.enum", new StaticReaderWriterFactory<>(enumReaderWriter()));
		context.registerWriterFactory("tweed5.enum", new StaticReaderWriterFactory<>(enumReaderWriter()));
		context.registerReaderFactory("tweed5.nullable", new NullableReaderWriterFactory<>(
				TweedEntryReaderWriters::nullableReader
		));
		context.registerWriterFactory("tweed5.nullable", new NullableReaderWriterFactory<>(
				TweedEntryReaderWriters::nullableWriter
		));
		context.registerReaderFactory("tweed5.collection", new StaticReaderWriterFactory<>(collectionReaderWriter()));
		context.registerWriterFactory("tweed5.collection", new StaticReaderWriterFactory<>(collectionReaderWriter()));
		context.registerReaderFactory("tweed5.compound", new StaticReaderWriterFactory<>(compoundReaderWriter()));
		context.registerWriterFactory("tweed5.compound", new StaticReaderWriterFactory<>(compoundReaderWriter()));
	}

	@RequiredArgsConstructor
	private static class NullableReaderWriterFactory<T> implements ReaderWriterFactory<T> {
		private final Function<T, T> delegateBasedFactory;

		@Override
		public T create(T... delegateReaderWriters) {
			if (delegateReaderWriters.length == 0) {
				//noinspection unchecked
				return (T) TweedEntryReaderWriters.nullableReaderWriter();
			} else if (delegateReaderWriters.length == 1) {
				return delegateBasedFactory.apply(delegateReaderWriters[0]);
			} else {
				throw new IllegalArgumentException("Nullable readers and writers may have only one or zero delegates");
			}
		}
	}
}
