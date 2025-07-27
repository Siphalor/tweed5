package de.siphalor.tweed5.defaultextensions.patch.impl;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.CollectionConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.hjson.HjsonLexer;
import de.siphalor.tweed5.data.hjson.HjsonReader;
import de.siphalor.tweed5.defaultextensions.patch.api.PatchExtension;
import de.siphalor.tweed5.defaultextensions.patch.api.PatchInfo;
import lombok.SneakyThrows;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.StringReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.entryReaderWriter;
import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.read;
import static de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters.*;
import static de.siphalor.tweed5.testutils.MapTestUtils.sequencedMap;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@NullUnmarked
class PatchExtensionImplTest {
	private ConfigContainer<Map<String, Object>> configContainer;
	private CompoundConfigEntry<Map<String, Object>> rootEntry;

	private Map<String, ConfigEntry<?>> entries;

	@SuppressWarnings({"unchecked", "rawtypes"})
	@BeforeEach
	void setUp() {
		configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(ReadWriteExtension.class);
		configContainer.registerExtension(PatchExtension.class);
		configContainer.finishExtensionSetup();

		var int1Entry = new SimpleConfigEntryImpl<>(configContainer, Integer.class)
				.apply(entryReaderWriter(intReaderWriter()));
		var int2Entry = new SimpleConfigEntryImpl<>(configContainer, Integer.class)
				.apply(entryReaderWriter(
						nullableReader(intReaderWriter()),
						nullableWriter(intReaderWriter())
				));
		var listEntry = new CollectionConfigEntryImpl<>(
				configContainer,
				(Class<List<Integer>>)(Class) List.class,
				ArrayList::new,
				new SimpleConfigEntryImpl<>(configContainer, Integer.class)
						.apply(entryReaderWriter(intReaderWriter()))
		)
				.apply(entryReaderWriter(
						nullableReader(collectionReaderWriter()),
						nullableWriter(collectionReaderWriter())
				));

		var nestedInt1Entry = new SimpleConfigEntryImpl<>(configContainer, Integer.class)
				.apply(entryReaderWriter(intReaderWriter()));
		var nestedInt2Entry = new SimpleConfigEntryImpl<>(configContainer, Integer.class)
				.apply(entryReaderWriter(intReaderWriter()));
		var compoundEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				(Class<Map<String, Object>>)(Class) Map.class,
				HashMap::new,
				sequencedMap(List.of(
						entry("int1", nestedInt1Entry),
						entry("int2", nestedInt2Entry)
				))
		)
				.apply(entryReaderWriter(
						nullableReader(compoundReaderWriter()),
						nullableWriter(compoundReaderWriter())
				));

		rootEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				(Class<Map<String, Object>>)(Class) Map.class,
				HashMap::new,
				sequencedMap(List.of(
						entry("int1", int1Entry),
						entry("int2", int2Entry),
						entry("list", listEntry),
						entry("compound", compoundEntry)
				))
		)
				.apply(entryReaderWriter(compoundReaderWriter()));

		configContainer.attachTree(rootEntry);
		configContainer.initialize();

		entries = Map.of(
				"root", rootEntry,
				"int1", int1Entry,
				"int2", int2Entry,
				"list", listEntry,
				"compound", compoundEntry,
				"compound.int1", nestedInt1Entry,
				"compound.int2", nestedInt2Entry
		);
	}

	@SneakyThrows
	@ParameterizedTest
	@MethodSource("collectPathInfoParams")
	void collectPatchInfo(String patch, Set<String> expectedEntries) {
		var reader = new HjsonReader(new HjsonLexer(new StringReader(patch)));

		PatchExtension patchExtension = configContainer.extension(PatchExtension.class).orElseThrow();

		var patchInfo = new AtomicReference<@Nullable PatchInfo>();
		rootEntry.call(read(
				reader, extensionsData ->
						patchInfo.set(patchExtension.collectPatchInfo(extensionsData))
		));
		assertThat(patchInfo.get()).isNotNull();

		entries.forEach((key, entry) ->
			assertThat(Objects.requireNonNull(patchInfo.get()).containsEntry(entry))
					.as("PatchInfo should contain entry %s", key)
					.isEqualTo(expectedEntries.contains(key))
		);
	}

	static Stream<Arguments> collectPathInfoParams() {
		return Stream.of(
				arguments("{int1:123}", Set.of("root", "int1")),
				arguments("{compound:{int2:123}}", Set.of("root", "compound", "compound.int2"))
		);
	}

	@ParameterizedTest
	@MethodSource("patchParams")
	void patch(Map<String, Object> baseValue, String patch, Map<String, Object> expectedValue) {
		var reader = new HjsonReader(new HjsonLexer(new StringReader(patch)));

		PatchExtension patchExtension = configContainer.extension(PatchExtension.class).orElseThrow();

		var patchInfo = new AtomicReference<@Nullable PatchInfo>();
		Map<String, Object> patchValue = rootEntry.call(read(
				reader, extensionsData ->
						patchInfo.set(patchExtension.collectPatchInfo(extensionsData))
		));

		Map<String, Object> resultValue = patchExtension.patch(
				rootEntry,
				baseValue,
				patchValue,
				Objects.requireNonNull(patchInfo.get())
		);

		assertThat(resultValue).isEqualTo(expectedValue).isSameAs(baseValue);
	}

	static Stream<Arguments> patchParams() {
		Map<String, Object> mapForNullCase = new LinkedHashMap<>();
		mapForNullCase.put("int2", null);
		mapForNullCase.put("list", null);
		mapForNullCase.put("compound", null);

		return Stream.of(
				argumentSet(
						"empty patch should not have effect",
						new HashMap<>(Map.of("int1", 123, "compound", new HashMap<>(Map.of("int1", 456)))),
						"{}",
						Map.of("int1", 123, "compound", Map.of("int1", 456))
				),
				argumentSet(
						"overriding only existing values",
						new HashMap<>(Map.of("int1", 123, "compound", new HashMap<>(Map.of("int1", 456)))),
						"{int1:1230,compound:{int1:4560}}",
						Map.of("int1", 1230, "compound", Map.of("int1", 4560))
				),
				argumentSet(
						"overriding lists",
						new HashMap<>(Map.of("int1", 123, "list", new ArrayList<>(List.of(12, 34, 56)))),
						"{list:[987]}",
						Map.of("int1", 123, "list", List.of(987))
				),
				argumentSet(
						"creating new compound on override",
						new HashMap<>(Map.of("int1", 123)),
						"{compound:{int1:456, int2:789}}",
						Map.of("int1", 123, "compound", Map.of("int1", 456, "int2", 789))
				),
				argumentSet(
						"null overrides",
						new HashMap<>(Map.of(
								"int2", 123,
								"list", new ArrayList<>(List.of(12, 34)),
								"compound", new HashMap<>(Map.of("int1", 98, "int2", 76))
						)),
						"{int2:null, list:null, compound:null}",
						mapForNullCase
				),
				argumentSet(
						"mixed overrides",
						new HashMap<>(Map.of("int1", 123, "compound", new HashMap<>(Map.of("int1", 456)))),
						"{int1:1230,compound:{int1:4560,int2:7890}}",
						Map.of("int1", 1230, "compound", Map.of("int1", 4560, "int2", 7890))
				)
		);
	}
}
