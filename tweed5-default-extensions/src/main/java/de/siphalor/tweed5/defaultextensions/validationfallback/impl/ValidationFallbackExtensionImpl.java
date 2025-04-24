package de.siphalor.tweed5.defaultextensions.validationfallback.impl;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationProvidingExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import de.siphalor.tweed5.defaultextensions.validationfallback.api.ValidationFallbackExtension;
import de.siphalor.tweed5.defaultextensions.validationfallback.api.ValidationFallbackValue;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(ValidationFallbackExtension.class)
public class ValidationFallbackExtensionImpl implements ValidationFallbackExtension, ValidationProvidingExtension {
	@Override
	public String getId() {
		return "validation-fallback";
	}

	@Override
	public void setup(TweedExtensionSetupContext context) {
		context.registerEntryExtensionData(ValidationFallbackValue.class);
	}

	@Override
	public Middleware<ConfigEntryValidator> validationMiddleware() {
		return new ValidationFallbackMiddleware();
	}

	private static class ValidationFallbackMiddleware implements Middleware<ConfigEntryValidator> {
		@Override
		public String id() {
			return "validation-fallback";
		}

		@Override
		public Set<String> mustComeBefore() {
			return Collections.emptySet();
		}

		@Override
		public Set<String> mustComeAfter() {
			return Collections.singleton("$default.end");
		}

		@Override
		public ConfigEntryValidator process(ConfigEntryValidator inner) {
			return new ConfigEntryValidator() {
				@Override
				public <T extends @Nullable Object> ValidationResult<T> validate(ConfigEntry<T> configEntry, T value) {
					ValidationResult<T> result = inner.validate(configEntry, value);
					if (!result.hasError()) {
						return result;
					}
					if (!configEntry.extensionsData().isPatchworkPartSet(ValidationFallbackValue.class)) {
						return result;
					}

					Object fallbackValue = ((ValidationFallbackValue) configEntry.extensionsData()).validationFallbackValue();
					if (fallbackValue != null) {
						if (fallbackValue.getClass() == configEntry.valueClass()) {
							//noinspection unchecked
							fallbackValue = configEntry.deepCopy((T) fallbackValue);
						} else {
							ArrayList<ValidationIssue> issues = new ArrayList<>(result.issues());
							issues.add(new ValidationIssue(
									"Fallback value is not of correct class, expected " + configEntry.valueClass().getName() +
											", but got " + fallbackValue.getClass().getName(),
									ValidationIssueLevel.ERROR
							));
							return ValidationResult.withIssues(value, issues);
						}
					}

					//noinspection unchecked
					return ValidationResult.withIssues(
							(T) fallbackValue,
							result.issues().stream()
									.map(issue -> new ValidationIssue(issue.message(), ValidationIssueLevel.WARN))
									.collect(Collectors.toList())
					);
				}

				@Override
				public <T> String description(ConfigEntry<T> configEntry) {
					if (!configEntry.extensionsData().isPatchworkPartSet(ValidationFallbackValue.class)) {
						return inner.description(configEntry);
					}
					return inner.description(configEntry) +
							"\n\nDefault/Fallback value: " +
							((ValidationFallbackValue) configEntry.extensionsData()).validationFallbackValue();
				}
			};
		}
	}
}
