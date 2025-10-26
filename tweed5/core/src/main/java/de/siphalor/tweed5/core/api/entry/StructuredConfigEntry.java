package de.siphalor.tweed5.core.api.entry;

import java.util.Map;
import java.util.function.Consumer;

public interface StructuredConfigEntry<T> extends ConfigEntry<T> {
	@Override
	default StructuredConfigEntry<T> apply(Consumer<ConfigEntry<T>> function) {
		ConfigEntry.super.apply(function);
		return this;
	}

	@Override
	default void visitInOrder(ConfigEntryVisitor visitor) {
		if (visitor.enterStructuredEntry(this)) {
			subEntries().forEach((key, entry) -> {
				if (visitor.enterStructuredSubEntry(key)) {
					entry.visitInOrder(visitor);
					visitor.leaveStructuredSubEntry(key);
				}
			});
			visitor.leaveStructuredEntry(this);
		}
	}

	Map<String, ConfigEntry<?>> subEntries();
}
