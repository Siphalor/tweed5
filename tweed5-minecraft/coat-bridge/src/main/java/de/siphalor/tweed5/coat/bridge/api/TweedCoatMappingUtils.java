package de.siphalor.tweed5.coat.bridge.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TweedCoatMappingUtils {
	public static MutableComponent translatableComponentWithFallback(String translationKey, @Nullable String fallback) {
		return Component.translatableWithFallback(translationKey, fallback == null ? "" : fallback);
		// FIXME
		//if (I18n.exists(translationKey)) {
		//	return Component.translatable(translationKey);
		//} else if (fallback != null) {
		//	return Component.literal(fallback);
		//} else {
		//	return Component.empty();
		//}
	}
}
