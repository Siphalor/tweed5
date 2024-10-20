package de.siphalor.tweed5.weaver.pojo.impl.entry;

import de.siphalor.tweed5.core.api.entry.BaseConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCompoundConfigEntry;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StaticPojoCompoundConfigEntry<T> extends BaseConfigEntry<T> implements WeavableCompoundConfigEntry<T> {
	private final MethodHandle noArgsConstructor;
	private final Map<String, SubEntry> subEntries = new HashMap<>();
	private final Map<String, ConfigEntry<?>> subConfigEntries = new HashMap<>();

	public StaticPojoCompoundConfigEntry(@NotNull Class<T> valueClass, @NotNull MethodHandle noArgsConstructor) {
		super(valueClass);
		this.noArgsConstructor = noArgsConstructor;
	}

	public void registerSubEntry(SubEntry subEntry) {
		requireUnsealed();

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
			//noinspection unchecked
			return (T) noArgsConstructor.invokeExact();
		} catch (Throwable e) {
			throw new IllegalStateException("Failed to instantiate compound class", e);
		}
	}

	@Override
	public void visitInOrder(ConfigEntryVisitor visitor) {
		if (visitor.enterCompoundEntry(this)) {
			subConfigEntries.forEach((key, entry) -> {
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
			subEntries.forEach((key, entry) -> {
				if (visitor.enterCompoundSubEntry(key)) {
					try {
						Object subValue = entry.getter().invokeExact(value);
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
	public @NotNull T deepCopy(@NotNull T value) {
		T copy = instantiateCompoundValue();
		for (SubEntry subEntry : subEntries.values()) {
			try {
				Object subValue = subEntry.getter().invokeExact(value);
				subEntry.setter().invoke(copy, subValue);
			} catch (Throwable e) {
				throw new RuntimeException("Failed to copy value of sub entry \"" + subEntry.name() + "\"", e);
			}
		}
		return copy;
	}
}
