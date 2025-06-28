package de.siphalor.tweed5.data.extension.impl;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.core.api.middleware.DefaultMiddlewareContainer;
import de.siphalor.tweed5.data.extension.api.*;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.TweedDataWriteException;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkFactory;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import lombok.Data;
import lombok.val;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@AutoService(ReadWriteExtension.class)
public class ReadWriteExtensionImpl implements ReadWriteExtension {
	private final ConfigContainer<?> configContainer;
	private final PatchworkPartAccess<CustomEntryData> customEntryDataAccess;
	private DefaultMiddlewareContainer<TweedEntryReader<?, ?>>
			entryReaderMiddlewareContainer
			= new DefaultMiddlewareContainer<>();
	private DefaultMiddlewareContainer<TweedEntryWriter<?, ?>>
			entryWriterMiddlewareContainer
			= new DefaultMiddlewareContainer<>();
	private @Nullable PatchworkFactory readWriteContextPatchworkFactory;

	public ReadWriteExtensionImpl(ConfigContainer<?> configContainer, TweedExtensionSetupContext context) {
		this.configContainer = configContainer;
		this.customEntryDataAccess = context.registerEntryExtensionData(CustomEntryData.class);
	}

	@Override
	public String getId() {
		return "read-write";
	}

	@Override
	public void extensionsFinalized() {
		Collection<TweedExtension> extensions = configContainer.extensions();

		PatchworkFactory.Builder readWriteContextPatchworkFactorBuilder = PatchworkFactory.builder();
		ReadWriteExtensionSetupContext setupContext = readWriteContextPatchworkFactorBuilder::registerPart;

		entryReaderMiddlewareContainer = new DefaultMiddlewareContainer<>();
		entryWriterMiddlewareContainer = new DefaultMiddlewareContainer<>();

		for (TweedExtension extension : extensions) {
			if (extension instanceof ReadWriteRelatedExtension) {
				ReadWriteRelatedExtension rwExtension = (ReadWriteRelatedExtension) extension;

				rwExtension.setupReadWriteExtension(setupContext);

				val readerMiddleware = rwExtension.entryReaderMiddleware();
				if (readerMiddleware != null) {
					entryReaderMiddlewareContainer.register(readerMiddleware);
				}
				val writerMiddleware = rwExtension.entryWriterMiddleware();
				if (writerMiddleware != null) {
					entryWriterMiddlewareContainer.register(writerMiddleware);
				}
			}
		}

		readWriteContextPatchworkFactory = readWriteContextPatchworkFactorBuilder.build();

		entryReaderMiddlewareContainer.seal();
		entryWriterMiddlewareContainer.seal();
	}

	@Override
	public @Nullable <T, C extends ConfigEntry<T>> TweedEntryReader<T, C> getDefinedEntryReader(ConfigEntry<T> entry) {
		CustomEntryData customEntryData = entry.extensionsData().get(customEntryDataAccess);
		if (customEntryData == null) {
			return null;
		}
		//noinspection unchecked
		return (TweedEntryReader<T, C>) customEntryData.readerDefinition();
	}

	@Override
	public @Nullable <T, C extends ConfigEntry<T>> TweedEntryWriter<T, C> getDefinedEntryWriter(ConfigEntry<T> entry) {
		CustomEntryData customEntryData = entry.extensionsData().get(customEntryDataAccess);
		if (customEntryData == null) {
			return null;
		}
		//noinspection unchecked
		return (TweedEntryWriter<T, C>) customEntryData.writerDefinition();
	}

	@Override
	public <T, C extends ConfigEntry<T>> void setEntryReaderWriter(
			ConfigEntry<T> entry,
			TweedEntryReader<T, C> entryReader,
			TweedEntryWriter<T, C> entryWriter
	) {
		CustomEntryData customEntryData = getOrCreateCustomEntryData(entry);
		customEntryData.readerDefinition(entryReader);
		customEntryData.writerDefinition(entryWriter);
	}

	@Override
	public <T, C extends ConfigEntry<T>> void setEntryReader(ConfigEntry<T> entry, TweedEntryReader<T, C> entryReader) {
		getOrCreateCustomEntryData(entry).readerDefinition(entryReader);
	}

	@Override
	public <T, C extends ConfigEntry<T>> void setEntryWriter(ConfigEntry<T> entry, TweedEntryWriter<T, C> entryWriter) {
		getOrCreateCustomEntryData(entry).writerDefinition(entryWriter);
	}

	@Override
	public void initEntry(ConfigEntry<?> configEntry) {
		CustomEntryData customEntryData = getOrCreateCustomEntryData(configEntry);
		customEntryData.readerChain(entryReaderMiddlewareContainer.process(customEntryData.readerDefinition()));
		customEntryData.writerChain(entryWriterMiddlewareContainer.process(customEntryData.writerDefinition()));
	}

	private CustomEntryData getOrCreateCustomEntryData(ConfigEntry<?> entry) {
		CustomEntryData entryData = entry.extensionsData().get(customEntryDataAccess);
		if (entryData == null) {
			entryData = new CustomEntryData();
			entry.extensionsData().set(customEntryDataAccess, entryData);
		}
		return entryData;
	}

	@Override
	public Patchwork createReadWriteContextExtensionsData() {
		assert readWriteContextPatchworkFactory != null;
		return readWriteContextPatchworkFactory.create();
	}

	public <T extends @Nullable Object> T read(
			TweedDataReader reader,
			ConfigEntry<T> entry,
			Patchwork contextExtensionsData
	) throws TweedEntryReadException {
		TweedReadContext context = new TweedReadWriteContextImpl(this, contextExtensionsData);
		return getReaderChain(entry).read(reader, entry, context);
	}

	@Override
	public <T extends @Nullable Object> void write(
			TweedDataVisitor writer,
			@Nullable T value,
			ConfigEntry<T> entry,
			Patchwork contextExtensionsData
	) throws TweedEntryWriteException {
		TweedWriteContext context = new TweedReadWriteContextImpl(this, contextExtensionsData);
		try {
			getWriterChain(entry).write(writer, value, entry, context);
		} catch (TweedDataWriteException e) {
			throw new TweedEntryWriteException("Failed to write entry", e, context);
		}
	}

	@Data
	private static class CustomEntryData {
		private TweedEntryReader<?, ?> readerDefinition = TweedEntryReaderWriterImpls.NOOP_READER_WRITER;
		private TweedEntryWriter<?, ?> writerDefinition = TweedEntryReaderWriterImpls.NOOP_READER_WRITER;
		private TweedEntryReader<?, ?> readerChain;
		private TweedEntryWriter<?, ?> writerChain;
	}

	@Override
	public <T, C extends ConfigEntry<T>> TweedEntryReader<T, C> getReaderChain(C entry) {
		//noinspection unchecked
		return (TweedEntryReader<T, C>) entry.extensionsData().get(customEntryDataAccess).readerChain();
	}

	@Override
	public <T, C extends ConfigEntry<T>> TweedEntryWriter<T, C> getWriterChain(C entry) {
		//noinspection unchecked
		return (TweedEntryWriter<T, C>) entry.extensionsData().get(customEntryDataAccess).writerChain();
	}
}
