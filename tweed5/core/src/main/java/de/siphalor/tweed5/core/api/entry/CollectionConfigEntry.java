package de.siphalor.tweed5.core.api.entry;

import de.siphalor.tweed5.core.api.Arity;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public interface CollectionConfigEntry<E, T extends Collection<E>> extends StructuredConfigEntry<T> {
	@Override
	default CollectionConfigEntry<E, T> apply(Consumer<ConfigEntry<T>> function) {
		StructuredConfigEntry.super.apply(function);
		return this;
	}

	@Override
	default void visitInOrder(ConfigEntryVisitor visitor) {
		if (visitor.enterStructuredEntry(this)) {
			subEntries().forEach((key, entry) -> {
				if (visitor.enterStructuredSubEntry(key, Arity.ANY)) {
					entry.visitInOrder(visitor);
					visitor.leaveStructuredSubEntry(key, Arity.ANY);
				}
			});
			visitor.leaveStructuredEntry(this);
		}
	}

	@Override
	default void visitInOrder(ConfigEntryValueVisitor visitor, @Nullable T value) {
		if (value == null) {
			return;
		}

		if (visitor.enterStructuredEntry(this, value)) {
			int index = 0;
			for (E item : value) {
				String indexString = Integer.toString(index);
				if (visitor.enterStructuredSubEntry("element", indexString)) {
					elementEntry().visitInOrder(visitor, item);
					visitor.leaveStructuredSubEntry("element", indexString);
				}
				index++;
			}
			visitor.leaveStructuredEntry(this, value);
		}
	}

	@Override
	default Map<String, ConfigEntry<?>> subEntries() {
		return Collections.singletonMap("element", elementEntry());
	}

	ConfigEntry<E> elementEntry();

	T instantiateCollection(int size);
}
