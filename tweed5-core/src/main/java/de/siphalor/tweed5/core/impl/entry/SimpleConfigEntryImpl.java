package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.BaseConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import de.siphalor.tweed5.core.api.entry.SimpleConfigEntry;
import org.jspecify.annotations.NonNull;

public class SimpleConfigEntryImpl<T> extends BaseConfigEntry<T> implements SimpleConfigEntry<T> {
	public SimpleConfigEntryImpl(ConfigContainer<?> container, Class<T> valueClass) {
		super(container, valueClass);
	}

	@Override
	public void visitInOrder(ConfigEntryVisitor visitor) {
		visitor.visitEntry(this);
	}

	@Override
	public void visitInOrder(ConfigEntryValueVisitor visitor, T value) {
		visitor.visitEntry(this, value);
	}

	@Override
	public T deepCopy(@NonNull T value) {
		return value;
	}
}
