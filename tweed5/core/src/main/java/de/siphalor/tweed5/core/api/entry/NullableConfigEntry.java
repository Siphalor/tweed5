package de.siphalor.tweed5.core.api.entry;

import de.siphalor.tweed5.core.api.Arity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface NullableConfigEntry<T extends @Nullable Object> extends StructuredConfigEntry<T> {
	String NON_NULL_KEY = ":nonNull";

	@Override
	default NullableConfigEntry<T> apply(Consumer<ConfigEntry<T>> function) {
		StructuredConfigEntry.super.apply(function);
		return this;
	}

	ConfigEntry<T> nonNullEntry();

	@Override
	default void visitInOrder(ConfigEntryVisitor visitor) {
		if (visitor.enterStructuredEntry(this)) {
			subEntries().forEach((key, entry) -> {
				if (visitor.enterStructuredSubEntry(key, Arity.OPTIONAL)) {
					entry.visitInOrder(visitor);
					visitor.leaveStructuredSubEntry(key, Arity.OPTIONAL);
				}
			});
			visitor.leaveStructuredEntry(this);
		}
	}
}
