package de.siphalor.tweed5.defaultextensions.validation.impl;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.EntryComment;
import de.siphalor.tweed5.defaultextensions.comment.impl.CommentExtensionImpl;
import de.siphalor.tweed5.defaultextensions.validation.api.EntrySpecificValidation;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssues;
import de.siphalor.tweed5.defaultextensions.validation.api.validators.NumberRangeValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.validators.SimpleValidatorMiddleware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValidationExtensionImplTest {
	private DefaultConfigContainer<Map<String, Object>> configContainer;
	private CommentExtension commentExtension;
	private ValidationExtension validationExtension;
	private StaticMapCompoundConfigEntryImpl<Map<String, Object>> rootEntry;
	private SimpleConfigEntryImpl<Byte> byteEntry;
	private SimpleConfigEntryImpl<Integer> intEntry;
	private SimpleConfigEntryImpl<Double> doubleEntry;

	@BeforeEach
	void setUp() {
		configContainer = new DefaultConfigContainer<>();

		commentExtension = new CommentExtensionImpl();
		configContainer.registerExtension(commentExtension);
		validationExtension = new ValidationExtensionImpl();
		configContainer.registerExtension(validationExtension);
		configContainer.finishExtensionSetup();

		//noinspection unchecked
		rootEntry = new StaticMapCompoundConfigEntryImpl<>(((Class<Map<String, Object>>) (Class<?>) Map.class), LinkedHashMap::new);

		byteEntry = new SimpleConfigEntryImpl<>(Byte.class);
		rootEntry.addSubEntry("byte", byteEntry);
		intEntry = new SimpleConfigEntryImpl<>(Integer.class);
		rootEntry.addSubEntry("int", intEntry);
		doubleEntry = new SimpleConfigEntryImpl<>(Double.class);
		rootEntry.addSubEntry("double", doubleEntry);

		configContainer.attachAndSealTree(rootEntry);

		//noinspection unchecked
		RegisteredExtensionData<EntryExtensionsData, EntryComment> commentData = (RegisteredExtensionData<EntryExtensionsData, EntryComment>) configContainer.entryDataExtensions().get(EntryComment.class);
		commentData.set(intEntry.extensionsData(), () -> "This is the main comment!");
		//noinspection unchecked
		RegisteredExtensionData<EntryExtensionsData, EntrySpecificValidation> entrySpecificValidation = (RegisteredExtensionData<EntryExtensionsData, EntrySpecificValidation>) configContainer.entryDataExtensions().get(EntrySpecificValidation.class);
		entrySpecificValidation.set(
				byteEntry.extensionsData(),
				() -> Collections.singleton(new SimpleValidatorMiddleware("range", new NumberRangeValidator<>(Byte.class, (byte) 10, (byte) 100)))
		);
		entrySpecificValidation.set(
				intEntry.extensionsData(),
				() -> Collections.singleton(new SimpleValidatorMiddleware("range", new NumberRangeValidator<>(Integer.class, null, 123)))
		);
		entrySpecificValidation.set(
				doubleEntry.extensionsData(),
				() -> Collections.singleton(new SimpleValidatorMiddleware("range", new NumberRangeValidator<>(Double.class, 0.5, null)))
		);

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