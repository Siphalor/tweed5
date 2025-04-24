package de.siphalor.tweed5.defaultextensions.validation.impl;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.core.api.middleware.DefaultMiddlewareContainer;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.core.api.middleware.MiddlewareContainer;
import de.siphalor.tweed5.data.extension.api.TweedEntryReadException;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedReadContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteContextExtensionsData;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentModifyingExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentProducer;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTracking;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTrackingConfigEntryValueVisitor;
import de.siphalor.tweed5.defaultextensions.pather.api.PatherData;
import de.siphalor.tweed5.defaultextensions.pather.impl.PatherExtensionImpl;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.EntrySpecificValidation;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationProvidingExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssues;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@AutoService(ValidationExtension.class)
@NullUnmarked
public class ValidationExtensionImpl implements ReadWriteRelatedExtension, ValidationExtension, CommentModifyingExtension {
	private static final ValidationResult<?> PRIMITIVE_IS_NULL_RESULT = ValidationResult.withIssues(
			null,
			Collections.singletonList(new ValidationIssue("Primitive value must not be null", ValidationIssueLevel.ERROR))
	);
	private static final ConfigEntryValidator PRIMITIVE_VALIDATOR = new ConfigEntryValidator() {
		@Override
		public <T> ValidationResult<T> validate(@NotNull ConfigEntry<T> configEntry, @Nullable T value) {
			if (value == null) {
				//noinspection unchecked
				return (ValidationResult<T>) PRIMITIVE_IS_NULL_RESULT;
			}
			return ValidationResult.ok(value);
		}

		@Override
		public <T> String description(@NotNull ConfigEntry<T> configEntry) {
			return "Value must not be null.";
		}
	};
	private static final ConfigEntryValidator NOOP_VALIDATOR = new ConfigEntryValidator() {
		@Override
		public <T> ValidationResult<T> validate(@NotNull ConfigEntry<T> configEntry, @Nullable T value) {
			return ValidationResult.ok(value);
		}

		@Override
		public <T> String description(@NotNull ConfigEntry<T> configEntry) {
			return "";
		}
	};

	private RegisteredExtensionData<EntryExtensionsData, InternalValidationEntryData> validationEntryDataExtension;
	private MiddlewareContainer<ConfigEntryValidator> entryValidatorMiddlewareContainer;
	private RegisteredExtensionData<ReadWriteContextExtensionsData, ValidationIssues> readContextValidationIssuesExtensionData;

	@Override
	public String getId() {
		return "validation";
	}

	@Override
	public void setup(TweedExtensionSetupContext context) {
		context.registerExtension(new PatherExtensionImpl());

		validationEntryDataExtension = context.registerEntryExtensionData(InternalValidationEntryData.class);
		context.registerEntryExtensionData(EntrySpecificValidation.class);

		entryValidatorMiddlewareContainer = new DefaultMiddlewareContainer<>();
		for (TweedExtension extension : context.configContainer().extensions()) {
			if (extension instanceof ValidationProvidingExtension) {
				entryValidatorMiddlewareContainer.register(((ValidationProvidingExtension) extension).validationMiddleware());
			}
		}
		entryValidatorMiddlewareContainer.seal();
	}

