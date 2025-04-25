package de.siphalor.tweed5.data.extension.impl;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.CollectionConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.data.extension.api.EntryReaderWriterDefinition;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriter;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters;
import de.siphalor.tweed5.data.hjson.HjsonLexer;
import de.siphalor.tweed5.data.hjson.HjsonReader;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReadWriteExtensionImplTest {
	private final StringWriter stringWriter = new StringWriter();
	private final ConfigContainer<Map<String, Object>> configContainer = new DefaultConfigContainer<>();
	private StaticMapCompoundConfigEntryImpl<Map<String, Object>> rootEntry;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		configContainer.registerExtension(ReadWriteExtension.DEFAULT);
		configContainer.finishExtensionSetup();

		rootEntry = new StaticMapCompoundConfigEntryImpl<>(((Class<Map<String, Object>>) (Class<?>) Map.class), LinkedHashMap::new);

		SimpleConfigEntryImpl<Integer> intEntry = new SimpleConfigEntryImpl<>(Integer.class);
		rootEntry.addSubEntry("int", intEntry);

		CollectionConfigEntryImpl<Boolean, List<Boolean>> listEntry = new CollectionConfigEntryImpl<>((Class<List<Boolean>>) (Class<?>) List.class, ArrayList::new);
		rootEntry.addSubEntry("list", listEntry);

		SimpleConfigEntryImpl<Boolean> booleanEntry = new SimpleConfigEntryImpl<>(Boolean.class);
		listEntry.elementEntry(booleanEntry);

		configContainer.attachAndSealTree(rootEntry);

		RegisteredExtensionData<EntryExtensionsData, EntryReaderWriterDefinition> readerWriterData = (RegisteredExtensionData<EntryExtensionsData, EntryReaderWriterDefinition>) configContainer.entryDataExtensions().get(EntryReaderWriterDefinition.class);
		readerWriterData.set(rootEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.compoundReaderWriter()));
		readerWriterData.set(intEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.intReaderWriter()));
		readerWriterData.set(listEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.collectionReaderWriter()));
		readerWriterData.set(booleanEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.booleanReaderWriter()));

		configContainer.initialize();
	}


	@Test
	void write() {
		Map<String, Object> value = new HashMap<>();
		value.put("int", 123);
		value.put("list", Arrays.asList(true, false, true));

		ReadWriteExtension readWriteExtension = configContainer.extension(ReadWriteExtension.class).orElseThrow();
		assertDoesNotThrow(() -> readWriteExtension.write(
				setupWriter(writer -> new HjsonWriter(writer, new HjsonWriter.Options())),
				value,
				rootEntry,
				readWriteExtension.createReadWriteContextExtensionsData()
		));

		assertThat(stringWriter.toString()).isEqualTo("""
				{
				\tint: 123
				\tlist: [
				\t\ttrue
				\t\tfalse
				\t\ttrue
				\t]
				}
				"""
		);
	}

	@Test
	void read() {
		ReadWriteExtension readWriteExtension = configContainer.extension(ReadWriteExtension.class).orElseThrow();
		Map<String, Object> result = assertDoesNotThrow(() -> readWriteExtension.read(
				new HjsonReader(new HjsonLexer(new StringReader("""
						{
						\tint: 123
						\tlist: [
						\t\ttrue
						\t\tfalse
						\t\ttrue
						\t]
						}
						"""))),
				rootEntry,
				readWriteExtension.createReadWriteContextExtensionsData()
		));

		assertEquals(2, result.size());
		assertEquals(123, result.get("int"));
		assertEquals(Arrays.asList(true, false, true), result.get("list"));
	}

	private TweedDataVisitor setupWriter(Function<Writer, TweedDataVisitor> writerFactory) {
		return writerFactory.apply(stringWriter);
	}

	@RequiredArgsConstructor
	private static class TrivialEntryReaderWriterDefinition implements EntryReaderWriterDefinition {
		private final TweedEntryReaderWriter<?, ?> readerWriter;

		@Override
		public TweedEntryReader<?, ?> reader() {
			return readerWriter;
		}

		@Override
		public TweedEntryWriter<?, ?> writer() {
			return readerWriter;
		}
	}
}
