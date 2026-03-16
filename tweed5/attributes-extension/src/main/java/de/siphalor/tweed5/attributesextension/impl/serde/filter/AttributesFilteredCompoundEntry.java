package de.siphalor.tweed5.attributesextension.impl.serde.filter;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@RequiredArgsConstructor
public class AttributesFilteredCompoundEntry<T extends @Nullable Object> implements CompoundConfigEntry<T> {
	private final CompoundConfigEntry<T> delegate;

	@Override
	public void set(T compoundValue, String key, Object value) {
		if (value == AttributesReadWriteFilterExtensionImpl.NOOP_MARKER) {
			return;
		}
		delegate.set(compoundValue, key, value);
	}

	@Override
	public Object get(T compoundValue, String key) {
		return delegate.get(compoundValue, key);
	}

	@Override
	public @NonNull T instantiateValue() {
		return delegate.instantiateValue();
	}

	@Override
	public Map<String, ConfigEntry<?>> subEntries() {
		return delegate.subEntries();
	}

	@Override
	public ConfigContainer<?> container() {
		return delegate.container();
	}

	@Override
	public Class<T> valueClass() {
		return delegate.valueClass();
	}

	@Override
	public Patchwork extensionsData() {
		return delegate.extensionsData();
	}

	@Override
	public void visitInOrder(ConfigEntryValueVisitor visitor, T value) {
		delegate.visitInOrder(visitor, value);
	}

	@Override
	public void visitInOrder(ConfigEntryVisitor visitor) {
		delegate.visitInOrder(visitor);
	}

	@Override
	public T deepCopy(T value) {
		return delegate.deepCopy(value);
	}
}
