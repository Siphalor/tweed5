package de.siphalor.tweed5.defaultextensions.validationfallback.impl;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.SimpleConfigEntry;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.hjson.HjsonCommentType;
import de.siphalor.tweed5.data.hjson.HjsonLexer;
import de.siphalor.tweed5.data.hjson.HjsonReader;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import de.siphalor.tweed5.defaultextensions.validationfallback.api.ValidationFallbackExtension;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.*;
import static de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters.*;
import static de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension.validators;
import static de.siphalor.tweed5.defaultextensions.validationfallback.api.ValidationFallbackExtension.validationFallbackValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationFallbackExtensionImplTest {
	private DefaultConfigContainer<Integer> configContainer;
	private SimpleConfigEntry<Integer> intEntry;

	@BeforeEach
	void setUp() {
		configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(CommentExtension.DEFAULT);
		configContainer.registerExtension(ValidationExtension.DEFAULT);
		configContainer.registerExtension(ValidationFallbackExtension.DEFAULT);
		configContainer.registerExtension(ReadWriteExtension.DEFAULT);

		configContainer.finishExtensionSetup();

		intEntry = new SimpleConfigEntryImpl<>(configContainer, Integer.class)
				.apply(entryReaderWriter(nullableReader(intReaderWriter()), nullableWriter(intReaderWriter())))
				.apply(validators(
						new ConfigEntryValidator() {
							@Override
							public <T extends @Nullable Object> ValidationResult<T> validate(ConfigEntry<T> configEntry, @Nullable T value) {
								if (value == null) {
									return ValidationResult.withIssues(
											null, Collections.singleton(
													new ValidationIssue(
															"Value must not be null",
															ValidationIssueLevel.ERROR
													)
											)
									);
								}
								return ValidationResult.ok(value);
							}

							@Override
							public <T> String description(ConfigEntry<T> configEntry) {
								return "Must not be null.";
							}
						},
						new ConfigEntryValidator() {

							@Override
							public <T> ValidationResult<T> validate(ConfigEntry<T> configEntry, @Nullable T value) {
								assert value != null;
								int intValue = (int) value;
								if (intValue < 1) {
									return ValidationResult.withIssues(
											value,
											Collections.singleton(new ValidationIssue(
													"Must be greater or equal to 1",
													ValidationIssueLevel.ERROR
											))
									);
								}
								if (intValue > 6) {
									return ValidationResult.withIssues(
											value,
											Collections.singleton(new ValidationIssue(
													"Must be smaller or equal to 6",
													ValidationIssueLevel.ERROR
											))
									);
								}
								return ValidationResult.ok(value);
							}

							@Override
							public <T> String description(ConfigEntry<T> configEntry) {
								return "Must be between 1 and 6";
							}
						}
				))
				.apply(validationFallbackValue(3));

		configContainer.attachTree(intEntry);
		configContainer.initialize();
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "7", "123", "null"})
	void fallbackTriggers(String input) {
		Integer result = intEntry.call(read(new HjsonReader(new HjsonLexer(new StringReader(input)))));
		assertEquals(3, result);
	}

	@Test
	void description() {
		StringWriter stringWriter = new StringWriter();
		intEntry.apply(write(
				new HjsonWriter(stringWriter, new HjsonWriter.Options().multilineCommentType(HjsonCommentType.SLASHES)),
				5
		));

		assertEquals(
				"""
				// Must not be null.
				// Must be between 1 and 6
				//\s
				// Default/Fallback value: 3
				5
				""", stringWriter.toString());
	}
}
