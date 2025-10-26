package de.siphalor.tweed5.weaver.pojo.impl.entry;

import de.siphalor.tweed5.construct.api.ConstructParameter;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.BaseConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCollectionConfigEntry;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.IntFunction;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CollectionConfigEntryImpl<E, T extends Collection<E>> extends BaseConfigEntry<T> implements WeavableCollectionConfigEntry<E, T> {
	private final IntFunction<T> constructor;
	private final ConfigEntry<E> elementEntry;

	public CollectionConfigEntryImpl(
			ConfigContainer<?> configContainer,
			Class<T> valueClass,
			IntFunction<T> constructor,
			@ConstructParameter(name = "elementEntry") ConfigEntry<E> elementEntry
	) {
		super(configContainer, valueClass);
		this.constructor = constructor;
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
	public @NotNull T deepCopy(T value) {
		T copy = instantiateCollection(value.size());
		for (E element : value) {
			copy.add(elementEntry.deepCopy(element));
		}
		return copy;
	}
}
