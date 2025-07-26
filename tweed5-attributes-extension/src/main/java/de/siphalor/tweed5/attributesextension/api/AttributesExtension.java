package de.siphalor.tweed5.attributesextension.api;

import de.siphalor.tweed5.attributesextension.impl.AttributesExtensionImpl;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public interface AttributesExtension extends TweedExtension {
	Class<? extends AttributesExtension> DEFAULT = AttributesExtensionImpl.class;

	static <C extends ConfigEntry<?>> Consumer<C> attribute(String key, String value) {
		return entry -> entry.container().extension(AttributesExtension.class)
				.orElseThrow(() -> new IllegalStateException("No attributes extension registered"))
				.setAttribute(entry, key, value);
	}

	static <C extends ConfigEntry<?>> Consumer<C> attributeDefault(String key, String value) {
		return entry -> entry.container().extension(AttributesExtension.class)
				.orElseThrow(() -> new IllegalStateException("No attributes extension registered"))
				.setAttributeDefault(entry, key, value);
	}

	default void setAttribute(ConfigEntry<?> entry, String key, String value) {
		setAttribute(entry, key, Collections.singletonList(value));
	}
	void setAttribute(ConfigEntry<?> entry, String key, List<String> values);
	default void setAttributeDefault(ConfigEntry<?> entry, String key, String value) {
		setAttributeDefault(entry, key, Collections.singletonList(value));
	}
	void setAttributeDefault(ConfigEntry<?> entry, String key, List<String> values);

	List<String> getAttributeValues(ConfigEntry<?> entry, String key);
	@Nullable String getAttributeValue(ConfigEntry<?> entry, String key);
}
