package de.siphalor.tweed5.serde.extension.api.extension;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import lombok.*;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReaderMiddlewareContext {
	ConfigEntry<?> entry;
}
