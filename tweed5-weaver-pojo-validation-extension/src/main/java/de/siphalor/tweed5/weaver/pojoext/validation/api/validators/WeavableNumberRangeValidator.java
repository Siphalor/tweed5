package de.siphalor.tweed5.weaver.pojoext.validation.api.validators;

import de.siphalor.tweed5.construct.api.ConstructParameter;
import de.siphalor.tweed5.construct.api.TweedConstruct;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import de.siphalor.tweed5.defaultextensions.validation.api.validators.NumberRangeValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.var;
import org.jetbrains.annotations.ApiStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WeavableNumberRangeValidator implements WeavableConfigEntryValidator {
	private static final String NUMBER_PATTERN = "[+-]?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?";
	private static final Pattern CONFIG_PATTERN =
			Pattern.compile("^(" + NUMBER_PATTERN + "=?)?\\.\\.(=?" + NUMBER_PATTERN + ")?$");

	NumberRangeValidator<Number> validator;

	@ApiStatus.Internal
	@TweedConstruct(WeavableConfigEntryValidator.class)
	public static WeavableNumberRangeValidator construct(
			ConfigEntry<?> configEntry,
			@ConstructParameter(name = "config") String config
	) {
		if (config.isEmpty()) {
			throw new IllegalArgumentException("Config is required for number range validator");
		}

		Matcher matcher = CONFIG_PATTERN.matcher(config);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(
					"Invalid config: " + config + "; expected format: [<min>[=]]..[[=]<max>]"
			);
		}

		//noinspection unchecked
		Class<? extends Number> numberClass = boxClass((Class<? extends Number>) configEntry.valueClass());
		//noinspection unchecked
		var builder = NumberRangeValidator.builder((Class<Number>) numberClass);

		String minGroup = matcher.group(1);
		if (minGroup != null) {
			if (minGroup.endsWith("=")) {
				minGroup = minGroup.substring(0, minGroup.length() - 1);
				builder.greaterThanOrEqualTo(parseNumber(minGroup, numberClass));
			} else {
				builder.greaterThan(parseNumber(minGroup, numberClass));
			}
		}

		String maxGroup = matcher.group(2);
		if (maxGroup != null) {
			if (maxGroup.startsWith("=")) {
				maxGroup = maxGroup.substring(1);
				builder.lessThanOrEqualTo(parseNumber(maxGroup, numberClass));
			} else {
				builder.lessThan(parseNumber(maxGroup, numberClass));
			}
		}

		return new WeavableNumberRangeValidator(builder.build());
	}

	private static Class<? extends Number> boxClass(Class<? extends Number> numberClass) {
		if (numberClass == byte.class) {
			return Byte.class;
		} else if (numberClass == short.class) {
			return Short.class;
		} else if (numberClass == int.class) {
			return Integer.class;
		} else if (numberClass == long.class) {
			return Long.class;
		} else if (numberClass == float.class) {
			return Float.class;
		} else if (numberClass == double.class) {
			return Double.class;
		}
		return numberClass;
	}

	private static Number parseNumber(String number, Class<? extends Number> numberClass) {
		if (numberClass == Byte.class) {
			return Byte.valueOf(number);
		} else if (numberClass == Short.class) {
			return Short.valueOf(number);
		} else if (numberClass == Integer.class) {
			return Integer.valueOf(number);
		} else if (numberClass == Long.class) {
			return Long.valueOf(number);
		} else if (numberClass == Float.class) {
			return Float.valueOf(number);
		} else if (numberClass == Double.class) {
			return Double.valueOf(number);
		} else {
			throw new IllegalArgumentException("Unsupported number class: " + numberClass.getName());
		}
	}

	@Override
	public <T> ValidationResult<T> validate(ConfigEntry<T> configEntry, T value) {
		return validator.validate(configEntry, value);
	}

	@Override
	public <T> String description(ConfigEntry<T> configEntry) {
		return validator.description(configEntry);
	}
}
