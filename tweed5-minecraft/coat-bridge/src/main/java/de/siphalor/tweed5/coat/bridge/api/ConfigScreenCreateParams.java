package de.siphalor.tweed5.coat.bridge.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import lombok.Builder;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@Builder
@Getter
public class ConfigScreenCreateParams<T extends @Nullable Object> {
	private final ConfigEntry<T> rootEntry;
	private final T currentValue;
	private final T defaultValue;
	/**
	 * The title of the screen, defaults to {@code translationKeyPrefix + ".title"}
	 */
	private final @Nullable Component title;
	/**
	 * The translation key prefix for all entries without a trailing dot.
	 */
	private final String translationKeyPrefix;
	private final @Nullable Consumer<T> saveHandler;
}
