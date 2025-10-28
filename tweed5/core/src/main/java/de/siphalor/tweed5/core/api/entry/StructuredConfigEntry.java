package de.siphalor.tweed5.core.api.entry;

import java.util.Map;
import java.util.function.Consumer;

public interface StructuredConfigEntry<T> extends ConfigEntry<T> {
	@Override
	default StructuredConfigEntry<T> apply(Consumer<ConfigEntry<T>> function) {
		ConfigEntry.super.apply(function);
		return this;
	}

	Map<String, ConfigEntry<?>> subEntries();
}
