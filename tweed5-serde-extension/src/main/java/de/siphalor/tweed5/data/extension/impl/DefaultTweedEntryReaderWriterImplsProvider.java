package de.siphalor.tweed5.data.extension.impl;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.data.extension.api.TweedReaderWriterProvider;

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
		context.registerReaderFactory("tweed5.collection", new StaticReaderWriterFactory<>(collectionReaderWriter()));
		context.registerWriterFactory("tweed5.collection", new StaticReaderWriterFactory<>(collectionReaderWriter()));
		context.registerReaderFactory("tweed5.compound", new StaticReaderWriterFactory<>(compoundReaderWriter()));
		context.registerWriterFactory("tweed5.compound", new StaticReaderWriterFactory<>(compoundReaderWriter()));

		context.registerReaderFactory("tweed5.nullable", delegateReaders -> {
			if (delegateReaders.length != 1) {
				throw new IllegalArgumentException("Nullable reader requires a single delegate argument, got " + delegateReaders.length);
			}
			return nullableReader(delegateReaders[0]);
		});
		context.registerWriterFactory("tweed5.nullable", delegateWriters -> {
			if (delegateWriters.length != 1) {
				throw new IllegalArgumentException("Nullable writer requires a single delegate argument, got " + delegateWriters.length);
			}
			return nullableWriter(delegateWriters[0]);
		});
	}
}
