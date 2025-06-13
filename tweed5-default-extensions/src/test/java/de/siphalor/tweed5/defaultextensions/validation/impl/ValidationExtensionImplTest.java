package de.siphalor.tweed5.defaultextensions.validation.impl;

import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.SimpleConfigEntry;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssues;
import de.siphalor.tweed5.defaultextensions.validation.api.validators.NumberRangeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension.baseComment;
import static de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension.validators;
import static de.siphalor.tweed5.testutils.MapTestUtils.sequencedMap;
import static java.util.Map.entry;
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

		configContainer.registerExtension(CommentExtension.DEFAULT);
		configContainer.registerExtension(ValidationExtension.DEFAULT);
		configContainer.finishExtensionSetup();

		byteEntry = new SimpleConfigEntryImpl<>(configContainer, Byte.class)
				.apply(validators(new NumberRangeValidator<>(Byte.class, (byte) 10, (byte) 100)));
		intEntry = new SimpleConfigEntryImpl<>(configContainer, Integer.class)
				.apply(validators(new NumberRangeValidator<>(Integer.class, null, 123)))
				.apply(baseComment("This is the main comment!"));
		doubleEntry = new SimpleConfigEntryImpl<>(configContainer, Double.class)
				.apply(validators(new NumberRangeValidator<>(Double.class, 0.5, null)));

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
		);


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
				() -> assertValidationIssue(result, ".byte", byteEntry, new ValidationIssue("Value must be at least 10", ValidationIssueLevel.WARN)),
				() -> assertValidationIssue(result, ".int", intEntry, new ValidationIssue("Value must be at most 123", ValidationIssueLevel.WARN)),
				() -> assertValidationIssue(result, ".double", doubleEntry, new ValidationIssue("Value must be at least 0.5", ValidationIssueLevel.WARN))
		);
	}

	private static void assertValidationIssue(
			ValidationIssues issues,
			String expectedPath,
			ConfigEntry<?> expectedEntry,
			ValidationIssue expectedIssue
	) {
		assertTrue(issues.issuesByPath().containsKey(expectedPath), "Must have issues for path " + expectedPath);
		ValidationIssues.EntryIssues entryIssues = issues.issuesByPath().get(expectedPath);
		assertSame(expectedEntry, entryIssues.entry(), "Entry must match");
		assertEquals(1, entryIssues.issues().size(), "Entry must have exactly one issue");
		assertEquals(expectedIssue, entryIssues.issues().iterator().next(), "Issue must match");
	}

}
