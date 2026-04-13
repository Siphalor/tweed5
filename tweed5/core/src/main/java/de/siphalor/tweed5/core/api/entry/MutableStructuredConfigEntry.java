package de.siphalor.tweed5.core.api.entry;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public interface MutableStructuredConfigEntry<T> extends AddressableStructuredConfigEntry<T> {
	@Override
	default AddressableStructuredConfigEntry<T> apply(Consumer<ConfigEntry<T>> function) {
		AddressableStructuredConfigEntry.super.apply(function);
		return this;
	}

	@NonNull T instantiateValue();
	void set(T value, String dataKey, Object subValue);
}
