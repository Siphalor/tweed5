package de.siphalor.tweed5.core.api.extension;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;

public interface TweedExtension {
	String getId();

	default void setup(TweedExtensionSetupContext context) {
	}

	default void initEntry(ConfigEntry<?> configEntry) {
	}
}
