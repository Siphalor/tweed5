package de.siphalor.tweed5.attributesextension.api.serde.filter;

import de.siphalor.tweed5.attributesextension.impl.serde.filter.AttributesReadWriteFilterExtensionImpl;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.patchwork.api.Patchwork;

public interface AttributesReadWriteFilterExtension extends TweedExtension {
	Class<? extends AttributesReadWriteFilterExtension> DEFAULT = AttributesReadWriteFilterExtensionImpl.class;
	String EXTENSION_ID = "attributes-read-write-filter";

	default String getId() {
		return EXTENSION_ID;
	}

	void markAttributeForFiltering(String key);

	void addFilter(Patchwork contextExtensionsData, String key, String value);
}
