package de.siphalor.tweed5.defaultextensions.validation.api;

import de.siphalor.tweed5.core.api.middleware.Middleware;

public interface ValidationProvidingExtension {
	Middleware<ConfigEntryValidator> validationMiddleware();
}
