package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.*;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntFunction;

public class StaticMapCompoundConfigEntryImpl<T extends @NonNull Map<String, Object>> extends BaseConfigEntry<T> implements CompoundConfigEntry<T> {
	private final IntFunction<T> mapConstructor;
	private final Map<String, ConfigEntry<?>> compoundEntries;

	public StaticMapCompoundConfigEntryImpl(
			ConfigContainer<?> container,
			Class<T> valueClass,
			IntFunction<T> mapConstructor,
			Map<String, ConfigEntry<?>> compoundEntries
	) {
		super(container, valueClass);
		this.mapConstructor = mapConstructor;
		this.compoundEntries = new LinkedHashMap<>(compoundEntries);
	}

	@Override
	public Map<String, ConfigEntry<?>> subEntries() {
		return compoundEntries;
	}

	@Override
	public void set(T compoundValue, String dataKey, Object value) {
		requireKey(dataKey);
		compoundValue.put(dataKey, value);
	}

	@Override
	public Object get(T compoundValue, String dataKey) {
		requireKey(dataKey);
		return compoundValue.get(dataKey);
	}

	private void requireKey(String key) {
		if (!compoundEntries.containsKey(key)) {
			throw new IllegalArgumentException("Key " + key + " does not exist on this compound entry!");
		}
	}

	@Override
	public T instantiateValue() {
		return mapConstructor.apply(compoundEntries.size());
	}

	@Override
	public void visitInOrder(ConfigEntryValueVisitor visitor, T value) {
		if (visitor.enterStructuredEntry(this, value)) {
			compoundEntries.forEach((key, entry) -> {
				SubEntryKey subEntryKey = SubEntryKey.addressable(key, key, key);
				if (visitor.enterSubEntry(subEntryKey)) {
					//noinspection unchecked
					((ConfigEntry<Object>) entry).visitInOrder(visitor, value.get(key));
					visitor.leaveSubEntry(subEntryKey);
				}
			});
			visitor.leaveStructuredEntry(this, value);
		}
	}

	@Override
	public T deepCopy(T value) {
		T copy = instantiateValue();
		value.forEach((String key, Object element) -> {
			//noinspection unchecked
			ConfigEntry<Object> elementEntry = (ConfigEntry<Object>) compoundEntries.get(key);
			if (elementEntry != null) {
				copy.put(key, elementEntry.deepCopy(element));
			}
		});
		return copy;
	}
}
