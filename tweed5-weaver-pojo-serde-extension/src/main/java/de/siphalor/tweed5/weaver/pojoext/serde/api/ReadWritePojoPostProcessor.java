package de.siphalor.tweed5.weaver.pojoext.serde.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.data.extension.api.*;
import de.siphalor.tweed5.data.extension.impl.TweedEntryReaderWriterImpls;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;
import de.siphalor.tweed5.weaver.pojo.api.weaving.postprocess.TweedPojoWeavingPostProcessor;
import de.siphalor.tweed5.weaver.pojoext.serde.impl.SerdePojoReaderWriterSpec;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
public class ReadWritePojoPostProcessor implements TweedPojoWeavingPostProcessor {
	private final Map<String, TweedReaderWriterProvider.ReaderWriterFactory<TweedEntryReader<?, ?>>> readerFactories = new HashMap<>();
	private final Map<String, TweedReaderWriterProvider.ReaderWriterFactory<TweedEntryWriter<?, ?>>> writerFactories = new HashMap<>();

	public ReadWritePojoPostProcessor() {
		loadProviders();
	}

	private void loadProviders() {
		ServiceLoader<TweedReaderWriterProvider> serviceLoader = ServiceLoader.load(TweedReaderWriterProvider.class);

		for (TweedReaderWriterProvider readerWriterProvider : serviceLoader) {
			TweedReaderWriterProvider.ProviderContext providerContext = new TweedReaderWriterProvider.ProviderContext() {
				@Override
				public void registerReaderFactory(
						String id,
						TweedReaderWriterProvider.ReaderWriterFactory<TweedEntryReader<?, ?>> readerFactory
				) {
					if (readerFactories.putIfAbsent(id, readerFactory) != null) {
						log.warn(
								"Found duplicate Tweed entry reader id \"{}\" in provider class {}",
								id,
								readerWriterProvider.getClass().getName()
						);
					}
				}

				@Override
				public void registerWriterFactory(
						String id,
						TweedReaderWriterProvider.ReaderWriterFactory<TweedEntryWriter<?, ?>> writerFactory
				) {
					if (writerFactories.putIfAbsent(id, writerFactory) != null) {
						log.warn(
								"Found duplicate Tweed entry writer id \"{}\" in provider class {}",
								id,
								readerWriterProvider.getClass().getName()
						);
					}
				}
			};

			readerWriterProvider.provideReaderWriters(providerContext);
		}
	}

	@Override
	public void apply(ConfigEntry<?> configEntry, WeavingContext context) {
		EntryReadWriteConfig entryConfig = context.annotations().getAnnotation(EntryReadWriteConfig.class);
		if (entryConfig == null) {
			return;
		}

		ReadWriteExtension readWriteExtension = context.configContainer().extension(ReadWriteExtension.class);
		if (readWriteExtension == null) {
			log.error("You must not use {} without the {}", this.getClass().getSimpleName(), ReadWriteExtension.class.getSimpleName());
			return;
		}

		readWriteExtension.setEntryReaderWriterDefinition(configEntry, createDefinitionFromEntryConfig(entryConfig, context));
	}

	private EntryReaderWriterDefinition createDefinitionFromEntryConfig(EntryReadWriteConfig entryConfig, WeavingContext context) {
		String readerSpecText = entryConfig.reader().isEmpty() ? entryConfig.value() : entryConfig.reader();
		String writerSpecText = entryConfig.writer().isEmpty() ? entryConfig.value() : entryConfig.writer();

		SerdePojoReaderWriterSpec readerSpec;
		SerdePojoReaderWriterSpec writerSpec;
		if (readerSpecText.equals(writerSpecText)) {
			readerSpec = writerSpec = specFromText(readerSpecText, context);
		} else {
			readerSpec = specFromText(readerSpecText, context);
			writerSpec = specFromText(writerSpecText, context);
		}

		//noinspection unchecked
		TweedEntryReader<?, ?> reader = readerSpec == null
				? TweedEntryReaderWriterImpls.NOOP_READER_WRITER
				: resolveReaderWriterFromSpec((Class<TweedEntryReader<?, ?>>)(Object) TweedEntryReader.class, readerFactories, readerSpec, context);
		//noinspection unchecked
		TweedEntryWriter<?, ?> writer = writerSpec == null
				? TweedEntryReaderWriterImpls.NOOP_READER_WRITER
				: resolveReaderWriterFromSpec((Class<TweedEntryWriter<?, ?>>)(Object) TweedEntryWriter.class, writerFactories, writerSpec, context);

		return new EntryReaderWriterDefinition() {
			@Override
			public TweedEntryReader<?, ?> reader() {
				return reader;
			}

			@Override
			public TweedEntryWriter<?, ?> writer() {
				return writer;
			}
		};
	}

	@Nullable
	private SerdePojoReaderWriterSpec specFromText(String specText, WeavingContext context) {
		if (specText.isEmpty()) {
			return null;
		}
		try {
			return SerdePojoReaderWriterSpec.parse(specText);
		} catch (SerdePojoReaderWriterSpec.ParseException e) {
			log.warn(
					"Failed to parse definition for reader or writer on entry {}, entry will not be included in serde",
					context.path(),
					e
			);
			return null;
		}
	}

	private <T> T resolveReaderWriterFromSpec(
			Class<T> baseClass,
			Map<String, TweedReaderWriterProvider.ReaderWriterFactory<T>> factories,
			SerdePojoReaderWriterSpec spec,
			WeavingContext context
	) {
		//noinspection unchecked
		T[] arguments = spec.arguments()
				.stream()
				.map(argSpec -> resolveReaderWriterFromSpec(baseClass, factories, argSpec, context))
				.toArray(length -> (T[]) Array.newInstance(baseClass, length));

		TweedReaderWriterProvider.ReaderWriterFactory<T> factory = factories.get(spec.identifier());

		T instance;
		if (factory != null) {
			instance = factory.create(arguments);
		} else {
			instance = loadClassIfExists(baseClass, spec.identifier(), arguments);
		}

		if (instance == null) {
			log.warn(
					"Failed to resolve reader or writer factory \"{}\" for entry {}, entry will not be included in serde",
					spec.identifier(),
					context.path()
			);
			return null;
		}

		return instance;
	}

	private <T> T loadClassIfExists(Class<T> baseClass, String className, T[] arguments) {
		try {
			Class<?> clazz = Class.forName(className);
			Class<?>[] argClassses = new Class<?>[arguments.length];
			Arrays.fill(argClassses, baseClass);

			Constructor<?> constructor = clazz.getConstructor(argClassses);

			//noinspection unchecked
			return (T) constructor.newInstance((Object[]) arguments);
		} catch (ClassNotFoundException e) {
			return null;
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			log.warn("Failed to instantiate class {}", className, e);
			return null;
		}
	}
}
