package de.siphalor.tweed5.defaultextensions.validation.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import org.jetbrains.annotations.NotNull;

public interface ConfigEntryValidator {
	<T> ValidationResult<T> validate(ConfigEntry<T> configEntry, T value);

	@NotNull
	<T> String description(ConfigEntry<T> configEntry);
}
