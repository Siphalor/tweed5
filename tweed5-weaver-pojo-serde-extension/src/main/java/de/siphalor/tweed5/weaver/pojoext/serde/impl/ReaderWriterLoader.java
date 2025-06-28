package de.siphalor.tweed5.weaver.pojoext.serde.impl;

import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.data.extension.api.TweedReaderWriterProvider;
import de.siphalor.tweed5.data.extension.impl.TweedEntryReaderWriterImpls;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CommonsLog
public class ReaderWriterLoader {
	@Getter
	private final Map<String, TweedReaderWriterProvider.ReaderWriterFactory<TweedEntryReader<?, ?>>> readerFactories
			= new HashMap<>();
	@Getter
	private final Map<String, TweedReaderWriterProvider.ReaderWriterFactory<TweedEntryWriter<?, ?>>> writerFactories
			= new HashMap<>();
	private final TweedReaderWriterProvider.ProviderContext providerContext = new ProviderContext();

	public void load(TweedReaderWriterProvider provider) {
		try {
			provider.provideReaderWriters(providerContext);
		} catch (Exception e) {
			log.warn(
					"Unexpected exception while providing reader and writer factories using "
							+ provider.getClass().getName(),
					e
			);
		}
	}

	public TweedEntryReader<?, ?> resolveReaderFromSpec(SerdePojoReaderWriterSpec spec) {
		// noinspection unchecked
		TweedEntryReader<?, ?> reader = resolveReaderWriterFromSpec(
				(Class<TweedEntryReader<?, ?>>) (Object) TweedEntryReader.class,
				readerFactories(),
				spec
		);
		if (reader != null) {
			return reader;
		}
		return TweedEntryReaderWriterImpls.NOOP_READER_WRITER;
	}

	public TweedEntryWriter<?, ?> resolveWriterFromSpec(SerdePojoReaderWriterSpec spec) {
		//noinspection unchecked
		TweedEntryWriter<?, ?> writer = resolveReaderWriterFromSpec(
				(Class<TweedEntryWriter<?,?>>) (Object) TweedEntryWriter.class,
				writerFactories(),
				spec
		);
		if (writer != null) {
			return writer;
		}
		return TweedEntryReaderWriterImpls.NOOP_READER_WRITER;
	}

	private <T> @Nullable T resolveReaderWriterFromSpec(
			Class<T> baseClass,
			Map<String, TweedReaderWriterProvider.ReaderWriterFactory<T>> factories,
			SerdePojoReaderWriterSpec spec
	) {
		//noinspection unchecked
		T[] arguments = spec.arguments()
				.stream()
				.map(argSpec -> resolveReaderWriterFromSpec(baseClass, factories, argSpec))
				.toArray(length -> (T[]) Array.newInstance(baseClass, length));

		TweedReaderWriterProvider.ReaderWriterFactory<T> factory = factories.get(spec.identifier());

		T instance;
		try {
			if (factory != null) {
				instance = factory.create(arguments);
			} else {
				instance = loadClassIfExists(baseClass, spec.identifier(), arguments);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Failed to resolve reader or writer factory from \"" + spec.identifier() + "\"",
					e
			);
		}

		return instance;
	}

	private <T> @Nullable T loadClassIfExists(Class<T> baseClass, String className, T[] arguments) {
		try {
			Class<?> clazz = Class.forName(className);
			Class<?>[] argClasses = new Class<?>[arguments.length];
			Arrays.fill(argClasses, baseClass);

			Constructor<?> constructor = clazz.getConstructor(argClasses);

			//noinspection unchecked
			return (T) constructor.newInstance((Object[]) arguments);
		} catch (ClassNotFoundException e) {
			return null;
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			log.warn("Failed to instantiate class " + className, e);
			return null;
		}
	}

	private class ProviderContext implements TweedReaderWriterProvider.ProviderContext {
		@Override
		public void registerReaderFactory(
				String id,
				TweedReaderWriterProvider.ReaderWriterFactory<TweedEntryReader<?, ?>> readerFactory
		) {
			if (readerFactories.putIfAbsent(id, readerFactory) != null) {
				throw new IllegalArgumentException("Found duplicate Tweed entry reader id \"" + id + "\"");
			}
		}

		@Override
		public void registerWriterFactory(
				String id,
				TweedReaderWriterProvider.ReaderWriterFactory<TweedEntryWriter<?, ?>> writerFactory
		) {
			if (writerFactories.putIfAbsent(id, writerFactory) != null) {
				throw new IllegalArgumentException("Found duplicate Tweed entry reader id \"" + id + "\"");
			}
		}
	}
}
