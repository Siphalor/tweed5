package de.siphalor.tweed5.coat.bridge.api;

import de.siphalor.coat.screen.ConfigScreen;
import de.siphalor.tweed5.coat.bridge.api.mapping.TweedCoatMapper;
import de.siphalor.tweed5.coat.bridge.impl.TweedCoatBridgeExtensionImpl;
import de.siphalor.tweed5.core.api.extension.TweedExtension;

public interface TweedCoatBridgeExtension extends TweedExtension {
	Class<? extends TweedCoatBridgeExtension> DEFAULT = TweedCoatBridgeExtensionImpl.class;
	String EXTENSION_ID = "coat-bridge";

	//static <T> Function<ConfigEntry<T>, ConfigScreen> createConfigScreen(T value) {
	//	return configEntry -> configEntry.container().extension(TweedCoatBridgeExtension.class)
	//			.map(extension -> extension.createConfigScreen(configEntry, value))
	//			.orElseThrow(() -> new IllegalStateException("No TweedCoatBridgeExtension present"));
	//}

	void addMapper(TweedCoatMapper<?> mapper);

	<T> ConfigScreen createConfigScreen(ConfigScreenCreateParams<T> params);

	@Override
	default String getId() {
		return EXTENSION_ID;
	}
}
