package de.siphalor.tweed5.coat.bridge.api.mapping;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;

public interface TweedCoatMapper<T> {
	TweedCoatEntryMappingResult<T, ?> mapEntry(ConfigEntry<T> entry, TweedCoatEntryMappingContext context);

}
