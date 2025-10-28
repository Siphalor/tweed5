package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.BaseConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.NullableConfigEntry;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

@Getter
public class NullableConfigEntryImpl<T extends @Nullable Object> extends BaseConfigEntry<T> implements NullableConfigEntry<T> {
	private final ConfigEntry<T> nonNullEntry;

	public NullableConfigEntryImpl(
			ConfigContainer<?> container,
			Class<T> valueClass,
			ConfigEntry<T> nonNullEntry
	) {
		super(container, valueClass);
		this.nonNullEntry = nonNullEntry;
	}

	@Override
	public Map<String, ConfigEntry<?>> subEntries() {
		return Collections.singletonMap(NON_NULL_KEY, nonNullEntry);
	}

	@Override
	public void visitInOrder(ConfigEntryValueVisitor visitor, T value) {
		if (value != null) {
			if (visitor.enterStructuredEntry(this, value)) {
				if (visitor.enterStructuredSubEntry(NON_NULL_KEY, NON_NULL_KEY)) {
					nonNullEntry.visitInOrder(visitor, value);
					visitor.leaveStructuredSubEntry(NON_NULL_KEY, NON_NULL_KEY);
				}
				visitor.leaveStructuredEntry(this, value);
			}
		}
	}

	@Override
	public T deepCopy(T value) {
		if (value != null) {
			return nonNullEntry.deepCopy(value);
		}
		return null;
	}
}
