package de.siphalor.tweed5.weaver.pojo.api.entry;

import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.PojoWeavingException;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * {@inheritDoc}
 * <br />
 * A constructor taking the value {@link Class} and a {@link MethodHandle} that allows to instantiate the value Class with no arguments.
 */
public interface WeavableCompoundConfigEntry<T> extends CompoundConfigEntry<T> {
	static <T, C extends WeavableCompoundConfigEntry<T>> C instantiate(
			Class<C> weavableClass, Class<T> valueClass, MethodHandle constructorHandle
	) throws PojoWeavingException {
		try {
			Constructor<C> weavableEntryConstructor = weavableClass.getConstructor(Class.class, MethodHandle.class);
			return weavableEntryConstructor.newInstance(valueClass, constructorHandle);
		} catch (NoSuchMethodException e) {
			throw new PojoWeavingException(
					"Class " + weavableClass.getName() + " must have constructor with value class and value constructor",
					e
			);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new PojoWeavingException(
					"Failed to instantiate class for weavable compound entry " + weavableClass.getName(),
					e
			);
		}
	}

	void registerSubEntry(SubEntry subEntry);

	@Value
	@RequiredArgsConstructor
	class SubEntry {
		@NotNull String name;
		@NotNull ConfigEntry<?> configEntry;
		@NotNull MethodHandle getter;
		@NotNull MethodHandle setter;
	}
}
