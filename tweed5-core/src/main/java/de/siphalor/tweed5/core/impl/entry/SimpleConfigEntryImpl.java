package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import de.siphalor.tweed5.core.api.entry.SimpleConfigEntry;
import org.jetbrains.annotations.NotNull;

public class SimpleConfigEntryImpl<T> extends BaseConfigEntryImpl<T> implements SimpleConfigEntry<T> {
	public SimpleConfigEntryImpl(Class<T> valueClass) {
		super(valueClass);
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
	@NotNull
	public T deepCopy(@NotNull T value) {
		return value;
	}
}
