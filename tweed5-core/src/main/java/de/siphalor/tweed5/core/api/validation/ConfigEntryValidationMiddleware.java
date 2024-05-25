package de.siphalor.tweed5.core.api.validation;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;

@FunctionalInterface
public interface ConfigEntryValidationMiddleware {
	<T> void validate(ConfigEntry<T> configEntry, T value) throws ConfigEntryValueValidationException;
}
