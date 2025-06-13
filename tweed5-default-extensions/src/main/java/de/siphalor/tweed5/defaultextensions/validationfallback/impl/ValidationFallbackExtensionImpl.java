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
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(ValidationFallbackExtension.class)
public class ValidationFallbackExtensionImpl implements ValidationFallbackExtension, ValidationProvidingExtension {

	private final PatchworkPartAccess<CustomEntryData> customEntryDataAccess;

	public ValidationFallbackExtensionImpl(TweedExtensionSetupContext context) {
		customEntryDataAccess = context.registerEntryExtensionData(CustomEntryData.class);
	}

	@Override
	public String getId() {
		return "validation-fallback";
	}

	@Override
	public <T> void setFallbackValue(ConfigEntry<T> entry, T value) {
		getOrCreateCustomEntryData(entry).fallbackValue(value);
	}

	private CustomEntryData getOrCreateCustomEntryData(ConfigEntry<?> entry) {
		CustomEntryData customEntryData = entry.extensionsData().get(customEntryDataAccess);
		if (customEntryData == null) {
			customEntryData = new CustomEntryData();
			entry.extensionsData().set(customEntryDataAccess, customEntryData);
		}
		return customEntryData;
	}

	@Override
	public Middleware<ConfigEntryValidator> validationMiddleware() {
		return new ValidationFallbackMiddleware();
	}

	@Data
	private static class CustomEntryData {
		@Nullable Object fallbackValue;
	}

	private class ValidationFallbackMiddleware implements Middleware<ConfigEntryValidator> {
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
					CustomEntryData entryData = configEntry.extensionsData().get(customEntryDataAccess);
					if (entryData == null) {
						return result;
					}

					Object fallbackValue = entryData.fallbackValue();
					if (fallbackValue != null) {
						if (configEntry.valueClass().isInstance(fallbackValue)) {
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
					CustomEntryData entryData = configEntry.extensionsData().get(customEntryDataAccess);
					if (entryData == null) {
						return inner.description(configEntry);
					}
					return inner.description(configEntry) + "\n\nDefault/Fallback value: " + entryData.fallbackValue();
				}
			};
		}
	}
}
