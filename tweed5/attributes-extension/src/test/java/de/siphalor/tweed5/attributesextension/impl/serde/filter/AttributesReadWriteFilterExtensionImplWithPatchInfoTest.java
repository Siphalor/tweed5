package de.siphalor.tweed5.attributesextension.impl.serde.filter;

import de.siphalor.tweed5.attributesextension.api.serde.filter.AttributesReadWriteFilterExtension;
import de.siphalor.tweed5.attributesextension.impl.AttributesExtensionImpl;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.serde.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.serde.extension.api.read.result.TweedReadResult;
import de.siphalor.tweed5.serde.hjson.HjsonLexer;
import de.siphalor.tweed5.serde.hjson.HjsonReader;
import de.siphalor.tweed5.defaultextensions.patch.api.PatchExtension;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.siphalor.tweed5.attributesextension.api.AttributesExtension.attribute;
import static de.siphalor.tweed5.serde.extension.api.ReadWriteExtension.entryReaderWriter;
import static de.siphalor.tweed5.serde.extension.api.readwrite.TweedEntryReaderWriters.compoundReaderWriter;
import static de.siphalor.tweed5.serde.extension.api.readwrite.TweedEntryReaderWriters.stringReaderWriter;
import static de.siphalor.tweed5.testutils.generic.MapTestUtils.sequencedMap;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;

public class AttributesReadWriteFilterExtensionImplWithPatchInfoTest {
	@Test
	@SneakyThrows
	void testOrder() {
		ConfigContainer<Map<String, Object>> configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(AttributesReadWriteFilterExtensionImpl.class);
		configContainer.registerExtension(ReadWriteExtension.class);
		configContainer.registerExtension(AttributesExtensionImpl.class);
		configContainer.registerExtension(PatchExtension.class);
		configContainer.finishExtensionSetup();

		var firstEntry = new SimpleConfigEntryImpl<>(configContainer, String.class)
				.apply(entryReaderWriter(stringReaderWriter()))
				.apply(attribute("type", "a"));
		var secondEntry = new SimpleConfigEntryImpl<>(configContainer, String.class)
				.apply(entryReaderWriter(stringReaderWriter()));
		var rootEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				(Class<Map<String, Object>>) (Class<?>) Map.class,
				HashMap::new,
				sequencedMap(List.of(entry("first", firstEntry), entry("second", secondEntry)))
		)
				.apply(entryReaderWriter(compoundReaderWriter()));

		configContainer.attachTree(rootEntry);

		var readWriteFilterExtension = configContainer.extension(AttributesReadWriteFilterExtension.class).orElseThrow();
		readWriteFilterExtension.markAttributeForFiltering("type");

		configContainer.initialize();

		var readWriteExtension = configContainer.extension(ReadWriteExtension.class).orElseThrow();
		var patchExtension = configContainer.extension(PatchExtension.class).orElseThrow();
		var filterExtension = configContainer.extension(AttributesReadWriteFilterExtension.class).orElseThrow();

		var readExtData = readWriteExtension.createReadWriteContextExtensionsData();
		var patchInfo = patchExtension.collectPatchInfo(readExtData);
		filterExtension.addFilter(readExtData, "type", "a");

		var readResult = readWriteExtension.read(
				new HjsonReader(new HjsonLexer(new StringReader("""
						{
							"first": "FIRST",
							"second": "SECOND"
						}
						"""))),
				configContainer.rootEntry(),
				readExtData
		);

		assertThat(readResult)
				.extracting(TweedReadResult::value)
				.asInstanceOf(map(String.class, Object.class))
				.isEqualTo(Map.of("first", "FIRST"));
		assertThat(patchInfo.containsEntry(firstEntry)).isTrue();
		assertThat(patchInfo.containsEntry(secondEntry)).isFalse();
	}
}
