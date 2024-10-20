package de.siphalor.tweed5.data.extension.impl;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.data.extension.api.TweedReaderWriterProvider;

import static de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters.*;

@AutoService(TweedReaderWriterProvider.class)
public class DefaultTweedEntryReaderWriterImplsProvider implements TweedReaderWriterProvider {
	@Override
	public void provideReaderWriters(ProviderContext context) {
		context.registerReaderWriterFactory("boolean", new StaticReaderWriterFactory<>(booleanReaderWriter()));
		context.registerReaderWriterFactory("byte", new StaticReaderWriterFactory<>(byteReaderWriter()));
		context.registerReaderWriterFactory("short", new StaticReaderWriterFactory<>(shortReaderWriter()));
		context.registerReaderWriterFactory("int", new StaticReaderWriterFactory<>(intReaderWriter()));
		context.registerReaderWriterFactory("long", new StaticReaderWriterFactory<>(longReaderWriter()));
		context.registerReaderWriterFactory("float", new StaticReaderWriterFactory<>(floatReaderWriter()));
		context.registerReaderWriterFactory("double", new StaticReaderWriterFactory<>(doubleReaderWriter()));
		context.registerReaderWriterFactory("string", new StaticReaderWriterFactory<>(stringReaderWriter()));
		context.registerReaderWriterFactory("coherent_collection", new StaticReaderWriterFactory<>(coherentCollectionReaderWriter()));
		context.registerReaderWriterFactory("compound", new StaticReaderWriterFactory<>(compoundReaderWriter()));

		context.registerReaderWriterFactory("nullable", delegateReaderWriters -> {
			if (delegateReaderWriters.length != 1) {
				throw new IllegalArgumentException("Nullable reader writer requires a single delegate argument, got " + delegateReaderWriters.length);
			}
			return nullableReaderWriter(delegateReaderWriters[0]);
		});
	}
}
