package de.siphalor.tweed5.defaultextensions.validation.api.validators;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;

@Value
@AllArgsConstructor
public class NumberRangeValidator<N extends @NonNull Number> implements ConfigEntryValidator {
	Class<N> numberClass;
	@Nullable N minimum;
	@Nullable N maximum;

	@Override
	public <T extends @Nullable Object> ValidationResult<T> validate(ConfigEntry<T> configEntry, T value) {
		if (!(value instanceof Number)) {
			return ValidationResult.withIssues(value, Collections.singleton(
					new ValidationIssue("Value must be numeric", ValidationIssueLevel.ERROR)
			));
		}
		if (value.getClass() != numberClass) {
			return ValidationResult.withIssues(value, Collections.singleton(
					new ValidationIssue(
							"Value is of wrong type, expected " + numberClass.getSimpleName() +
									", got " + value.getClass().getSimpleName(),
							ValidationIssueLevel.ERROR
					)
			));
		}

		Number numberValue = (Number) value;
		if (minimum != null && compare(numberValue, minimum) < 0) {
			//noinspection unchecked
			return ValidationResult.withIssues((T) minimum, Collections.singleton(
					new ValidationIssue("Value must be at least " + minimum, ValidationIssueLevel.WARN)
			));
		}
		if (maximum != null && compare(numberValue, maximum) > 0) {
			//noinspection unchecked
			return ValidationResult.withIssues((T) maximum, Collections.singleton(
					new ValidationIssue("Value must be at most " + maximum, ValidationIssueLevel.WARN)
			));
		}

		return ValidationResult.ok(value);
	}

	private int compare(Number a, Number b) {
		if (numberClass == Byte.class) {
			return Byte.compare(a.byteValue(), b.byteValue());
		} else if (numberClass == Short.class) {
			return Short.compare(a.shortValue(), b.shortValue());
		} else if (numberClass == Integer.class) {
			return Integer.compare(a.intValue(), b.intValue());
		} else if (numberClass == Long.class) {
			return Long.compare(a.longValue(), b.longValue());
		} else if (numberClass == Float.class) {
			return Float.compare(a.floatValue(), b.floatValue());
		} else {
			return Double.compare(a.doubleValue(), b.doubleValue());
		}
	}

	@Override
	public <T> String description(ConfigEntry<T> configEntry) {
		if (minimum == null) {
			if (maximum == null) {
				return "";
			} else {
				return "Must be smaller or equal to " + maximum + ".";
			}
		} else {
			if (maximum == null) {
				return "Must be greater or equal to " + minimum + ".";
			} else {
				return "Must be inclusively between " + minimum + " and " + maximum + ".";
			}
		}
	}
}
