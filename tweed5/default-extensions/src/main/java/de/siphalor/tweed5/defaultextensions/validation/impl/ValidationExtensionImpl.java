package de.siphalor.tweed5.defaultextensions.validation.impl;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.core.api.middleware.DefaultMiddlewareContainer;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.core.api.middleware.MiddlewareContainer;
import de.siphalor.tweed5.data.extension.api.TweedEntryReadException;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedReadContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentModifyingExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentProducer;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTracking;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTrackingConfigEntryValueVisitor;
import de.siphalor.tweed5.defaultextensions.pather.api.PatherExtension;
import de.siphalor.tweed5.defaultextensions.pather.api.ValuePathTracking;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationProvidingExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssues;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ValidationExtensionImpl implements ReadWriteRelatedExtension, ValidationExtension, CommentModifyingExtension {
	private static final ValidationResult<?> PRIMITIVE_IS_NULL_RESULT = ValidationResult.withIssues(
			null,
			Collections.singletonList(new ValidationIssue("Primitive value must not be null", ValidationIssueLevel.ERROR))
	);
	private static final ConfigEntryValidator PRIMITIVE_VALIDATOR = new ConfigEntryValidator() {
		@Override
		public <T> ValidationResult<T> validate(ConfigEntry<T> configEntry, @Nullable T value) {
			if (value == null) {
				//noinspection unchecked
				return (ValidationResult<T>) PRIMITIVE_IS_NULL_RESULT;
			}
			return ValidationResult.ok(value);
		}

		@Override
		public <T> String description(ConfigEntry<T> configEntry) {
			return "Value must not be null.";
		}
	};
	private static final ConfigEntryValidator NOOP_VALIDATOR = new ConfigEntryValidator() {
		@Override
		public <T> ValidationResult<@Nullable T> validate(ConfigEntry<T> configEntry, @Nullable T value) {
			return ValidationResult.ok(value);
		}

		@Override
		public <T> String description(ConfigEntry<T> configEntry) {
			return "";
		}
	};

	private final ConfigContainer<?> configContainer;
	private final PatchworkPartAccess<CustomEntryData> customEntryDataAccess;
	private final MiddlewareContainer<ConfigEntryValidator> entryValidatorMiddlewareContainer
			= new DefaultMiddlewareContainer<>();
	private @Nullable PatchworkPartAccess<ValidationIssues> readContextValidationIssuesAccess;
	private @Nullable PatherExtension patherExtension;

	public ValidationExtensionImpl(ConfigContainer<?> configContainer, TweedExtensionSetupContext context) {
		this.configContainer = configContainer;
		this.customEntryDataAccess = context.registerEntryExtensionData(CustomEntryData.class);
		context.registerExtension(PatherExtension.class);
	}

	@Override
	public void extensionsFinalized() {
		for (TweedExtension extension : configContainer.extensions()) {
			if (extension instanceof ValidationProvidingExtension) {
				entryValidatorMiddlewareContainer.register(
						((ValidationProvidingExtension) extension).validationMiddleware()
				);
			}
		}
		entryValidatorMiddlewareContainer.seal();

		patherExtension = configContainer.extension(PatherExtension.class)
				.orElseThrow(() -> new IllegalStateException("Missing requested PatherExtension"));
	}

	@Override
	public Middleware<CommentProducer> commentMiddleware() {
		return new Middleware<CommentProducer>() {
			@Override
			public String id() {
				return EXTENSION_ID;
			}

			@Override
			public CommentProducer process(CommentProducer inner) {
				return entry -> {
					String baseComment = inner.createComment(entry);
					CustomEntryData entryData = entry.extensionsData().get(customEntryDataAccess);
					if (entryData == null || entryData.completeValidator() == null) {
						return baseComment;
					}
					String validationDescription = entryData.completeValidator()
							.description(entry)
							.trim();
					if (validationDescription.isEmpty()) {
						return baseComment;
					}
					if (baseComment.isEmpty()) {
						return validationDescription;
					} else {
						return baseComment + "\n\n" + validationDescription;
					}
				};
			}
		};
	}

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		readContextValidationIssuesAccess = context.registerReadWriteContextExtensionData(ValidationIssues.class);
		context.registerReaderMiddleware(new EntryValidationReaderMiddleware());
	}

	@Override
	public <T> void addValidatorMiddleware(ConfigEntry<T> entry, Middleware<ConfigEntryValidator> validator) {
		CustomEntryData entryData = getOrCreateCustomEntryData(entry);
		entryData.addValidator(validator);
	}

	@Override
	public void initEntry(ConfigEntry<?> configEntry) {
		ConfigEntryValidator baseValidator;
		if (configEntry.valueClass().isPrimitive()) {
			baseValidator = PRIMITIVE_VALIDATOR;
		} else {
			baseValidator = NOOP_VALIDATOR;
		}

		CustomEntryData entryData = getOrCreateCustomEntryData(configEntry);

		if (entryData.validators().isEmpty()) {
			entryData.completeValidator(entryValidatorMiddlewareContainer.process(baseValidator));
		} else {
			DefaultMiddlewareContainer<ConfigEntryValidator> entrySpecificValidatorContainer = new DefaultMiddlewareContainer<>();
			entrySpecificValidatorContainer.registerAll(entryValidatorMiddlewareContainer.middlewares());
			entrySpecificValidatorContainer.registerAll(entryData.validators());
			entrySpecificValidatorContainer.seal();
			entryData.completeValidator(entrySpecificValidatorContainer.process(baseValidator));
		}
	}

	private CustomEntryData getOrCreateCustomEntryData(ConfigEntry<?> entry) {
		CustomEntryData entryData = entry.extensionsData().get(customEntryDataAccess);
		if (entryData == null) {
			entryData = new CustomEntryData();
			entry.extensionsData().set(customEntryDataAccess, entryData);
		}
		return entryData;
	}

	@Override
	public ValidationIssues captureValidationIssues(Patchwork readContextExtensionsData) {
		return getOrCreateValidationIssues(readContextExtensionsData);
	}

	@Override
	public <T> ValidationIssues validate(ConfigEntry<T> entry, @Nullable T value) {
		ValuePathTracking pathTracking = ValuePathTracking.create();
		ValidatingConfigEntryVisitor validatingVisitor = new ValidatingConfigEntryVisitor(pathTracking);

		entry.visitInOrder(new PathTrackingConfigEntryValueVisitor(validatingVisitor, pathTracking), value);

		return validatingVisitor.validationIssues();
	}

	@Override
	public <T> ValidationResult<T> validateValueFlat(ConfigEntry<T> entry, T value) {
		ConfigEntryValidator entryValidator = entry.extensionsData()
				.get(customEntryDataAccess)
				.completeValidator();
		assert entryValidator != null;

		return entryValidator.validate(entry, value);
	}

	@Data
	private static class CustomEntryData {
		@Setter(AccessLevel.NONE)
		private @Nullable List<Middleware<ConfigEntryValidator>> validators;
		private @Nullable ConfigEntryValidator completeValidator;

		public List<Middleware<ConfigEntryValidator>> validators() {
			return validators == null ? Collections.emptyList() : validators;
		}

		public void addValidator(Middleware<ConfigEntryValidator> validator) {
			if (validators == null) {
				validators = new ArrayList<>();
			}
			validators.add(validator);
		}
	}

	private class EntryValidationReaderMiddleware implements Middleware<TweedEntryReader<?, ?>> {
		@Override
		public String id() {
			return EXTENSION_ID;
		}

		@Override
		public Set<String> mustComeBefore() {
			return Collections.singleton(PatherExtension.EXTENSION_ID);
		}

		@Override
		public TweedEntryReader<?, ?> process(TweedEntryReader<?, ?> inner) {
			assert readContextValidationIssuesAccess != null && patherExtension != null;

			//noinspection unchecked
			TweedEntryReader<Object, ConfigEntry<Object>> castedInner = (TweedEntryReader<Object, ConfigEntry<Object>>) inner;
			return (TweedDataReader reader, ConfigEntry<Object> entry, TweedReadContext context) -> {
				ValidationIssues validationIssues = getOrCreateValidationIssues(context.extensionsData());

				Object value = castedInner.read(reader, entry, context);

				ConfigEntryValidator entryValidator = entry.extensionsData()
						.get(customEntryDataAccess)
						.completeValidator();
				assert entryValidator != null;

				ValidationResult<Object> validationResult = entryValidator.validate(entry, value);

				if (!validationResult.issues().isEmpty()) {
					String path = patherExtension.getPath(context);
					validationIssues.issuesByPath().put(path, new ValidationIssues.EntryIssues(
							entry,
							validationResult.issues()
					));
				}

				if (validationResult.hasError()) {
					throw new TweedEntryReadException(
							"Failed to validate entry: " + validationResult.issues(),
							context
					);
				}

				return validationResult.value();
			};
		}
	}

	private ValidationIssues getOrCreateValidationIssues(Patchwork readContextExtensionsData) {
		assert readContextValidationIssuesAccess != null;
		ValidationIssues validationIssues = readContextExtensionsData.get(readContextValidationIssuesAccess);
		if (validationIssues == null) {
			validationIssues = new ValidationIssuesImpl();
			readContextExtensionsData.set(readContextValidationIssuesAccess, validationIssues);
		}
		return validationIssues;
	}

	@Getter
	@RequiredArgsConstructor
	private class ValidatingConfigEntryVisitor implements ConfigEntryValueVisitor {
		private final PathTracking pathTracking;
		private final ValidationIssues validationIssues = new ValidationIssuesImpl();

		@Override
		public <T> void visitEntry(ConfigEntry<T> entry, T value) {
			CustomEntryData entryData = entry.extensionsData().get(customEntryDataAccess);
			assert entryData != null;
			ConfigEntryValidator entryValidator = entryData.completeValidator();
			assert entryValidator != null;
			ValidationResult<T> result = entryValidator.validate(entry, value);
			if (!result.issues().isEmpty()) {
				validationIssues.issuesByPath().put(pathTracking.currentPath(), new ValidationIssues.EntryIssues(entry, result.issues()));
			}
		}

		@Override
		public <T> boolean enterStructuredEntry(ConfigEntry<T> entry, T value) {
			return true;
		}

		@Override
		public <T> void leaveStructuredEntry(ConfigEntry<T> entry, T value) {
			visitEntry(entry, value);
		}
	}

	@Value
	private static class ValidationIssuesImpl implements ValidationIssues {
		Map<String, EntryIssues> issuesByPath = new HashMap<>();
	}
}
