package de.siphalor.tweed5.defaultextensions.validationfallback.api;

import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.defaultextensions.validationfallback.impl.ValidationFallbackExtensionImpl;

public interface ValidationFallbackExtension extends TweedExtension {
	Class<? extends ValidationFallbackExtension> DEFAULT = ValidationFallbackExtensionImpl.class;
	String EXTENSION_ID = "validation-fallback";

	@Override
	default String getId() {
		return EXTENSION_ID;
	}

	void fallbackToPreset(String presetName);
}
