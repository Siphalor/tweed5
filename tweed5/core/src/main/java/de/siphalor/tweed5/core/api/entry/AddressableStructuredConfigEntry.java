package de.siphalor.tweed5.core.api.entry;

import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public interface AddressableStructuredConfigEntry<T> extends StructuredConfigEntry<T> {
	@Override
	default AddressableStructuredConfigEntry<T> apply(Consumer<ConfigEntry<T>> function) {
		StructuredConfigEntry.super.apply(function);
		return this;
	}

	@Nullable ConfigEntry<?> getEntry(String dataKey);

	Object get(T value, String dataKey);
}
