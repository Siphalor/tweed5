package de.siphalor.tweed5.core.api.extension;

import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;

public interface TweedExtension {
	TweedConstructFactory<TweedExtension> FACTORY = TweedConstructFactory.builder(TweedExtension.class)
			.typedArg(ConfigContainer.class)
			.typedArg(TweedExtensionSetupContext.class)
			.build();

	String getId();

	default void extensionsFinalized() {
	}

	default void initEntry(ConfigEntry<?> configEntry) {
	}
}
