package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import lombok.Getter;
import lombok.Value;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ReflectiveCompoundConfigEntryImpl<T> extends BaseConfigEntryImpl<T> implements CompoundConfigEntry<T> {
	private final Constructor<T> noArgsConstructor;
	private final Map<String, CompoundEntry> compoundEntries;

	public ReflectiveCompoundConfigEntryImpl(Class<T> valueClass) {
		super(valueClass);
		try {
			this.noArgsConstructor = valueClass.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Value class must have a no-arg constructor", e);
		}
		this.compoundEntries = new LinkedHashMap<>();
	}

	public void addSubEntry(String name, Field field, ConfigEntry<?> configEntry) {
		requireUnsealed();

		if (field.getType() != valueClass()) {
			throw new IllegalArgumentException("Field is not defined on the correct type");
		}

		//noinspection unchecked
		compoundEntries.put(name, new CompoundEntry(name, field, (ConfigEntry<Object>) configEntry));
	}

	public Map<String, ConfigEntry<?>> subEntries() {
		return compoundEntries.values().stream().collect(Collectors.toMap(CompoundEntry::name, CompoundEntry::configEntry));
	}

	@Override
	public <V> void set(T compoundValue, String key, V value) {
		CompoundEntry compoundEntry = compoundEntries.get(key);
		if (compoundEntry == null) {
			throw new IllegalArgumentException("Unknown config entry: " + key);
		}

		try {
			compoundEntry.field().set(compoundValue, value);

		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public <V> V get(T compoundValue, String key) {
		CompoundEntry compoundEntry = compoundEntries.get(key);
		if (compoundEntry == null) {
			throw new IllegalArgumentException("Unknown config entry: " + key);
		}

		try {
			//noinspection unchecked
			return (V) compoundEntry.field().get(compoundValue);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public T instantiateCompoundValue() {
		try {
			return noArgsConstructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException("Failed to instantiate compound value", e);
		}
	}

	@Override
	public void visitInOrder(ConfigEntryVisitor visitor) {
		if (visitor.enterCompoundEntry(this)) {
			for (Map.Entry<String, CompoundEntry> entry : compoundEntries.entrySet()) {
				if (visitor.enterCompoundSubEntry(entry.getKey())) {
					entry.getValue().configEntry().visitInOrder(visitor);
					visitor.leaveCompoundSubEntry(entry.getKey());
				}
			}
			visitor.leaveCompoundEntry(this);
		}
	}

	@Override
	public void visitInOrder(ConfigEntryValueVisitor visitor, T value) {
		if (visitor.enterCompoundEntry(this, value)) {
			compoundEntries.forEach((key, entry) -> {
				if (visitor.enterCompoundSubEntry(key)) {
					try {
						visitor.visitEntry(entry.configEntry(), entry.field().get(value));
					} catch (IllegalAccessException ignored) {
						// ignored
					}
					visitor.leaveCompoundSubEntry(key);
				}
			});
			visitor.leaveCompoundEntry(this, value);
		}
	}

	@Value
	public static class CompoundEntry {
		String name;
		Field field;
		ConfigEntry<Object> configEntry;
	}
}
