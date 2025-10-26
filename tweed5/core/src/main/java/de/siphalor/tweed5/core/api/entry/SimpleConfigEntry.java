package de.siphalor.tweed5.core.api.entry;

import java.util.function.Consumer;

public interface SimpleConfigEntry<T> extends ConfigEntry<T> {
	@Override
	default SimpleConfigEntry<T> apply(Consumer<ConfigEntry<T>> function) {
		ConfigEntry.super.apply(function);
		return this;
	}
}
