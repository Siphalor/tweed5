package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.entry.CoherentCollectionConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;

import java.util.Collection;
import java.util.function.IntFunction;

public class CoherentCollectionConfigEntryImpl<E, T extends Collection<E>> extends BaseConfigEntryImpl<T> implements CoherentCollectionConfigEntry<E, T> {
	private final IntFunction<T> collectionConstructor;
	private ConfigEntry<E> elementEntry;

	public CoherentCollectionConfigEntryImpl(Class<T> valueClass, IntFunction<T> collectionConstructor) {
		super(valueClass);
		this.collectionConstructor = collectionConstructor;
	}

	public void elementEntry(ConfigEntry<E> elementEntry) {
		requireUnsealed();

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
}
