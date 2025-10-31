package de.siphalor.tweed5.coat.bridge.impl;

import de.siphalor.coat.util.EnumeratedMaterial;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.chat.Component;

import java.util.Locale;

import static de.siphalor.tweed5.coat.bridge.api.TweedCoatMappingUtils.translatableComponentWithFallback;

@RequiredArgsConstructor
public class CoatEnumMaterial<T> implements EnumeratedMaterial<T> {
	private final Class<T> enumClass;
	private final String textTranslationKeyPrefix;

	@Override
	public T[] values() {
		return enumClass.getEnumConstants();
	}

	@Override
	public Component asText(T value) {
		return translatableComponentWithFallback(
				textTranslationKeyPrefix + "." + value.toString().toLowerCase(Locale.ROOT),
				value.toString()
		);
	}
}
