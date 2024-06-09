package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.entry.CoherentCollectionConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import org.jetbrains.annotations.NotNull;

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
	public @NotNull T deepCopy(@NotNull T value) {
		T copy = collectionConstructor.apply(value.size());
		for (E element : value) {
			copy.add(elementEntry().deepCopy(element));
		}
		return copy;
	}
}
