package de.siphalor.tweed5.defaultextensions.validation.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssues;
import de.siphalor.tweed5.defaultextensions.validation.impl.ValidationExtensionImpl;
import org.jspecify.annotations.Nullable;

public interface ValidationExtension extends TweedExtension {
	Class<? extends ValidationExtension> DEFAULT = ValidationExtensionImpl.class;

	<T extends @Nullable Object> ValidationIssues validate(ConfigEntry<T> entry, T value);
}
