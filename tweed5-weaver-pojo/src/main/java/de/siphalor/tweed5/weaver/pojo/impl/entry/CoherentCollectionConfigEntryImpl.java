package de.siphalor.tweed5.weaver.pojo.impl.entry;

import de.siphalor.tweed5.core.api.entry.BaseConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCoherentCollectionConfigEntry;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.IntFunction;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CoherentCollectionConfigEntryImpl<E, T extends Collection<E>> extends BaseConfigEntry<T> implements WeavableCoherentCollectionConfigEntry<E, T> {
	private final IntFunction<T> constructor;
	private ConfigEntry<E> elementEntry;

	public CoherentCollectionConfigEntryImpl(@NotNull Class<T> valueClass, IntFunction<T> constructor) {
		super(valueClass);
		this.constructor = constructor;
	}

	@Override
	public void elementEntry(ConfigEntry<E> elementEntry) {
		this.elementEntry = elementEntry;
	}

	@Override
	public T instantiateCollection(int size) {
		try {
			return constructor.apply(size);
		} catch (Throwable e) {
			throw new IllegalStateException("Failed to instantiate collection class", e);
		}
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
			for (E element : value) {
				elementEntry.visitInOrder(visitor, element);
			}
			visitor.leaveCollectionEntry(this, value);
		}
	}

	@Override
	public @NotNull T deepCopy(@NotNull T value) {
		T copy = instantiateCollection(value.size());
		for (E element : value) {
			copy.add(elementEntry.deepCopy(element));
		}
		return copy;
	}
}
