package de.siphalor.tweed5.defaultextensions.readfallback.api;

import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.defaultextensions.readfallback.impl.ReadFallbackExtensionImpl;

public interface ReadFallbackExtension extends TweedExtension {
	Class<? extends ReadFallbackExtension> DEFAULT = ReadFallbackExtensionImpl.class;
	String EXTENSION_ID = "read-fallback";

	@Override
	default String getId() {
		return EXTENSION_ID;
	}
}
