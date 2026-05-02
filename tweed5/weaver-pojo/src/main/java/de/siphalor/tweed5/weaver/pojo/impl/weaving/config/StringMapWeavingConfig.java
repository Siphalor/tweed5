package de.siphalor.tweed5.weaver.pojo.impl.weaving.config;

import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableStringMapConfigEntry;
import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Builder
@Value
public class StringMapWeavingConfig {

	@SuppressWarnings("rawtypes")
	@Nullable Class<? extends WeavableStringMapConfigEntry> stringMapEntryClass;

	public static StringMapWeavingConfig withOverrides(StringMapWeavingConfig self, StringMapWeavingConfig overrides) {
		return StringMapWeavingConfig.builder()
				.stringMapEntryClass(overrides.stringMapEntryClass() != null ? overrides.stringMapEntryClass() : self.stringMapEntryClass())
				.build();
	}
}
