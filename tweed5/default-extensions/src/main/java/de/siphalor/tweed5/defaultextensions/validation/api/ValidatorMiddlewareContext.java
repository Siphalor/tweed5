package de.siphalor.tweed5.defaultextensions.validation.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
public class ValidatorMiddlewareContext {
	ConfigEntry<?> entry;
}
