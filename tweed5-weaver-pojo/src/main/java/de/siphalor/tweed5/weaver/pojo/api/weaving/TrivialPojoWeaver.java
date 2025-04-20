package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import org.jetbrains.annotations.Nullable;

public class TrivialPojoWeaver implements TweedPojoWeaver {
	@Override
	public void setup(SetupContext context) {
		// nothing to set up here
	}

	@Override
	public @Nullable <T> ConfigEntry<T> weaveEntry(ActualType<T> valueType, WeavingContext context) {
		SimpleConfigEntryImpl<T> entry = new SimpleConfigEntryImpl<>(valueType.declaredType());
		entry.seal(context.configContainer());
		return entry;
	}
}
