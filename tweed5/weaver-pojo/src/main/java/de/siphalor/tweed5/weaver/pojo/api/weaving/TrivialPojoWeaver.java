package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.typeutils.api.type.ActualType;

public class TrivialPojoWeaver implements TweedPojoWeavingExtension {
	@Override
	public void setup(SetupContext context) {
		// nothing to set up here
	}

	@Override
	public <T> ConfigEntry<T> weaveEntry(ActualType<T> valueType, WeavingContext context) {
		return new SimpleConfigEntryImpl<>(context.configContainer(), valueType.declaredType());
	}
}
