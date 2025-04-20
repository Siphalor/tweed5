package de.siphalor.tweed5.weaver.pojo.api.entry;

import de.siphalor.tweed5.core.api.entry.CoherentCollectionConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.PojoWeavingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.function.IntFunction;

/**
 * {@inheritDoc}
 * <br />
 * A constructor taking the value {@link Class}
 * and a {@link java.util.function.IntFunction} that allows to instantiate the value class with a single capacity argument.
 */
public interface WeavableCoherentCollectionConfigEntry<E, T extends Collection<E>>
		extends CoherentCollectionConfigEntry<E, T> {
	static <E, T extends Collection<E>, C extends WeavableCoherentCollectionConfigEntry<E, T>> C instantiate(
			Class<C> weavableClass, Class<T> valueClass, IntFunction<T> constructor
	) throws PojoWeavingException {
		try {
			Constructor<C> weavableEntryConstructor = weavableClass.getConstructor(Class.class, IntFunction.class);
			return weavableEntryConstructor.newInstance(valueClass, constructor);
		} catch (NoSuchMethodException e) {
			throw new PojoWeavingException(
					"Class " + weavableClass.getName() + " must have constructor with value class and value constructor",
					e
			);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new PojoWeavingException(
					"Failed to instantiate class for weavable collection entry " + weavableClass.getName(),
					e
			);
		}
	}

	void elementEntry(ConfigEntry<E> elementEntry);
}
