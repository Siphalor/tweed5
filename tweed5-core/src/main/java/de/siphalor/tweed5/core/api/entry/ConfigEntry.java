package de.siphalor.tweed5.core.api.entry;

import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import org.jetbrains.annotations.NotNull;

public interface ConfigEntry<T> {
	Class<T> valueClass();

	void seal(ConfigContainer<?> container);
	boolean sealed();

	EntryExtensionsData extensionsData();

	void visitInOrder(ConfigEntryVisitor visitor);
	void visitInOrder(ConfigEntryValueVisitor visitor, T value);

	@NotNull
	T deepCopy(@NotNull T value);
}
