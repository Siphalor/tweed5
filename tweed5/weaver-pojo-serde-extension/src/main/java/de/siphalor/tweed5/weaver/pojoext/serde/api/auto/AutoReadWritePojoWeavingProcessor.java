package de.siphalor.tweed5.weaver.pojoext.serde.api.auto;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.NullableConfigEntry;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.data.extension.api.TweedReaderWriterProvider;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters;
import de.siphalor.tweed5.data.extension.impl.TweedEntryReaderWriterImpls;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.weaving.ProtoWeavingContext;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;
import de.siphalor.tweed5.weaver.pojoext.serde.impl.ReaderWriterLoader;
import de.siphalor.tweed5.weaver.pojoext.serde.impl.SerdePojoReaderWriterSpec;
import lombok.Value;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class AutoReadWritePojoWeavingProcessor implements TweedPojoWeavingExtension {
	private final ReadWriteExtension readWriteExtension;
	private final ReaderWriterLoader readerWriterLoader = new ReaderWriterLoader();
	private @Nullable PatchworkPartAccess<CustomData> customDataAccess;

	@ApiStatus.Internal
	public AutoReadWritePojoWeavingProcessor(ConfigContainer<?> configContainer) {
		this.readWriteExtension = configContainer.extension(ReadWriteExtension.class)
				.orElseThrow(() -> new IllegalStateException(
						"You must register a " + ReadWriteExtension.class.getSimpleName()
								+ " to use the " + getClass().getSimpleName()
				));
	}

	@Override
	public void setup(SetupContext context) {
		customDataAccess = context.registerWeavingContextExtensionData(CustomData.class);

		loadProviders();
	}

	private void loadProviders() {
		ServiceLoader<TweedReaderWriterProvider> serviceLoader = ServiceLoader.load(TweedReaderWriterProvider.class);
		serviceLoader.forEach(readerWriterLoader::load);
	}

	@Override
	public <T> void beforeWeaveEntry(ActualType<T> valueType, Patchwork extensionsData, ProtoWeavingContext context) {
		assert customDataAccess != null;

		CustomData existingCustomData = extensionsData.get(customDataAccess);
		List<Mapping> existingMappings = existingCustomData == null ? Collections.emptyList() : existingCustomData.mappings();

		AutoReadWriteMapping[] mappingAnnotations = context.annotations()
				.getAnnotationsByType(AutoReadWriteMapping.class);

		if (existingCustomData == null || mappingAnnotations.length > 0) {
			List<Mapping> mappings;
			if (existingMappings.isEmpty() && mappingAnnotations.length == 0) {
				mappings = Collections.emptyList();
			} else {
				mappings = new ArrayList<>(existingMappings.size() + mappingAnnotations.length + 5);
				mappings.addAll(existingMappings);
				for (AutoReadWriteMapping mappingAnnotation : mappingAnnotations) {
					mappings.add(annotationToMapping(mappingAnnotation));
				}
			}
			extensionsData.set(customDataAccess, new CustomData(mappings));
		}
	}

	private Mapping annotationToMapping(AutoReadWriteMapping annotation) {
		return new Mapping(
				annotation.entryClasses(),
				annotation.valueClasses(),
				resolveReader(annotation.reader().isEmpty() ? annotation.spec() : annotation.reader()),
				resolveWriter(annotation.writer().isEmpty() ? annotation.spec() : annotation.writer())
		);
	}

	private TweedEntryReader<?, ?> resolveReader(String specText) {
		if (specText.isEmpty()) {
			return TweedEntryReaderWriterImpls.NOOP_READER_WRITER;
		}
		try {
			SerdePojoReaderWriterSpec spec = SerdePojoReaderWriterSpec.parse(specText);
			return readerWriterLoader.resolveReaderFromSpec(spec);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Failed to parse definition for reader: \"" + specText + "\"",
					e
			);
		}
	}

	private TweedEntryWriter<?, ?> resolveWriter(String specText) {
		if (specText.isEmpty()) {
			return TweedEntryReaderWriterImpls.NOOP_READER_WRITER;
		}
		try {
			SerdePojoReaderWriterSpec spec = SerdePojoReaderWriterSpec.parse(specText);
			return readerWriterLoader.resolveWriterFromSpec(spec);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Failed to parse definition for writer: \"" + specText + "\"",
					e
			);
		}
	}

	@Override
	public <T> void afterWeaveEntry(ActualType<T> valueType, ConfigEntry<T> configEntry, WeavingContext context) {
		if (configEntry instanceof NullableConfigEntry) {
			readWriteExtension.setEntryReaderWriter(
					configEntry,
					TweedEntryReaderWriters.nullableReaderWriter(),
					TweedEntryReaderWriters.nullableReaderWriter()
			);
			return;
		}

		assert customDataAccess != null;
		CustomData customData = context.extensionsData().get(customDataAccess);
		if (customData == null || customData.mappings().isEmpty()) {
			return;
		}

		Mapping mapping = determineMapping(customData, configEntry, valueType);
		if (mapping == null) {
			return;
		}

		//noinspection unchecked
		readWriteExtension.setEntryReaderWriter(
				(ConfigEntry<Object>) configEntry,
				(TweedEntryReader<Object, @org.jspecify.annotations.NonNull ConfigEntry<Object>>) mapping.reader(),
				(TweedEntryWriter<Object, @org.jspecify.annotations.NonNull ConfigEntry<Object>>) mapping.writer()
		);
	}

	private @Nullable Mapping determineMapping(CustomData customData, ConfigEntry<?> configEntry, ActualType<?> valueType) {
		Mapping strictMapping = customData.strictMappings()
				.get(new MappingStrictKey(configEntry.getClass(), valueType.declaredType()));
		if (strictMapping != null) {
			return strictMapping;
		}

		for (int i = customData.mappings().size() - 1; i >= 0; i--) {
			Mapping mapping = customData.mappings().get(i);
			if (mappingMatches(mapping, configEntry, valueType)) {

				customData.strictMappings.put(
						new MappingStrictKey(configEntry.getClass(), valueType.declaredType()),
						mapping
				);

				return mapping;
			}
		}

		return null;
	}

	private boolean mappingMatches(Mapping mapping, ConfigEntry<?> configEntry, ActualType<?> valueType) {
		return anyClassMatches(mapping.entryClasses, configEntry.getClass())
				&& anyClassMatches(mapping.valueClasses, valueType.declaredType());
	}

	private boolean anyClassMatches(Class<?>[] haystack, Class<?> needle) {
		for (Class<?> hay : haystack) {
			if (hay.isAssignableFrom(needle)) {
				return true;
			}
		}
		return false;
	}

	@Value
	private static class CustomData {
		List<Mapping> mappings;
		Map<MappingStrictKey, Mapping> strictMappings = new HashMap<>();
	}

	@Value
	private static class Mapping {
		Class<?>[] entryClasses;
		Class<?>[] valueClasses;
		TweedEntryReader<?, ?> reader;
		TweedEntryWriter<?, ?> writer;
	}

	@Value
	private static class MappingStrictKey {
		Class<?> entryClass;
		Class<?> valueClass;
	}
}
