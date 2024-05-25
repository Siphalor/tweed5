package de.siphalor.tweed5.core.api.entry;

import java.util.Map;

public interface CompoundConfigEntry<T> extends ConfigEntry<T> {
	Map<String, ConfigEntry<?>> subEntries();

	<V> void set(T compoundValue, String key, V value);
	<V> V get(T compoundValue, String key);

	T instantiateCompoundValue();
}
