package de.siphalor.tweed5.data.extension.impl;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.core.api.middleware.DefaultMiddlewareContainer;
import de.siphalor.tweed5.data.extension.api.*;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteContextExtensionsData;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataWriteException;
import de.siphalor.tweed5.dataapi.api.TweedDataWriter;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkClassCreator;
import de.siphalor.tweed5.patchwork.impl.PatchworkClass;
import de.siphalor.tweed5.patchwork.impl.PatchworkClassGenerator;
import lombok.Setter;
import lombok.Value;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ReadWriteExtensionImpl implements ReadWriteExtension {

	private RegisteredExtensionData<EntryExtensionsData, ReadWriteEntryDataExtension> readWriteEntryDataExtension;
	private DefaultMiddlewareContainer<TweedEntryReader<?, ?>> entryReaderMiddlewareContainer;
	private DefaultMiddlewareContainer<TweedEntryWriter<?, ?>> entryWriterMiddlewareContainer;
	private Map<Class<?>, RegisteredExtensionDataImpl<ReadWriteContextExtensionsData, ?>> readWriteContextExtensionsDataClasses;
	private PatchworkClass<ReadWriteContextExtensionsData> readWriteContextExtensionsDataPatchwork;

	@Override
	public String getId() {
		return "read-write";
	}

	@Override
	public void setup(TweedExtensionSetupContext context) {
		readWriteEntryDataExtension = context.registerEntryExtensionData(ReadWriteEntryDataExtension.class);
		context.registerEntryExtensionData(EntryReaderWriterDefinition.class);

		Collection<TweedExtension> extensions = context.configContainer().extensions();

		readWriteContextExtensionsDataClasses = new HashMap<>(extensions.size());

		ReadWriteExtensionSetupContext setupContext = new ReadWriteExtensionSetupContext() {
			@Override
			public <E> RegisteredExtensionData<ReadWriteContextExtensionsData, E> registerReadWriteContextExtensionData(Class<E> extensionDataClass) {
				if (readWriteContextExtensionsDataClasses.containsKey(extensionDataClass)) {
					throw new IllegalArgumentException("Context extension " + extensionDataClass.getName() + " is already registered");
				}
				RegisteredExtensionDataImpl<ReadWriteContextExtensionsData, E> registeredExtensionData = new RegisteredExtensionDataImpl<>();
				readWriteContextExtensionsDataClasses.put(extensionDataClass, registeredExtensionData);
				return registeredExtensionData;
			}
		};

		entryReaderMiddlewareContainer = new DefaultMiddlewareContainer<>();
		entryWriterMiddlewareContainer = new DefaultMiddlewareContainer<>();

		for (TweedExtension extension : extensions) {
			if (extension instanceof ReadWriteRelatedExtension) {
				ReadWriteRelatedExtension rwExtension = (ReadWriteRelatedExtension) extension;

				rwExtension.setupReadWriteExtension(setupContext);

				if (rwExtension.entryReaderMiddleware() != null) {
					entryReaderMiddlewareContainer.register(rwExtension.entryReaderMiddleware());
				}
				if (rwExtension.entryWriterMiddleware() != null) {
					entryWriterMiddlewareContainer.register(rwExtension.entryWriterMiddleware());
				}
			}
		}

		entryReaderMiddlewareContainer.seal();
		entryWriterMiddlewareContainer.seal();

		PatchworkClassCreator<ReadWriteContextExtensionsData> patchworkClassCreator = PatchworkClassCreator.<ReadWriteContextExtensionsData>builder()
				.patchworkInterface(ReadWriteContextExtensionsData.class)
				.classPackage("de.siphalor.tweed5.data.extension.generated")
				.classPrefix("ReadWriteContextExtensionsData$")
				.build();

		try {
			readWriteContextExtensionsDataPatchwork = patchworkClassCreator.createClass(readWriteContextExtensionsDataClasses.keySet());
		} catch (PatchworkClassGenerator.GenerationException e) {
			throw new IllegalStateException("Failed to generate read write context extensions' data patchwork class", e);
		}
	}

	@Override
	public void initEntry(ConfigEntry<?> configEntry) {
		TweedEntryReader<?, ?> baseReader;
		TweedEntryWriter<?, ?> baseWriter;
		if (configEntry.extensionsData().isPatchworkPartSet(EntryReaderWriterDefinition.class)) {
			EntryReaderWriterDefinition rwDefintion = (EntryReaderWriterDefinition) configEntry.extensionsData();
			baseReader = rwDefintion.reader();
			baseWriter = rwDefintion.writer();
		} else {
			baseReader = TweedEntryReaderWriterImpls.NOOP_READER_WRITER;
			baseWriter = TweedEntryReaderWriterImpls.NOOP_READER_WRITER;
		}

		readWriteEntryDataExtension.set(configEntry.extensionsData(), new ReadWriteEntryDataExtensionImpl(
				entryReaderMiddlewareContainer.process(baseReader),
				entryWriterMiddlewareContainer.process(baseWriter)
		));
	}

	@Override
	public ReadWriteContextExtensionsData createReadWriteContextExtensionsData() {
		try {
			return (ReadWriteContextExtensionsData) readWriteContextExtensionsDataPatchwork.constructor().invoke();
		} catch (Throwable e) {
			throw new IllegalStateException("Failed to instantiate read write context extensions' data", e);
		}
	}

	@Override
	public <T> T read(TweedDataReader reader, ConfigEntry<T> entry, ReadWriteContextExtensionsData contextExtensionsData) throws TweedEntryReadException {
		try {
			return getReaderChain(entry).read(reader, entry, new TweedReadWriteContextImpl(contextExtensionsData));
		} catch (TweedDataReadException e) {
			throw new TweedEntryReadException("Failed to read entry", e);
		}
	}

	@Override
	public <T> void write(TweedDataWriter writer, T value, ConfigEntry<T> entry, ReadWriteContextExtensionsData contextExtensionsData) throws TweedEntryWriteException {
		try {
			getWriterChain(entry).write(writer, value, entry, new TweedReadWriteContextImpl(contextExtensionsData));
		} catch (TweedDataWriteException e) {
			throw new TweedEntryWriteException("Failed to write entry", e);
		}
	}

	@Value
	private static class ReadWriteEntryDataExtensionImpl implements ReadWriteEntryDataExtension {
		TweedEntryReader<?, ?> entryReaderChain;
		TweedEntryWriter<?, ?> entryWriterChain;
	}

	@Setter
	private static class RegisteredExtensionDataImpl<U extends Patchwork<U>, E> implements RegisteredExtensionData<U, E> {
		private MethodHandle setter;

		@Override
		public void set(U patchwork, E extension) {
			try {
				setter.invokeWithArguments(patchwork, extension);
			} catch (Throwable e) {
				throw new IllegalStateException(e);
			}
		}
	}

	static <T> TweedEntryReader<T, ConfigEntry<T>> getReaderChain(ConfigEntry<T> elementEntry) {
		//noinspection unchecked
		return (TweedEntryReader<T, ConfigEntry<T>>) ((ReadWriteEntryDataExtension) elementEntry.extensionsData()).entryReaderChain();
	}

	static <T> TweedEntryWriter<T, ConfigEntry<T>> getWriterChain(ConfigEntry<T> elementEntry) {
		//noinspection unchecked
		return (TweedEntryWriter<T, ConfigEntry<T>>) ((ReadWriteEntryDataExtension) elementEntry.extensionsData()).entryWriterChain();
	}
}
