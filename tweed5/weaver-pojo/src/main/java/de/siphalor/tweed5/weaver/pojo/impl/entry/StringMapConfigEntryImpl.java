package de.siphalor.tweed5.weaver.pojo.impl.entry;

import de.siphalor.tweed5.construct.api.ConstructParameter;
import de.siphalor.tweed5.core.api.Arity;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.BaseConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import de.siphalor.tweed5.core.api.entry.SubEntryKey;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableStringMapConfigEntry;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class StringMapConfigEntryImpl<V, M extends Map<String, V>>
		extends BaseConfigEntry<M>
		implements WeavableStringMapConfigEntry<V, M> {
	private final Supplier<M> mapConstructor;
	private final ConfigEntry<V> valueEntry;

	public StringMapConfigEntryImpl(
			ConfigContainer<?> container,
			@ConstructParameter(name = "mapClass") Class<M> mapClass,
			@ConstructParameter(name = "mapConstructor") Supplier<M> mapConstructor,
			@ConstructParameter(name = "valueEntry") ConfigEntry<V> valueEntry
	) {
		super(container, mapClass);
		this.mapConstructor = mapConstructor;
		this.valueEntry = valueEntry;
	}

	@Override
	public M instantiateValue() {
		return mapConstructor.get();
	}

	@Override
	public void set(M value, String dataKey, Object subValue) {
		//noinspection unchecked
		value.put(dataKey, (V) subValue);
	}

	@Override
	public @Nullable ConfigEntry<?> getEntry(String dataKey) {
		return valueEntry;
	}

	@Override
	public Object get(M value, String dataKey) {
		return value.get(dataKey);
	}

	@Override
	public Map<String, ConfigEntry<?>> subEntries() {
		return Collections.singletonMap(":value", valueEntry);
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
	public void visitInOrder(ConfigEntryValueVisitor visitor, M value) {
		if (value == null) {
			return;
		}
		if (visitor.enterStructuredEntry(this, value)) {
			for (Map.Entry<String, V> entry : value.entrySet()) {
				SubEntryKey subEntryKey = SubEntryKey.addressable(":value", entry.getKey(), entry.getKey());
				if (visitor.enterSubEntry(subEntryKey)) {
					valueEntry.visitInOrder(visitor, entry.getValue());
					visitor.leaveSubEntry(subEntryKey);
				}
			}
			visitor.leaveStructuredEntry(this, value);
		}
	}

	@Override
	public M deepCopy(M value) {
		if (value == null) {
			return null;
		}
		M copy = mapConstructor.get();
		for (Map.Entry<String, V> entry : value.entrySet()) {
			copy.put(entry.getKey(), valueEntry.deepCopy(entry.getValue()));
		}
		return copy;
	}
}
