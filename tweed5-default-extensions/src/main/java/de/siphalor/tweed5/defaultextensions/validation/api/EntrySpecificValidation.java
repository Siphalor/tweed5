package de.siphalor.tweed5.defaultextensions.validation.api;

import de.siphalor.tweed5.core.api.middleware.Middleware;

import java.util.Collection;

public interface EntrySpecificValidation {
	Collection<Middleware<ConfigEntryValidator>> validators();
}
