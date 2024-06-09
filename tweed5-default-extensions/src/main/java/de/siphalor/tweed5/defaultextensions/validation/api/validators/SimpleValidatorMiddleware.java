package de.siphalor.tweed5.defaultextensions.validation.api.validators;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class SimpleValidatorMiddleware implements Middleware<ConfigEntryValidator> {
	String id;
	ConfigEntryValidator validator;

	@Override
	public ConfigEntryValidator process(ConfigEntryValidator inner) {
		return new ConfigEntryValidator() {
			@Override
			public <T> ValidationResult<T> validate(ConfigEntry<T> configEntry, T value) {
				return inner.validate(configEntry, value).andThen(v -> validator.validate(configEntry, v));
			}

			@Override
			public @NotNull <T> String description(ConfigEntry<T> configEntry) {
				String description = validator.description(configEntry);
				if (description.isEmpty()) {
					return inner.description(configEntry);
				}
				return inner.description(configEntry) + "\n" + description;
			}
		};
	}
}
