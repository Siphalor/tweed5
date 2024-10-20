package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import org.jetbrains.annotations.Nullable;

public class TrivialPojoWeaver implements TweedPojoWeaver {
	@Override
	public void setup(SetupContext context) {
		// nothing to set up here
	}

	@Override
	public @Nullable <T> ConfigEntry<T> weaveEntry(Class<T> valueClass, WeavingContext context) {
		return new SimpleConfigEntryImpl<>(valueClass);
	}
}
