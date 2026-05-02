package de.siphalor.tweed5.serde.extension.impl;

import de.siphalor.tweed5.core.api.Arity;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.BaseConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import de.siphalor.tweed5.core.api.entry.MutableStructuredConfigEntry;
import de.siphalor.tweed5.core.api.entry.SubEntryKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestMapConfigEntry<V>
		extends BaseConfigEntry<Map<String, V>>
		implements MutableStructuredConfigEntry<Map<String, V>> {
	private final ConfigEntry<V> valueEntry;

	public TestMapConfigEntry(
			ConfigContainer<?> container,
			Class<Map<String, V>> valueClass,
			ConfigEntry<V> valueEntry
	) {
		super(container, valueClass);
		this.valueEntry = valueEntry;
	}

	@Override
	public @NonNull Map<String, V> instantiateValue() {
		return new LinkedHashMap<>();
	}

	@Override
	public void set(Map<String, V> value, String dataKey, Object subValue) {
		value.put(dataKey, (V) subValue);
	}

	@Override
	public @Nullable ConfigEntry<?> getEntry(String dataKey) {
		return valueEntry;
	}

	@Override
	public Object get(Map<String, V> value, String dataKey) {
		return value.get(dataKey);
	}

	@Override
	public Map<String, ConfigEntry<?>> subEntries() {
		return Map.of(":value", valueEntry);
	}

	@Override
	public void visitInOrder(ConfigEntryVisitor visitor) {
		if (visitor.enterStructuredEntry(this)) {
			if (visitor.enterStructuredSubEntry(":value", Arity.ANY)) {
				valueEntry.visitInOrder(visitor);
				visitor.leaveStructuredSubEntry(":value", Arity.ANY);
			}
			visitor.leaveStructuredEntry(this);
		}
	}

	@Override
	public void visitInOrder(ConfigEntryValueVisitor visitor, Map<String, V> value) {
		if (visitor.enterStructuredEntry(this, value)) {
			value.forEach((key, subValue) -> {
				SubEntryKey subEntryKey = SubEntryKey.addressable(":value", key, key);
				if (visitor.enterSubEntry(subEntryKey)) {
					valueEntry.visitInOrder(visitor, subValue);
					visitor.leaveSubEntry(subEntryKey);
				}
			});
			visitor.leaveStructuredEntry(this, value);
		}
	}

	@Override
	public Map<String, V> deepCopy(Map<String, V> value) {
		Map<String, V> copy = instantiateValue();
		value.forEach((key, subValue) -> copy.put(key, valueEntry.deepCopy(subValue)));
		return copy;
	}
}
