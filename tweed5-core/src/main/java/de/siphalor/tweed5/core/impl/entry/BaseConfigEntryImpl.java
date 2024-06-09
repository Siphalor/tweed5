package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
abstract class BaseConfigEntryImpl<T> implements ConfigEntry<T> {

	@NotNull
	private final Class<T> valueClass;
	private ConfigContainer<?> container;
	private EntryExtensionsData extensionsData;
	private boolean sealed;

	@Override
	public void seal(ConfigContainer<?> container) {
		requireUnsealed();

		this.container = container;
		this.extensionsData = container.createExtensionsData();
		sealed = true;
	}

	protected void requireUnsealed() {
		if (sealed) {
			throw new IllegalStateException("Config entry is already sealed!");
		}
	}
}
