package de.siphalor.tweed5.defaultextensions.validationfallback.impl;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.data.extension.api.EntryReaderWriterDefinition;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters;
import de.siphalor.tweed5.data.hjson.HjsonCommentType;
import de.siphalor.tweed5.data.hjson.HjsonLexer;
import de.siphalor.tweed5.data.hjson.HjsonReader;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.EntrySpecificValidation;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import de.siphalor.tweed5.defaultextensions.validation.api.validators.SimpleValidatorMiddleware;
import de.siphalor.tweed5.defaultextensions.validationfallback.api.ValidationFallbackExtension;
import de.siphalor.tweed5.defaultextensions.validationfallback.api.ValidationFallbackValue;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationFallbackExtensionImplTest {
	private DefaultConfigContainer<Integer> configContainer;
	private SimpleConfigEntryImpl<Integer> intEntry;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(CommentExtension.DEFAULT);
		configContainer.registerExtension(ValidationExtension.DEFAULT);
		configContainer.registerExtension(ValidationFallbackExtension.DEFAULT);
		configContainer.registerExtension(ReadWriteExtension.DEFAULT);

		configContainer.finishExtensionSetup();

		intEntry = new SimpleConfigEntryImpl<>(Integer.class);

		configContainer.attachAndSealTree(intEntry);

		RegisteredExtensionData<EntryExtensionsData, EntrySpecificValidation> entrySpecificValidation = (RegisteredExtensionData<EntryExtensionsData, EntrySpecificValidation>) configContainer.entryDataExtensions().get(EntrySpecificValidation.class);
		entrySpecificValidation.set(intEntry.extensionsData(), () -> Arrays.asList(
				new SimpleValidatorMiddleware("non-null", new ConfigEntryValidator() {
					@Override
					public <T> ValidationResult<T> validate(ConfigEntry<T> configEntry, @Nullable T value) {
						if (value == null) {
							return ValidationResult.withIssues(null, Collections.singleton(
									new ValidationIssue("Value must not be null", ValidationIssueLevel.ERROR)
							));
						}
						return ValidationResult.ok(value);
					}

					@Override
					public @NotNull <T> String description(ConfigEntry<T> configEntry) {
						return "Must not be null.";
					}
				}),
				new SimpleValidatorMiddleware("range", new ConfigEntryValidator() {
					@Override
					public <T> ValidationResult<T> validate(ConfigEntry<T> configEntry, @Nullable T value) {
						Integer intValue = (Integer) value;
						if (intValue < 1) {
							return ValidationResult.withIssues(value, Collections.singleton(new ValidationIssue("Must be greater or equal to 1", ValidationIssueLevel.ERROR)));
						}
						if (intValue > 6) {
							return ValidationResult.withIssues(value, Collections.singleton(new ValidationIssue("Must be smaller or equal to 6", ValidationIssueLevel.ERROR)));
						}
						return ValidationResult.ok(value);
					}

					@Override
					public @NotNull <T> String description(ConfigEntry<T> configEntry) {
						return "Must be between 1 and 6";
					}
				}) {
					@Override
					public Set<String> mustComeAfter() {
						return Collections.singleton("non-null");
					}
				}
		));

		RegisteredExtensionData<EntryExtensionsData, ValidationFallbackValue> validationFallbackValue = (RegisteredExtensionData<EntryExtensionsData, ValidationFallbackValue>) configContainer.entryDataExtensions().get(ValidationFallbackValue.class);
		validationFallbackValue.set(intEntry.extensionsData(), () -> 3);

		RegisteredExtensionData<EntryExtensionsData, EntryReaderWriterDefinition> readerWriterData = (RegisteredExtensionData<EntryExtensionsData, EntryReaderWriterDefinition>) configContainer.entryDataExtensions().get(EntryReaderWriterDefinition.class);
		readerWriterData.set(intEntry.extensionsData(), new EntryReaderWriterDefinition() {
			@Override
			public TweedEntryReader<?, ?> reader() {
				return TweedEntryReaderWriters.nullableReader(TweedEntryReaderWriters.intReaderWriter());
			}

			@Override
			public TweedEntryWriter<?, ?> writer() {
				return TweedEntryReaderWriters.nullableWriter(TweedEntryReaderWriters.intReaderWriter());
			}
		});

		configContainer.initialize();
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "7", "123", "null"})
	void fallbackTriggers(String input) {
		ReadWriteExtension readWriteExtension = configContainer.extension(ReadWriteExtension.class).orElseThrow();
		Integer result = assertDoesNotThrow(() -> readWriteExtension.read(
				new HjsonReader(new HjsonLexer(new StringReader(input))),
				intEntry,
				readWriteExtension.createReadWriteContextExtensionsData()
		));
		assertEquals(3, result);
	}

	@Test
	void description() {
		ReadWriteExtension readWriteExtension = configContainer.extension(ReadWriteExtension.class).orElseThrow();
		StringWriter stringWriter = new StringWriter();
		assertDoesNotThrow(() -> readWriteExtension.write(
				new HjsonWriter(stringWriter, new HjsonWriter.Options().multilineCommentType(HjsonCommentType.SLASHES)),
				5,
				intEntry,
				readWriteExtension.createReadWriteContextExtensionsData()
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
