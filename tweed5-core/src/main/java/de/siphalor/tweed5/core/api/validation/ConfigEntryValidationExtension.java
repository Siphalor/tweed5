package de.siphalor.tweed5.core.api.validation;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.middleware.Middleware;

public interface ConfigEntryValidationExtension {
	Middleware<ConfigEntryValidationMiddleware> validationMiddleware(ConfigEntry<?> configEntry);
}