	@Override
	public Middleware<CommentProducer> commentMiddleware() {
		return new Middleware<CommentProducer>() {
			@Override
			public String id() {
				return "validation";
			}

			@Override
			public CommentProducer process(CommentProducer inner) {
				return entry -> {
					String baseComment = inner.createComment(entry);
					if (entry.extensionsData().isPatchworkPartSet(InternalValidationEntryData.class)) {
						String validationDescription = ((InternalValidationEntryData) entry.extensionsData())
								.completeEntryValidator()
								.description(entry)
								.trim();
						if (!validationDescription.isEmpty()) {
							baseComment += "\n\n" + validationDescription;
						}
					}
					return baseComment;
				};
			}
		};
	}

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		readContextValidationIssuesExtensionData = context.registerReadWriteContextExtensionData(ValidationIssues.class);
	}

	@Override
	public void initEntry(ConfigEntry<?> configEntry) {
		ConfigEntryValidator baseValidator;
		if (configEntry.valueClass().isPrimitive()) {
			baseValidator = PRIMITIVE_VALIDATOR;
		} else {
			baseValidator = NOOP_VALIDATOR;
		}

		ConfigEntryValidator entryValidator;
		Collection<Middleware<ConfigEntryValidator>> entrySpecificValidators = getEntrySpecificValidators(configEntry);
		if (entrySpecificValidators.isEmpty()) {
			entryValidator = entryValidatorMiddlewareContainer.process(baseValidator);
		} else {
			DefaultMiddlewareContainer<ConfigEntryValidator> entrySpecificValidatorContainer = new DefaultMiddlewareContainer<>();
			entrySpecificValidatorContainer.registerAll(entryValidatorMiddlewareContainer.middlewares());
			entrySpecificValidatorContainer.registerAll(entrySpecificValidators);
			entrySpecificValidatorContainer.seal();
			entryValidator = entrySpecificValidatorContainer.process(baseValidator);
		}

		validationEntryDataExtension.set(configEntry.extensionsData(), new InternalValidationEntryDataImpl(entryValidator));
	}

	private Collection<Middleware<ConfigEntryValidator>> getEntrySpecificValidators(ConfigEntry<?> configEntry) {
		if (!configEntry.extensionsData().isPatchworkPartSet(EntrySpecificValidation.class)) {
			return Collections.emptyList();
		}
		return ((EntrySpecificValidation) configEntry.extensionsData()).validators();
	}

	@Override
	public @Nullable Middleware<TweedEntryReader<?, ?>> entryReaderMiddleware() {
		return new EntryValidationReaderMiddleware();
	}

	@Override
	public <T> ValidationIssues validate(@NotNull ConfigEntry<T> entry, @Nullable T value) {
		PathTracking pathTracking = new PathTracking();
		ValidatingConfigEntryVisitor validatingVisitor = new ValidatingConfigEntryVisitor(pathTracking);

		entry.visitInOrder(new PathTrackingConfigEntryValueVisitor(validatingVisitor, pathTracking), value);

		return validatingVisitor.validationIssues();
	}

	@Value
	private static class InternalValidationEntryDataImpl implements InternalValidationEntryData {
		ConfigEntryValidator completeEntryValidator;
	}

	private class EntryValidationReaderMiddleware implements Middleware<TweedEntryReader<?, ?>> {
		@Override
		public String id() {
			return "validation";
		}

		@Override
		public Set<String> mustComeAfter() {
			return Collections.singleton("pather");
		}

		@Override
		public TweedEntryReader<?, ?> process(TweedEntryReader<?, ?> inner) {
			//noinspection unchecked
			TweedEntryReader<Object, ConfigEntry<Object>> castedInner = (TweedEntryReader<Object, ConfigEntry<Object>>) inner;
			return (TweedDataReader reader, ConfigEntry<Object> entry, TweedReadContext context) -> {
				ValidationIssues validationIssues;
				if (!context.extensionsData().isPatchworkPartSet(ValidationIssues.class)) {
					validationIssues = new ValidationIssuesImpl();
					readContextValidationIssuesExtensionData.set(context.extensionsData(), validationIssues);
				} else {
					validationIssues = (ValidationIssues) context.extensionsData();
				}

				Object value = castedInner.read(reader, entry, context);

				ValidationResult<Object> validationResult = ((InternalValidationEntryData) entry.extensionsData()).completeEntryValidator().validate(entry, value);

				if (!validationResult.issues().isEmpty() && context.extensionsData().isPatchworkPartSet(PatherData.class)) {
					String path = ((PatherData) context.extensionsData()).valuePath();
					validationIssues.issuesByPath().put(path, new ValidationIssues.EntryIssues(
							entry,
							validationResult.issues()
					));
				}

				if (validationResult.hasError()) {
					throw new TweedEntryReadException("Failed to validate entry: " + validationResult.issues());
				}

				return validationResult.value();
			};
		}
	}

	@Getter
	@RequiredArgsConstructor
	private static class ValidatingConfigEntryVisitor implements ConfigEntryValueVisitor {
		private final PathTracking pathTracking;
		private final ValidationIssues validationIssues = new ValidationIssuesImpl();

		@Override
		public <T> void visitEntry(ConfigEntry<T> entry, T value) {
			ValidationResult<T> result = ((InternalValidationEntryData) entry.extensionsData()).completeEntryValidator().validate(entry, value);
			if (!result.issues().isEmpty()) {
				validationIssues.issuesByPath().put(pathTracking.valuePath(), new ValidationIssues.EntryIssues(entry, result.issues()));
			}
		}

		@Override
		public <T> boolean enterCollectionEntry(ConfigEntry<T> entry, T value) {
			return true;
		}

		@Override
		public <T> void leaveCollectionEntry(ConfigEntry<T> entry, T value) {
			visitEntry(entry, value);
		}

		@Override
		public <T> boolean enterCompoundEntry(ConfigEntry<T> entry, T value) {
			return true;
		}

		@Override
		public <T> void leaveCompoundEntry(ConfigEntry<T> entry, T value) {
			visitEntry(entry, value);
		}
	}

	@Value
	private static class ValidationIssuesImpl implements ValidationIssues {
		Map<String, EntryIssues> issuesByPath = new HashMap<>();
	}
}
