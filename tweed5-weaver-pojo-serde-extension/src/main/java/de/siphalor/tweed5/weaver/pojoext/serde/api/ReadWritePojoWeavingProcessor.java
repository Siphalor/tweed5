package de.siphalor.tweed5.weaver.pojoext.serde.api;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.data.extension.api.TweedReaderWriterProvider;
import de.siphalor.tweed5.data.extension.impl.TweedEntryReaderWriterImpls;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;
import de.siphalor.tweed5.weaver.pojoext.serde.impl.SerdePojoReaderWriterSpec;
import lombok.extern.apachecommons.CommonsLog;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@CommonsLog
public class ReadWritePojoWeavingProcessor implements TweedPojoWeavingExtension {
	private final Map<String, TweedReaderWriterProvider.ReaderWriterFactory<TweedEntryReader<?, ?>>> readerFactories = new HashMap<>();
	private final Map<String, TweedReaderWriterProvider.ReaderWriterFactory<TweedEntryWriter<?, ?>>> writerFactories = new HashMap<>();
	private final ReadWriteExtension readWriteExtension;

	public ReadWritePojoWeavingProcessor(ConfigContainer<?> configContainer) {
		this.readWriteExtension = configContainer.extension(ReadWriteExtension.class)
				.orElseThrow(() -> new IllegalStateException(
						"You must register a " + ReadWriteExtension.class.getSimpleName()
								+ " to use the " + getClass().getSimpleName()
				));
	}

	@Override
	public void setup(SetupContext context) {
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
					if (readerFactories.putIfAbsent(id, readerFactory) != null && log.isWarnEnabled()) {
						log.warn(
								"Found duplicate Tweed entry reader id \"" + id + "\" in provider class "
										+ readerWriterProvider.getClass().getName()
						);
					}
				}

				@Override
				public void registerWriterFactory(
						String id,
						TweedReaderWriterProvider.ReaderWriterFactory<TweedEntryWriter<?, ?>> writerFactory
				) {
					if (writerFactories.putIfAbsent(id, writerFactory) != null && log.isWarnEnabled()) {
						log.warn(
								"Found duplicate Tweed entry writer id \"" + id + "\" in provider class {}"
										+ readerWriterProvider.getClass().getName()
						);
					}
				}
			};

			readerWriterProvider.provideReaderWriters(providerContext);
		}
	}

	@Override
	public <T> void afterWeaveEntry(ActualType<T> valueType, ConfigEntry<T> configEntry, WeavingContext context) {
		EntryReadWriteConfig entryConfig = context.annotations().getAnnotation(EntryReadWriteConfig.class);
		if (entryConfig == null) {
			return;
		}

		//noinspection rawtypes,unchecked
		readWriteExtension.setEntryReaderWriter(
				(ConfigEntry) configEntry,
				(TweedEntryReader) resolveReader(entryConfig, context),
				(TweedEntryWriter) resolveWriter(entryConfig, context)
		);
	}

	private TweedEntryReader<?, ?> resolveReader(EntryReadWriteConfig entryConfig, WeavingContext context) {
		String specText = entryConfig.reader().isEmpty() ? entryConfig.value() : entryConfig.reader();
		SerdePojoReaderWriterSpec spec = specFromText(specText, context);

		//noinspection unchecked,rawtypes
		return Optional.ofNullable(spec)
				.map(s -> resolveReaderWriterFromSpec((Class<TweedEntryReader<?, ?>>)(Object) TweedEntryReader.class, readerFactories, s, context))
				.orElse(((TweedEntryReader) TweedEntryReaderWriterImpls.NOOP_READER_WRITER));
	}

	private TweedEntryWriter<?, ?> resolveWriter(EntryReadWriteConfig entryConfig, WeavingContext context) {
		String specText = entryConfig.writer().isEmpty() ? entryConfig.value() : entryConfig.writer();
		SerdePojoReaderWriterSpec spec = specFromText(specText, context);

		//noinspection unchecked,rawtypes
		return Optional.ofNullable(spec)
				.map(s -> resolveReaderWriterFromSpec((Class<TweedEntryWriter<?, ?>>)(Object) TweedEntryWriter.class, writerFactories, s, context))
				.orElse(((TweedEntryWriter) TweedEntryReaderWriterImpls.NOOP_READER_WRITER));
	}

	private @Nullable SerdePojoReaderWriterSpec specFromText(String specText, WeavingContext context) {
		if (specText.isEmpty()) {
			return null;
		}
		try {
			return SerdePojoReaderWriterSpec.parse(specText);
		} catch (SerdePojoReaderWriterSpec.ParseException e) {
			log.warn(
					"Failed to parse definition for reader or writer on entry "
							+ Arrays.toString(context.path())
							+ ", entry will not be included in serde", e
			);
			return null;
		}
	}

	private <T> @Nullable T resolveReaderWriterFromSpec(
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
		try {
			if (factory != null) {
				instance = factory.create(arguments);
			} else {
				instance = loadClassIfExists(baseClass, spec.identifier(), arguments);
			}
		} catch (Exception e) {
			log.warn(
					"Failed to resolve reader or writer factory \"" + spec.identifier() + "\" for entry "
							+ Arrays.toString(context.path()) + ", entry will not be included in serde",
					e
			);
			return null;
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
}
