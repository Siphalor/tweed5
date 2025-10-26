package de.siphalor.tweed5.defaultextensions.validationfallback.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.defaultextensions.validationfallback.impl.ValidationFallbackExtensionImpl;

import java.util.function.Consumer;

public interface ValidationFallbackExtension extends TweedExtension {
	Class<? extends ValidationFallbackExtension> DEFAULT = ValidationFallbackExtensionImpl.class;
	String EXTENSION_ID = "validation-fallback";

	@Override
	default String getId() {
		return EXTENSION_ID;
	}

	static <C extends ConfigEntry<T>, T> Consumer<C> validationFallbackValue(T value) {
		return entry -> {
			ValidationFallbackExtension extension = entry.container().extension(ValidationFallbackExtension.class)
					.orElseThrow(() -> new IllegalStateException("ValidationFallbackExtension is not registered"));
			extension.setFallbackValue(entry, value);
		};
	}

	<T> void setFallbackValue(ConfigEntry<T> entry, T value);
}
