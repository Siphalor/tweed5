package de.siphalor.tweed5.weaver.pojo.impl.weaving.compound;

import de.siphalor.tweed5.namingformat.api.NamingFormat;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCompoundConfigEntry;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Builder
@Value
public class CompoundWeavingConfigImpl implements CompoundWeavingConfig {
	private static final CompoundWeavingConfigImpl EMPTY = CompoundWeavingConfigImpl.builder().build();

	NamingFormat compoundSourceNamingFormat;
	NamingFormat compoundTargetNamingFormat;
	@SuppressWarnings("rawtypes")
	@Nullable
	Class<? extends WeavableCompoundConfigEntry> compoundEntryClass;

	public static CompoundWeavingConfigImpl withOverrides(CompoundWeavingConfig self, CompoundWeavingConfig overrides) {
		return CompoundWeavingConfigImpl.builder()
				.compoundSourceNamingFormat(overrides.compoundSourceNamingFormat() != null ? overrides.compoundSourceNamingFormat() : self.compoundSourceNamingFormat())
				.compoundTargetNamingFormat(overrides.compoundTargetNamingFormat() != null ? overrides.compoundTargetNamingFormat() : self.compoundTargetNamingFormat())
				.compoundEntryClass(overrides.compoundEntryClass() != null ? overrides.compoundEntryClass() : self.compoundEntryClass())
				.build();
	}
}
