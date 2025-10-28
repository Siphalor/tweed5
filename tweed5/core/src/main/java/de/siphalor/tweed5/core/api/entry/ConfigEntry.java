package de.siphalor.tweed5.core.api.entry;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ConfigEntry<T extends @Nullable Object> {

	ConfigContainer<?> container();

	Class<T> valueClass();

	Patchwork extensionsData();

	default ConfigEntry<T> apply(Consumer<ConfigEntry<T>> function) {
		function.accept(this);
		return this;
	}

	default <R> R call(Function<ConfigEntry<T>, R> function) {
		return function.apply(this);
	}

	void visitInOrder(ConfigEntryVisitor visitor);
	void visitInOrder(ConfigEntryValueVisitor visitor, T value);

	T deepCopy(T value);
}
