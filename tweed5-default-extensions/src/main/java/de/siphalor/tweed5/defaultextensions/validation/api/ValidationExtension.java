package de.siphalor.tweed5.defaultextensions.validation.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssues;
import org.jspecify.annotations.Nullable;

public interface ValidationExtension extends TweedExtension {
	<T extends @Nullable Object> ValidationIssues validate(ConfigEntry<T> entry, T value);
}
