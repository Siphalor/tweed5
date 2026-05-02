package de.siphalor.tweed5.weaver.pojo.impl.weaving.config;

import de.siphalor.tweed5.namingformat.api.NamingFormat;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCompoundConfigEntry;
import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Builder
@Value
public class CompoundWeavingConfig {

	@Nullable NamingFormat compoundSourceNamingFormat;
	@Nullable NamingFormat compoundTargetNamingFormat;
	@SuppressWarnings("rawtypes")
	@Nullable Class<? extends WeavableCompoundConfigEntry> compoundEntryClass;

	public static CompoundWeavingConfig withOverrides(CompoundWeavingConfig self, CompoundWeavingConfig overrides) {
		return CompoundWeavingConfig.builder()
				.compoundSourceNamingFormat(overrides.compoundSourceNamingFormat() != null ? overrides.compoundSourceNamingFormat() : self.compoundSourceNamingFormat())
				.compoundTargetNamingFormat(overrides.compoundTargetNamingFormat() != null ? overrides.compoundTargetNamingFormat() : self.compoundTargetNamingFormat())
				.compoundEntryClass(overrides.compoundEntryClass() != null ? overrides.compoundEntryClass() : self.compoundEntryClass())
				.build();
	}
}
