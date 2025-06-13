package de.siphalor.tweed5.core.api.entry;

import java.util.Map;
import java.util.function.Consumer;

public interface CompoundConfigEntry<T> extends ConfigEntry<T> {
	@Override
	default CompoundConfigEntry<T> apply(Consumer<ConfigEntry<T>> function) {
		ConfigEntry.super.apply(function);
		return this;
	}

	Map<String, ConfigEntry<?>> subEntries();

	<V> void set(T compoundValue, String key, V value);
	<V> V get(T compoundValue, String key);

	T instantiateCompoundValue();
}
