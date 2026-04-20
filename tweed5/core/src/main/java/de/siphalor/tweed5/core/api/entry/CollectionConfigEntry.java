package de.siphalor.tweed5.core.api.entry;

import de.siphalor.tweed5.core.api.Arity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public interface CollectionConfigEntry<E, T extends Collection<E>> extends StructuredConfigEntry<T> {
	String ELEMENT_KEY = "element";

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
				SubEntryKey subEntryKey = SubEntryKey.structured(ELEMENT_KEY, Integer.toString(index));
				if (visitor.enterSubEntry(subEntryKey)) {
					elementEntry().visitInOrder(visitor, item);
					visitor.leaveSubEntry(subEntryKey);
				}
				index++;
			}
			visitor.leaveStructuredEntry(this, value);
		}
	}

	@Override
	default Map<String, ConfigEntry<?>> subEntries() {
		return Collections.singletonMap(ELEMENT_KEY, elementEntry());
	}

	ConfigEntry<E> elementEntry();

	@NonNull T instantiateCollection(int size);
}
