package de.siphalor.tweed5.defaultextensions.validation.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import org.jspecify.annotations.Nullable;

public interface ConfigEntryValidator {
	<T extends @Nullable Object> ValidationResult<T> validate(ConfigEntry<T> configEntry, T value);

	<T> String description(ConfigEntry<T> configEntry);
}
