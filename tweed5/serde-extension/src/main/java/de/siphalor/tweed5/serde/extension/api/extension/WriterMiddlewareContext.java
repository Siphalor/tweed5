package de.siphalor.tweed5.serde.extension.api.extension;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class WriterMiddlewareContext {
	ConfigEntry<?> entry;
}
