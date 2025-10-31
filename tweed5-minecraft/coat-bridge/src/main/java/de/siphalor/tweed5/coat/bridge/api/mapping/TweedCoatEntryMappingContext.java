package de.siphalor.tweed5.coat.bridge.api.mapping;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
@Getter
public
class TweedCoatEntryMappingContext {
	@Getter(AccessLevel.NONE)
	private final TweedCoatMapper<?> mappingDelegate;
	private final String entryName;
	private final String translationKeyPrefix;
	private final @Nullable Class<?> parentWidgetClass;

	public static Builder rootBuilder(TweedCoatMapper<?> mappingDelegate, String translationKeyPrefix) {
		return new Builder(mappingDelegate, "root", translationKeyPrefix);
	}

	public <T> TweedCoatEntryMappingResult<T, ?> mapEntry(ConfigEntry<T> entry, TweedCoatEntryMappingContext context) {
		//noinspection unchecked,rawtypes
		return (TweedCoatEntryMappingResult<T, ?>) mappingDelegate.mapEntry((ConfigEntry) entry, context);
	}

	public Builder subContextBuilder(String entryName) {
		return new Builder(mappingDelegate, entryName, translationKeyPrefix + "." + entryName);
	}

	@Setter
	public static class Builder {
		private final TweedCoatMapper<?> mappingDelegate;
		private final String entryName;
		private String translationKeyPrefix;
		private @Nullable Class<?> parentWidgetClass;

		private Builder(TweedCoatMapper<?> mappingDelegate, String entryName, String translationKeyPrefix) {
			this.mappingDelegate = mappingDelegate;
			this.entryName = entryName;
			this.translationKeyPrefix = translationKeyPrefix;
		}

		public TweedCoatEntryMappingContext build() {
			return new TweedCoatEntryMappingContext(mappingDelegate, entryName, translationKeyPrefix, parentWidgetClass);
		}
	}
}
