package de.siphalor.tweed5.core.api.entry;

import java.util.Collection;
import java.util.function.Consumer;

public interface CollectionConfigEntry<E, T extends Collection<E>> extends ConfigEntry<T> {
	@Override
	default CollectionConfigEntry<E, T> apply(Consumer<ConfigEntry<T>> function) {
		ConfigEntry.super.apply(function);
		return this;
	}

	ConfigEntry<E> elementEntry();

	T instantiateCollection(int size);
}
