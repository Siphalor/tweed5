package de.siphalor.tweed5.serde.extension.impl;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.CollectionConfigEntry;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.SimpleConfigEntry;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.CollectionConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.serde.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.serde.hjson.HjsonLexer;
import de.siphalor.tweed5.serde.hjson.HjsonReader;
import de.siphalor.tweed5.serde.hjson.HjsonWriter;
import de.siphalor.tweed5.serde_api.api.TweedDataVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;

import static de.siphalor.tweed5.serde.extension.api.ReadWriteExtension.entryReaderWriter;
import static de.siphalor.tweed5.serde.extension.api.readwrite.TweedEntryReaderWriters.*;
import static de.siphalor.tweed5.testutils.generic.MapTestUtils.sequencedMap;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ReadWriteExtensionImplTest {
	private final StringWriter stringWriter = new StringWriter();
	private final ConfigContainer<Map<String, Object>> configContainer = new DefaultConfigContainer<>();
	private CompoundConfigEntry<Map<String, Object>> rootEntry;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		configContainer.registerExtension(ReadWriteExtension.DEFAULT);
		configContainer.finishExtensionSetup();

		SimpleConfigEntry<Integer> intEntry = new SimpleConfigEntryImpl<>(configContainer, Integer.class)
				.apply(entryReaderWriter(intReaderWriter()));

		CollectionConfigEntry<Boolean, List<Boolean>> listEntry = new CollectionConfigEntryImpl<>(
				configContainer,
				(Class<List<Boolean>>) (Class<?>) List.class,
				ArrayList::new,
				new SimpleConfigEntryImpl<>(configContainer, Boolean.class)
						.apply(entryReaderWriter(booleanReaderWriter()))
		).apply(entryReaderWriter(collectionReaderWriter()));

		rootEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				((Class<Map<String, Object>>) (Class<?>) Map.class),
				LinkedHashMap::new,
				sequencedMap(List.of(
						entry("int", intEntry),
						entry("list", listEntry)
				))
		).apply(entryReaderWriter(compoundReaderWriter()));

		configContainer.attachTree(rootEntry);
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
		var result = assertDoesNotThrow(() -> readWriteExtension.read(
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

		assertThat(result.isFailed()).isFalse();
		assertThat(result.hasValue()).isTrue();
		assertThat(result.value()).isEqualTo(Map.of(
				"int", 123,
				"list", Arrays.asList(true, false, true)
		));
	}

	private TweedDataVisitor setupWriter(Function<Writer, TweedDataVisitor> writerFactory) {
		return writerFactory.apply(stringWriter);
	}
}
