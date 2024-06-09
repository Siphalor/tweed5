package de.siphalor.tweed5.defaultextensions.validation.api.validators;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class NonNullValidator implements ConfigEntryValidator {
	@Override
	public <T> ValidationResult<T> validate(ConfigEntry<T> configEntry, T value) {
		if (value == null) {
			return ValidationResult.withIssues(null, Collections.singleton(
					new ValidationIssue("Value must not be null", ValidationIssueLevel.ERROR)
			));
		}
		return ValidationResult.ok(value);
	}

	@Override
	public @NotNull <T> String description(ConfigEntry<T> configEntry) {
		return "Must be set (not null).";
	}
}
