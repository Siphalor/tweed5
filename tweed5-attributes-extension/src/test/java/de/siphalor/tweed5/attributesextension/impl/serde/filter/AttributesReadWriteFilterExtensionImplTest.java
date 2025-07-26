package de.siphalor.tweed5.attributesextension.impl.serde.filter;

import de.siphalor.tweed5.attributesextension.api.AttributesExtension;
import de.siphalor.tweed5.attributesextension.api.serde.filter.AttributesReadWriteFilterExtension;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.SimpleConfigEntry;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.hjson.HjsonLexer;
import de.siphalor.tweed5.data.hjson.HjsonReader;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.siphalor.tweed5.attributesextension.api.AttributesExtension.attribute;
import static de.siphalor.tweed5.attributesextension.api.AttributesExtension.attributeDefault;
import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.*;
import static de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters.compoundReaderWriter;
import static de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters.stringReaderWriter;
import static de.siphalor.tweed5.testutils.MapTestUtils.sequencedMap;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;

@NullUnmarked
class AttributesReadWriteFilterExtensionImplTest {
	ConfigContainer<Map<String, Object>> configContainer;
	CompoundConfigEntry<Map<String, Object>> rootEntry;
	SimpleConfigEntry<String> firstEntry;
	SimpleConfigEntry<String> secondEntry;
	CompoundConfigEntry<Map<String, Object>> nestedEntry;
	SimpleConfigEntry<String> nestedFirstEntry;
	SimpleConfigEntry<String> nestedSecondEntry;
	AttributesReadWriteFilterExtension attributesReadWriteFilterExtension;

	@BeforeEach
	void setUp() {
		configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(AttributesReadWriteFilterExtensionImpl.class);
		configContainer.registerExtension(ReadWriteExtension.class);
		configContainer.registerExtension(AttributesExtension.class);
		configContainer.finishExtensionSetup();

		nestedFirstEntry = new SimpleConfigEntryImpl<>(configContainer, String.class)
				.apply(entryReaderWriter(stringReaderWriter()))
				.apply(attribute("type", "a"))
				.apply(attribute("sync", "true"));
		nestedSecondEntry = new SimpleConfigEntryImpl<>(configContainer, String.class)
				.apply(entryReaderWriter(stringReaderWriter()));
		//noinspection unchecked
		nestedEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				(Class<Map<String, Object>>) (Class<?>) Map.class,
				HashMap::new,
				sequencedMap(List.of(entry("first", nestedFirstEntry), entry("second", nestedSecondEntry)))
		)
				.apply(entryReaderWriter(compoundReaderWriter()))
				.apply(attributeDefault("type", "c"));

		firstEntry = new SimpleConfigEntryImpl<>(configContainer, String.class)
				.apply(entryReaderWriter(stringReaderWriter()))
				.apply(attribute("type", "a"))
				.apply(attribute("sync", "true"));
		secondEntry = new SimpleConfigEntryImpl<>(configContainer, String.class)
				.apply(entryReaderWriter(stringReaderWriter()));

		//noinspection unchecked
		rootEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				(Class<Map<String, Object>>) (Class<?>) Map.class,
				HashMap::new,
				sequencedMap(List.of(
						entry("first", firstEntry),
						entry("second", secondEntry),
						entry("nested", nestedEntry)
				))
		)
				.apply(entryReaderWriter(compoundReaderWriter()))
				.apply(attributeDefault("type", "b"));

		configContainer.attachTree(rootEntry);

		attributesReadWriteFilterExtension = configContainer.extension(AttributesReadWriteFilterExtension.class)
				.orElseThrow();

		attributesReadWriteFilterExtension.markAttributeForFiltering("type");
		attributesReadWriteFilterExtension.markAttributeForFiltering("sync");

		configContainer.initialize();
	}

	@ParameterizedTest
	@CsvSource(quoteCharacter = '`', textBlock = """
			a,`{
			\tfirst: 1st
			\tnested: {
			\t\tfirst: n 1st
			\t}
			}
			`
			b,`{
			\tsecond: 2nd
			}
			`
			c,`{
			\tnested: {
			\t\tsecond: n 2nd
			\t}
			}
			`
			"""
	)
	void writeWithType(String type, String serialized) {
		var writer = new StringWriter();
		configContainer.rootEntry().apply(write(
				new HjsonWriter(writer, new HjsonWriter.Options()),
				Map.of("first", "1st", "second", "2nd", "nested", Map.of("first", "n 1st", "second", "n 2nd")),
				patchwork -> attributesReadWriteFilterExtension.addFilter(patchwork, "type" , type)
		));

		assertThat(writer.toString()).isEqualTo(serialized);
	}

	@Test
	void writeWithSync() {
		var writer = new StringWriter();
		configContainer.rootEntry().apply(write(
				new HjsonWriter(writer, new HjsonWriter.Options()),
				Map.of("first", "1st", "second", "2nd", "nested", Map.of("first", "n 1st", "second", "n 2nd")),
				patchwork -> attributesReadWriteFilterExtension.addFilter(patchwork, "sync" , "true")
		));

		assertThat(writer.toString()).isEqualTo("""
				{
				\tfirst: 1st
				\tnested: {
				\t\tfirst: n 1st
				\t}
				}
				""");
	}

	@Test
	void readWithType() {
		HjsonReader reader = new HjsonReader(new HjsonLexer(new StringReader("""
				{
				first: 1st
				second: 2nd
				nested: {
				first: n 1st
				second: n 2nd
				}
				}
				""")));
		Map<String, Object> readValue = configContainer.rootEntry().call(read(
				reader,
				patchwork -> attributesReadWriteFilterExtension.addFilter(patchwork, "type", "a")
		));

		assertThat(readValue)
				.containsEntry("first", "1st")
				.containsEntry("second", null)
				.hasEntrySatisfying(
						"nested", nested -> assertThat(nested)
								.asInstanceOf(map(String.class, Object.class))
								.containsEntry("first", "n 1st")
								.containsEntry("second", null)
				);
	}
}
