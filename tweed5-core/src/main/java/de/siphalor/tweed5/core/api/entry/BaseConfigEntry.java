package de.siphalor.tweed5.core.api.entry;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import lombok.Getter;

@Getter
public abstract class BaseConfigEntry<T> implements ConfigEntry<T> {
	private final ConfigContainer<?> container;
	private final Class<T> valueClass;
	private final EntryExtensionsData extensionsData;

	public BaseConfigEntry(ConfigContainer<?> container, Class<T> valueClass) {
		this.container = container;
		this.valueClass = valueClass;
		this.extensionsData = container.createExtensionsData();
	}
}
