package de.siphalor.tweed5.weaver.pojo.impl.entry;

import de.siphalor.tweed5.construct.api.ConstructParameter;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.BaseConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCompoundConfigEntry;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StaticPojoCompoundConfigEntry<T> extends BaseConfigEntry<T> implements WeavableCompoundConfigEntry<T> {
	private final Supplier<T> noArgsConstructor;
	private final Map<String, SubEntry> subEntries;
	private final Map<String, ConfigEntry<?>> subConfigEntries;

	public StaticPojoCompoundConfigEntry(
			ConfigContainer<?> configContainer,
			Class<T> valueClass,
			Supplier<T> noArgsConstructor,
			@ConstructParameter(name = "subEntries") List<SubEntry> subEntries
	) {
		super(configContainer, valueClass);
		this.noArgsConstructor = noArgsConstructor;
		this.subEntries = new LinkedHashMap<>(subEntries.size(), 1);
		this.subConfigEntries = new LinkedHashMap<>(subEntries.size(), 1);
		for (SubEntry subEntry : subEntries) {
			this.subEntries.put(subEntry.name(), subEntry);
			this.subConfigEntries.put(subEntry.name(), subEntry.configEntry());
		}
	}

	public void registerSubEntry(SubEntry subEntry) {
		subEntries.put(subEntry.name(), subEntry);
		subConfigEntries.put(subEntry.name(), subEntry.configEntry());
	}

	@Override
	public Map<String, ConfigEntry<?>> subEntries() {
		return Collections.unmodifiableMap(subConfigEntries);
	}

	@Override
	public <V> void set(T compoundValue, String key, V value) {
		SubEntry subEntry = subEntries.get(key);
		if (subEntry == null) {
			throw new IllegalArgumentException("Unknown config entry: " + key);
		}

		try {
			subEntry.setter().invoke(compoundValue, value);
		} catch (Throwable e) {
			throw new IllegalStateException("Failed to set value for config entry \"" + key + "\"", e);
		}
	}

	@Override
	public <V> V get(T compoundValue, String key) {
		SubEntry subEntry = subEntries.get(key);
		if (subEntry == null) {
			throw new IllegalArgumentException("Unknown config entry: " + key);
		}

		try {
			//noinspection unchecked
			return (V) subEntry.getter().invoke(compoundValue);
		} catch (Throwable e) {
			throw new IllegalStateException("Failed to get value for config entry \"" + key + "\"", e);
		}
	}

	@Override
	public T instantiateCompoundValue() {
		try {
			return noArgsConstructor.get();
		} catch (Throwable e) {
			throw new IllegalStateException("Failed to instantiate compound class", e);
		}
	}

	@Override
	public void visitInOrder(ConfigEntryVisitor visitor) {
		if (visitor.enterStructuredEntry(this)) {
			subConfigEntries.forEach((key, entry) -> {
				if (visitor.enterStructuredSubEntry(key)) {
					entry.visitInOrder(visitor);
					visitor.leaveStructuredSubEntry(key);
				}
			});
			visitor.leaveStructuredEntry(this);
		}
	}

	@Override
	public void visitInOrder(ConfigEntryValueVisitor visitor, @Nullable T value) {
		if (visitor.enterStructuredEntry(this, value)) {
			subEntries.forEach((key, entry) -> {
				if (visitor.enterStructuredSubEntry(key, key)) {
					try {
						Object subValue = entry.getter().invoke(value);
						//noinspection unchecked
						visitor.visitEntry((ConfigEntry<Object>) entry.configEntry(), subValue);
					} catch (Throwable e) {
						throw new RuntimeException("Failed to get compound sub entry value \"" + key + "\"");
					}
				}
			});
		}
	}

	@Override
	public T deepCopy(T value) {
		T copy = instantiateCompoundValue();
		for (SubEntry subEntry : subEntries.values()) {
			try {
				Object subValue = subEntry.getter().invoke(value);
				subEntry.setter().invoke(copy, subValue);
			} catch (Throwable e) {
				throw new RuntimeException("Failed to copy value of sub entry \"" + subEntry.name() + "\"", e);
			}
		}
		return copy;
	}
}
