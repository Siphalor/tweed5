package de.siphalor.tweed5.defaultextensions.validation.api.validators;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.Collections;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberRangeValidator<N extends Number> implements ConfigEntryValidator {
	Class<N> numberClass;
	@Nullable N minimum;
	boolean minimumExclusive;
	@Nullable N maximum;
	boolean maximumExclusive;
	String description;

	public static <N extends Number> Builder<N> builder(Class<N> numberClass) {
		return new Builder<>(numberClass);
	}

	@Override
	public <T extends @Nullable Object> ValidationResult<T> validate(ConfigEntry<T> configEntry, T value) {
		if (!(value instanceof Number)) {
			return ValidationResult.withIssues(value, Collections.singleton(
					new ValidationIssue("Value must be numeric, got" + getClassName(value), ValidationIssueLevel.ERROR)
			));
		}
		if (value.getClass() != numberClass) {
			return ValidationResult.withIssues(value, Collections.singleton(
					new ValidationIssue(
							"Value is of wrong type, expected " + numberClass.getName() +
									", got " + getClassName(value),
							ValidationIssueLevel.ERROR
					)
			));
		}

		Number numberValue = (Number) value;
		if (minimum != null) {
			int minCmp = compare(numberValue, minimum);
			if (minimumExclusive ? minCmp <= 0 : minCmp < 0) {
				//noinspection unchecked
				return ValidationResult.withIssues(
						(T) minimum,
						Collections.singleton(new ValidationIssue(
								description + ", got: " + value,
								ValidationIssueLevel.WARN
						))
				);
			}
		}
		if (maximum != null) {
			int maxCmp = compare(numberValue, maximum);
			if (maximumExclusive ? maxCmp >= 0 : maxCmp > 0) {
				//noinspection unchecked
				return ValidationResult.withIssues(
						(T) maximum,
						Collections.singleton(new ValidationIssue(
								description + " Got: " + value,
								ValidationIssueLevel.WARN
						))
				);
			}
		}

		return ValidationResult.ok(value);
	}

	private static String getClassName(@Nullable Object value) {
		if (value == null) {
			return "<null>";
		}
		return value.getClass().getName();
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
		return description;
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Builder<N extends Number> {
		private final Class<N> numberClass;
		private @Nullable N minimum;
		private boolean minimumExclusive;
		private @Nullable N maximum;
		private boolean maximumExclusive;

		public Builder<N> greaterThan(N minimum) {
			this.minimumExclusive = true;
			this.minimum = minimum;
			return this;
		}

		public Builder<N> greaterThanOrEqualTo(N minimum) {
			this.minimumExclusive = false;
			this.minimum = minimum;
			return this;
		}

		public Builder<N> lessThan(N maximum) {
			this.maximumExclusive = true;
			this.maximum = maximum;
			return this;
		}

		public Builder<N> lessThanOrEqualTo(N maximum) {
			this.maximumExclusive = false;
			this.maximum = maximum;
			return this;
		}

		public NumberRangeValidator<N> build() {
			return new NumberRangeValidator<>(
					numberClass,
					minimum, minimumExclusive,
					maximum, maximumExclusive,
					createDescription()
			);
		}

		private String createDescription() {
			if (minimum != null) {
				if (maximum != null) {
					if (minimumExclusive == maximumExclusive) {
						if (minimumExclusive) {
							return "Must be exclusively between " + minimum + " and " + maximum + ".";
						} else {
							return "Must be inclusively between " + minimum + " and " + maximum + ".";
						}
					}

					StringBuilder description = new StringBuilder(40);
					description.append("Must be greater than ");
					if (!minimumExclusive) {
						description.append("or equal to ");
					}
					description.append(minimum).append(" and less than ");
					if (!maximumExclusive) {
						description.append("or equal to ");
					}
					description.append(maximum).append('.');
					return description.toString();
				} else if (minimumExclusive) {
					return "Must be greater than " + minimum + ".";
				} else {
					return "Must be greater than or equal to " + minimum + ".";
				}
			}
			if (maximum != null) {
				if (maximumExclusive) {
					return "Must be less than " + maximum + ".";
				} else {
					return "Must be less than or equal to " + maximum + ".";
				}
			}
			return "";
		}
	}
}
