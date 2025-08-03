package de.siphalor.tweed5.defaultextensions.validation.impl;

import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.SimpleConfigEntry;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.hjson.HjsonLexer;
import de.siphalor.tweed5.data.hjson.HjsonReader;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssues;
import de.siphalor.tweed5.defaultextensions.validation.api.validators.NumberRangeValidator;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.entryReaderWriter;
import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.read;
import static de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters.*;
import static de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension.baseComment;
import static de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension.validators;
import static de.siphalor.tweed5.testutils.generic.MapTestUtils.sequencedMap;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ValidationExtensionImplTest {
	private DefaultConfigContainer<Map<String, Object>> configContainer;
	private CompoundConfigEntry<Map<String, Object>> rootEntry;
	private SimpleConfigEntry<Byte> byteEntry;
	private SimpleConfigEntry<Integer> intEntry;
	private SimpleConfigEntry<Double> doubleEntry;

	@BeforeEach
	void setUp() {
		configContainer = new DefaultConfigContainer<>();

		configContainer.registerExtension(CommentExtension.class);
		configContainer.registerExtension(ReadWriteExtension.class);
		configContainer.registerExtension(ValidationExtension.class);
		configContainer.finishExtensionSetup();

		byteEntry = new SimpleConfigEntryImpl<>(configContainer, Byte.class)
				.apply(entryReaderWriter(byteReaderWriter()))
				.apply(validators(
						NumberRangeValidator.builder(Byte.class)
								.greaterThanOrEqualTo((byte) 11)
								.lessThanOrEqualTo((byte) 100)
								.build()
				));
		intEntry = new SimpleConfigEntryImpl<>(configContainer, Integer.class)
				.apply(entryReaderWriter(intReaderWriter()))
				.apply(validators(NumberRangeValidator.builder(Integer.class).lessThanOrEqualTo(123).build()))
				.apply(baseComment("This is the main comment!"));
		doubleEntry = new SimpleConfigEntryImpl<>(configContainer, Double.class)
				.apply(entryReaderWriter(doubleReaderWriter()))
				.apply(validators(NumberRangeValidator.builder(Double.class).greaterThanOrEqualTo(0.5).build()));

		//noinspection unchecked
		rootEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				((Class<Map<String, Object>>) (Class<?>) Map.class),
				LinkedHashMap::new,
				sequencedMap(List.of(
						entry("byte", byteEntry),
						entry("int", intEntry),
						entry("double", doubleEntry)
				))
		)
				.apply(entryReaderWriter(compoundReaderWriter()));


		configContainer.attachTree(rootEntry);
		configContainer.initialize();
	}

	@ParameterizedTest
	@CsvSource({
			"12, 34, 56.78"
	})
	void valid(Byte b, Integer i, Double d) {
		HashMap<String, Object> value = new HashMap<>();
		value.put("byte", b);
		value.put("int", i);
		value.put("double", d);

		ValidationExtension validationExtension = configContainer.extension(ValidationExtension.class).orElseThrow();
		ValidationIssues result = validationExtension.validate(rootEntry, value);
		assertNotNull(result);
		assertNotNull(result.issuesByPath());
		assertTrue(result.issuesByPath().isEmpty(), () -> "Should have no issues, but got " + result.issuesByPath());
	}

	@Test
	void invalid() {
		HashMap<String, Object> value = new HashMap<>();
		value.put("byte", (byte) 9);
		value.put("int", 124);
		value.put("double", 0.2);

		ValidationExtension validationExtension = configContainer.extension(ValidationExtension.class).orElseThrow();
		ValidationIssues result = validationExtension.validate(rootEntry, value);
		assertNotNull(result);
		assertNotNull(result.issuesByPath());

		assertAll(
				() -> assertValidationIssue(
						result,
						".byte",
						byteEntry,
						ValidationIssueLevel.WARN,
						message -> assertThat(message).contains("11", "100", "9")
				),
				() -> assertValidationIssue(
						result,
						".int",
						intEntry,
						ValidationIssueLevel.WARN,
						message -> assertThat(message).contains("123", "124")
				),
				() -> assertValidationIssue(
						result,
						".double",
						doubleEntry,
						ValidationIssueLevel.WARN,
						message -> assertThat(message).contains("0.5", "0.2")
				)
		);
	}

	@Test
	void readInvalid() {
		ValidationExtension validationExtension = configContainer.extension(ValidationExtension.class).orElseThrow();

		var reader = new HjsonReader(new HjsonLexer(new StringReader("""
				{
					byte: 9
					int: 124
					double: 0.2
				}
				""")));
		var validationIssues = new AtomicReference<@Nullable ValidationIssues>();
		Map<String, Object> value = configContainer.rootEntry().call(read(
				reader,
				extensionsData -> validationIssues.set(validationExtension.captureValidationIssues(extensionsData))
		));

		assertThat(value).isEqualTo(Map.of("byte", (byte) 11, "int", 123, "double", 0.5));
		//noinspection DataFlowIssue
		assertThat(validationIssues.get()).isNotNull().satisfies(
				vi -> assertValidationIssue(
						vi,
						".byte",
						byteEntry,
						ValidationIssueLevel.WARN,
						message -> assertThat(message).contains("11", "100", "9")
				),
				vi -> assertValidationIssue(
						vi,
						".int",
						intEntry,
						ValidationIssueLevel.WARN,
						message -> assertThat(message).contains("123", "124")
				 ),
				vi -> assertValidationIssue(
						vi,
						".double",
						doubleEntry,
						ValidationIssueLevel.WARN,
						message -> assertThat(message).contains("0.5", "0.2")
				)
		);
	}

	private static void assertValidationIssue(
			ValidationIssues issues,
			String expectedPath,
			ConfigEntry<?> expectedEntry,
			ValidationIssueLevel expectedLevel,
			Consumer<String> issueMessageConsumer
	) {
		assertThat(issues.issuesByPath()).hasEntrySatisfying(expectedPath, entryIssues -> assertThat(entryIssues).satisfies(
				eis -> assertThat(eis.entry()).isSameAs(expectedEntry),
				eis -> assertThat(eis.issues()).singleElement().satisfies(
						ei -> assertThat(ei.level()).isEqualTo(expectedLevel),
						ei -> assertThat(ei.message()).satisfies(issueMessageConsumer)
				)
		));
	}
}
