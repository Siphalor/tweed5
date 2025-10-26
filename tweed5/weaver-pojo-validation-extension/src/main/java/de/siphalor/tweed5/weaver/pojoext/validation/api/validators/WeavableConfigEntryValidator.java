package de.siphalor.tweed5.weaver.pojoext.validation.api.validators;

import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;

public interface WeavableConfigEntryValidator extends ConfigEntryValidator {
	TweedConstructFactory<WeavableConfigEntryValidator> FACTORY =
			TweedConstructFactory.builder(WeavableConfigEntryValidator.class)
					.typedArg(ConfigEntry.class)
					.namedArg("config", String.class)
					.build();
}
