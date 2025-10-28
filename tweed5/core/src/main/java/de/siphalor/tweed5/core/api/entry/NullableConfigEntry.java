package de.siphalor.tweed5.core.api.entry;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface NullableConfigEntry<T extends @Nullable Object> extends StructuredConfigEntry<T> {
	String NON_NULL_KEY = ":nonNull";

	@Override
	default NullableConfigEntry<T> apply(Consumer<ConfigEntry<T>> function) {
		StructuredConfigEntry.super.apply(function);
		return this;
	}

	ConfigEntry<T> nonNullEntry();
}
