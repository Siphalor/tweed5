package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.*;

import java.util.Collection;
import java.util.function.IntFunction;

public class CollectionConfigEntryImpl<E, T extends Collection<E>> extends BaseConfigEntry<T> implements CollectionConfigEntry<E, T> {
	private final IntFunction<T> collectionConstructor;
	private final ConfigEntry<E> elementEntry;

	public CollectionConfigEntryImpl(
			ConfigContainer<?> container,
			Class<T> valueClass,
			IntFunction<T> collectionConstructor,
			ConfigEntry<E> elementEntry
	) {
		super(container, valueClass);
		this.collectionConstructor = collectionConstructor;
		this.elementEntry = elementEntry;
	}

	@Override
	public ConfigEntry<E> elementEntry() {
		return elementEntry;
	}

	@Override
	public T instantiateCollection(int size) {
		return collectionConstructor.apply(size);
	}

	@Override
	public void visitInOrder(ConfigEntryVisitor visitor) {
		if (visitor.enterCollectionEntry(this)) {
			elementEntry.visitInOrder(visitor);
			visitor.leaveCollectionEntry(this);
		}
	}

	@Override
	public void visitInOrder(ConfigEntryValueVisitor visitor, T value) {
		if (visitor.enterCollectionEntry(this, value)) {
			if (value != null) {
				for (E element : value) {
					visitor.visitEntry(elementEntry, element);
				}
			}
			visitor.leaveCollectionEntry(this, value);
		}
	}

	@Override
	public T deepCopy(T value) {
		T copy = collectionConstructor.apply(value.size());
		for (E element : value) {
			copy.add(elementEntry().deepCopy(element));
		}
		return copy;
	}
}
