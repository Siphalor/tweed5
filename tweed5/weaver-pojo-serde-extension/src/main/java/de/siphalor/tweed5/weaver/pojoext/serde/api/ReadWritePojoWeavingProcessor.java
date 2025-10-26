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
import de.siphalor.tweed5.weaver.pojoext.serde.impl.ReaderWriterLoader;
import lombok.extern.apachecommons.CommonsLog;
import org.jspecify.annotations.Nullable;

import java.util.*;

@CommonsLog
public class ReadWritePojoWeavingProcessor implements TweedPojoWeavingExtension {
	private final ReadWriteExtension readWriteExtension;
	private final ReaderWriterLoader readerWriterLoader = new ReaderWriterLoader();

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
		serviceLoader.forEach(readerWriterLoader::load);
	}

	@Override
	public <T> void afterWeaveEntry(ActualType<T> valueType, ConfigEntry<T> configEntry, WeavingContext context) {
		EntryReadWriteConfig entryConfig = context.annotations().getAnnotation(EntryReadWriteConfig.class);
		if (entryConfig == null) {
			return;
		}

		try {
			//noinspection rawtypes,unchecked
			readWriteExtension.setEntryReaderWriter(
					(ConfigEntry) configEntry,
					(TweedEntryReader) resolveReader(entryConfig),
					(TweedEntryWriter) resolveWriter(entryConfig)
			);
		} catch (Exception e) {
			log.warn(
					"Unexpected exception while resolving serde reader and writer for "
							+ Arrays.toString(context.path())
							+ ". Entry will not be included in serde.",
					e
			);
		}
	}

	private TweedEntryReader<?, ?> resolveReader(EntryReadWriteConfig entryConfig) {
		String specText = entryConfig.reader().isEmpty() ? entryConfig.value() : entryConfig.reader();
		SerdePojoReaderWriterSpec spec = specFromText(specText);
		if (spec == null) {
			return TweedEntryReaderWriterImpls.NOOP_READER_WRITER;
		}

		return readerWriterLoader.resolveReaderFromSpec(spec);
	}

	private TweedEntryWriter<?, ?> resolveWriter(EntryReadWriteConfig entryConfig) {
		String specText = entryConfig.writer().isEmpty() ? entryConfig.value() : entryConfig.writer();
		SerdePojoReaderWriterSpec spec = specFromText(specText);
		if (spec == null) {
			return TweedEntryReaderWriterImpls.NOOP_READER_WRITER;
		}

		return readerWriterLoader.resolveWriterFromSpec(spec);
	}

	private @Nullable SerdePojoReaderWriterSpec specFromText(String specText) {
		if (specText.isEmpty()) {
			return null;
		}
		try {
			return SerdePojoReaderWriterSpec.parse(specText);
		} catch (SerdePojoReaderWriterSpec.ParseException e) {
			throw new IllegalArgumentException(
					"Failed to parse definition for reader or writer: \"" + specText + "\"",
					e
			);
		}
	}
}
