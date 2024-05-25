package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.entry.SimpleConfigEntry;

public class SimpleConfigEntryImpl<T> extends BaseConfigEntryImpl<T> implements SimpleConfigEntry<T> {
	public SimpleConfigEntryImpl(Class<T> valueClass) {
		super(valueClass);
	}
}
