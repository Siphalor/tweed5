package de.siphalor.tweed5.coat.bridge.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import lombok.Builder;
import lombok.Getter;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

@Builder
@Getter
public class ConfigScreenCreateParams<T> {
	private final ConfigEntry<T> rootEntry;
	private final T currentValue;
	private final T defaultValue;
	private final Component title;
	private final String translationKeyPrefix;
	private final Consumer<T> saveHandler;
}
