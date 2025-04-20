package de.siphalor.tweed5.core.api.entry;

import java.util.Collection;

public interface CollectionConfigEntry<E, T extends Collection<E>> extends ConfigEntry<T> {
	ConfigEntry<E> elementEntry();

	T instantiateCollection(int size);
}
