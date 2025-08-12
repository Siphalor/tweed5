package de.siphalor.tweed5.core.api.entry;

import java.util.function.Consumer;

public interface CompoundConfigEntry<T> extends StructuredConfigEntry<T> {
	@Override
	default CompoundConfigEntry<T> apply(Consumer<ConfigEntry<T>> function) {
		StructuredConfigEntry.super.apply(function);
		return this;
	}

	<V> void set(T compoundValue, String key, V value);
	<V> V get(T compoundValue, String key);

	T instantiateCompoundValue();
}
