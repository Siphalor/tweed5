package de.siphalor.tweed5.coat.bridge.api.mapping;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class TweedCoatEntryCreationContext<T> {
	private final ConfigEntry<T> entry;
	private final T currentValue;
	private final T defaultValue;
	private final @Nullable Consumer<T> parentSaveHandler;
}
