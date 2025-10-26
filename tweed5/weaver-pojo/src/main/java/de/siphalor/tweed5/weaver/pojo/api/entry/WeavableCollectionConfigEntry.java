package de.siphalor.tweed5.weaver.pojo.api.entry;

import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.CollectionConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;

import java.util.Collection;
import java.util.function.IntFunction;

/**
 * {@inheritDoc}
 * <br />
 * A constructor taking the value {@link Class}
 * and a {@link java.util.function.IntFunction} that allows to instantiate the value class with a single capacity argument.
 */
public interface WeavableCollectionConfigEntry<E, T extends Collection<E>> extends CollectionConfigEntry<E, T> {
	@SuppressWarnings("rawtypes")
	TweedConstructFactory<WeavableCollectionConfigEntry> FACTORY =
			TweedConstructFactory.builder(WeavableCollectionConfigEntry.class)
					.typedArg(ConfigContainer.class)
					.typedArg(Class.class) // value class
					.typedArg(IntFunction.class) // value class constructor with capacity
					.namedArg("elementEntry", ConfigEntry.class)
					.build();
}
