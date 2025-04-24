package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.entry.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntFunction;

public class StaticMapCompoundConfigEntryImpl<T extends Map<String, Object>> extends BaseConfigEntry<T> implements CompoundConfigEntry<T> {
	private final IntFunction<T> mapConstructor;
	private final Map<String, ConfigEntry<?>> compoundEntries = new LinkedHashMap<>();

	public StaticMapCompoundConfigEntryImpl(Class<T> valueClass, IntFunction<T> mapConstructor) {
		super(valueClass);
		this.mapConstructor = mapConstructor;
	}

	public void addSubEntry(String key, ConfigEntry<?> entry) {
		requireUnsealed();
		compoundEntries.put(key, entry);
	}

	@Override
	public Map<String, ConfigEntry<?>> subEntries() {
		return compoundEntries;
	}

	@Override
	public <V> void set(T compoundValue, String key, V value) {
		requireKey(key);
		compoundValue.put(key, value);
	}

	@Override
	public <V> V get(T compoundValue, String key) {
		requireKey(key);
		//noinspection unchecked
		return (V) compoundValue.get(key);
	}

	private void requireKey(String key) {
		if (!compoundEntries.containsKey(key)) {
			throw new IllegalArgumentException("Key " + key + " does not exist on this compound entry!");
		}
	}

	@Override
	public T instantiateCompoundValue() {
		return mapConstructor.apply(compoundEntries.size());
	}

	@Override
	public void visitInOrder(ConfigEntryVisitor visitor) {
		if (visitor.enterCompoundEntry(this)) {
			compoundEntries.forEach((key, entry) -> {
				if (visitor.enterCompoundSubEntry(key)) {
					entry.visitInOrder(visitor);
					visitor.leaveCompoundSubEntry(key);
				}
			});
			visitor.leaveCompoundEntry(this);
		}
	}

	@Override
	public void visitInOrder(ConfigEntryValueVisitor visitor, T value) {
		if (visitor.enterCompoundEntry(this, value)) {
			if (value != null) {
				compoundEntries.forEach((key, entry) -> {
					if (visitor.enterCompoundSubEntry(key)) {
						//noinspection unchecked
						((ConfigEntry<Object>) entry).visitInOrder(visitor, value.get(key));
						visitor.leaveCompoundSubEntry(key);
					}
				});
			}
			visitor.leaveCompoundEntry(this, value);
		}
	}

	@Override
	public T deepCopy(T value) {
		T copy = instantiateCompoundValue();
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
