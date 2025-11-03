package de.siphalor.tweed5.coat.bridge.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
//- import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
//- import net.minecraft.network.chat.TextComponent;
//- import net.minecraft.network.chat.TranslatableComponent;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TweedCoatMappingUtils {
	public static MutableComponent translatableComponentWithFallback(String translationKey, @Nullable String fallback) {
		//# if MC_VERSION_NUMBER >= 11900
		return Component.translatableWithFallback(translationKey, fallback == null ? "" : fallback);
		//# else
		//- if (I18n.exists(translationKey)) {
		//- 	return new TranslatableComponent(translationKey);
		//- } else if (fallback != null) {
		//- 	return new TextComponent(fallback);
		//- } else {
		//- 	return new TextComponent("");
		//- }
		//# end
	}

	public static MutableComponent translatableComponent(String translationKey, Object... args) {
		//# if MC_VERSION_NUMBER >= 11900
		return Component.translatable(translationKey, args);
		//# else
		//- return new TranslatableComponent(translationKey, args);
		//# end
	}

	public static MutableComponent literalComponent(String literal) {
		//# if MC_VERSION_NUMBER >= 11900
		return Component.literal(literal);
		//# else
		//- return new TextComponent(literal);
		//# end
	}
}
