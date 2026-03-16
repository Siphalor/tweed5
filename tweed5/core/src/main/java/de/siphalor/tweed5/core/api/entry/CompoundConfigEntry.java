package de.siphalor.tweed5.core.api.entry;

import de.siphalor.tweed5.core.api.Arity;

import java.util.function.Consumer;

public interface CompoundConfigEntry<T> extends MutableStructuredConfigEntry<T> {
	@Override
	default CompoundConfigEntry<T> apply(Consumer<ConfigEntry<T>> function) {
		MutableStructuredConfigEntry.super.apply(function);
		return this;
	}

	@Override
	default void visitInOrder(ConfigEntryVisitor visitor) {
		if (visitor.enterStructuredEntry(this)) {
			subEntries().forEach((key, entry) -> {
				if (visitor.enterStructuredSubEntry(key, Arity.SINGLE)) {
					entry.visitInOrder(visitor);
					visitor.leaveStructuredSubEntry(key, Arity.SINGLE);
				}
			});
			visitor.leaveStructuredEntry(this);
		}
	}
}
