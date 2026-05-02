package de.siphalor.tweed5.weaver.pojo.api.entry;

import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.MutableStructuredConfigEntry;

import java.util.Map;
import java.util.function.Supplier;

public interface WeavableStringMapConfigEntry<V, M extends Map<String, V>> extends MutableStructuredConfigEntry<M> {
	@SuppressWarnings("rawtypes")
	TweedConstructFactory<WeavableStringMapConfigEntry> FACTORY =
			TweedConstructFactory.builder(WeavableStringMapConfigEntry.class)
					.typedArg(ConfigContainer.class)
					.namedArg("mapClass", Class.class) // map class
					.namedArg("mapConstructor", Supplier.class) // map constructor
					.namedArg("valueEntry", ConfigEntry.class)
					.build();
}
