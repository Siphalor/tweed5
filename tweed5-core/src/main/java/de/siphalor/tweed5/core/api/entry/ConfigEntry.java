package de.siphalor.tweed5.core.api.entry;

import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.validation.ConfigEntryValueValidationException;

public interface ConfigEntry<T> {
	Class<T> valueClass();
	void validate(T value) throws ConfigEntryValueValidationException;

	void seal(ConfigContainer<?> container);
	boolean sealed();

	EntryExtensionsData extensionsData();

	void visitInOrder(ConfigEntryVisitor visitor);
}
